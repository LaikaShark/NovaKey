/*
 * NovaKey - An alternative touchscreen input method
 * Copyright (C) 2019  Viviano Cantu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 * Any questions about the program or source may be directed to <strellastudios@gmail.com>
 */

package hyperobject.keyboard.novakey;

import android.content.ClipboardManager;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.Clipboard;
import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.controller.Corrections;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.utils.Print;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.view.themes.AppTheme;

/**
 * Concrete phone/tablet IME — the entry point declared in
 * {@code app/src/main/AndroidManifest.xml}. Extends the abstract
 * {@link NovaKeyService} from {@code :core} and wires the phone-side
 * system services ({@link Vibrator}, {@link ClipboardManager},
 * {@link WindowManager}) into the shared {@link Controller}.
 * <p>
 * Also owns the IME-side text editing operations used by action
 * handlers: text commits, composing-region management, cursor moves,
 * selection queries, caps-mode detection, and the still-experimental
 * floating-window hooks at the bottom of the file.
 * <p>
 * The {@link #MY_PREFERENCES} SharedPreferences name is deliberately
 * preserved across the 2026 modernization pass so existing installs
 * don't lose their settings on upgrade — do not rename it.
 */
public class MainNovaKeyService extends NovaKeyService {

    /**
     * SharedPreferences filename used for the setup wizard's
     * {@code has_setup}/{@code progress} flags. Preserved verbatim
     * from the legacy codebase so upgraded installs continue to read
     * their existing state; renaming this would silently re-run the
     * setup wizard for every existing user.
     */
    public static String MY_PREFERENCES = "MyPreferences";
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private Vibrator vibrator;
    private ClipboardManager clipboard;
    private WindowManager windowManager;
    private List<View> mWindows;

    private Controller mController;


    /**
     * One-shot IME lifecycle hook fired when the service is first
     * created by the system. Responsibilities:
     * <ul>
     *   <li>Apply the app theme to the service's application context
     *       so that drawable lookups used by the keyboard see the right
     *       resources.</li>
     *   <li>Cache the system services the IME depends on (clipboard,
     *       vibrator, window manager) and register a primary-clip
     *       listener that feeds each copy into the in-memory
     *       {@link Clipboard} history.</li>
     *   <li>Construct the {@link Controller}, which builds the model,
     *       view, themes, fonts, keyboards, elements, and touch
     *       handlers.</li>
     *   <li>Mark {@code has_setup=true} in {@link #MY_PREFERENCES} so
     *       subsequent cold-starts of {@code SettingsActivity} skip the
     *       setup wizard.</li>
     * </ul>
     */
    @Override
    public void onCreate() {
        getApplicationContext().setTheme(R.style.AppTheme);
        super.onCreate();

        // Note: the API 15+ edge-to-edge IME fix lives in MainView.onMeasure,
        // which grows the reported height by the navigation bar inset so the
        // keys aren't drawn behind the navbar. setDecorFitsSystemWindows on
        // the IME's Window had no effect here.

        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(() -> {
            try {
                Clipboard.add(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
            } catch (NullPointerException e) {
            }
        });
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindows = new ArrayList<>();

        mController = new Controller(this);

        Editor temp = getApplicationContext().getSharedPreferences(MainNovaKeyService.MY_PREFERENCES, MODE_PRIVATE).edit();
        temp.putBoolean("has_setup", true);
        temp.commit();

    }


    /**
     * Returns the view that the framework should render as the
     * keyboard. Reloads {@link AppTheme} against the current resources
     * (picks up night-mode / config changes), then detaches the
     * controller's view from any previous parent — the framework may
     * call this multiple times and Android forbids a view having two
     * parents.
     */
    @Override
    public View onCreateInputView() {
        AppTheme.load(this, getResources());

        View v = mController.getView();
        if (v.getParent() != null)
            ((ViewGroup) v.getParent()).removeView(v);
        return v;
    }


    /**
     * Fired when the IME binds to an input connection — at this point
     * {@code getCurrentInputConnection()} and
     * {@code getCurrentInputBinding()} become valid. Intentionally
     * empty; the work is done in {@link #onStartInput(EditorInfo, boolean)}.
     */
    @Override
    public void onBindInput() {
    }


    /**
     * Fired each time the user focuses a new editor field. Forwards
     * the {@link EditorInfo} to the model's {@code InputState} (so
     * things like password mode, content hint, and initial capitals
     * propagate) and asks the controller to redraw.
     */
    @Override
    public void onStartInput(EditorInfo info, boolean restarting) {
        super.onStartInput(info, restarting);

        mController.getModel().onStart(info);
        mController.invalidate();
    }


    /**
     * Tracks selection and cursor changes made externally (user taps
     * elsewhere, IME commits text, etc.) and keeps the internal
     * composing region in sync with the editor.
     * <p>
     * How: updates {@link InputState#updateSelection}, then — if
     * auto-correct is on and the field allows it — recomputes the
     * composing region around a single cursor by walking outward from
     * the cursor position and stopping at the first character that
     * isn't a letter/number/apostrophe. The computed {@code [s, e)}
     * window is pushed back into the editor via
     * {@link InputConnection#setComposingRegion(int, int)}. A
     * non-collapsed selection clears the composing region instead.
     * <p>
     * Note: contains a leftover {@code Print.et(...)} debug call at
     * the end.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(
                oldSelStart, oldSelEnd,
                newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        InputState is = mController.getModel().getInputState();

        is.updateSelection(
                oldSelStart, oldSelEnd,
                newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        if (!Settings.autoCorrect || !is.shouldAutoCorrect())
            return;
        if (newSelStart == newSelEnd) {
            ExtractedText et = getExtractedText();
            if (et == null) {
                is.clearComposingText();
                return;
            }
            String text = et.text.toString();
            if (text.length() == 0) {
                is.clearComposingText();
                return;
            }

            int e, s;//start of end of composing

            for (s = Math.min(text.length(), newSelStart); s > 0; s--) {
                char c = text.charAt(s - 1);
                if (!Character.isLetter(c) && !Util.isNumber(c) && c != '\'')
                    break;
            }

            for (e = Math.min(text.length(), newSelEnd); e < text.length(); e++) {
                char c = text.charAt(e);
                if (!Character.isLetter(c) && !Util.isNumber(c) && c != '\'')
                    break;
            }

            getCurrentInputConnection().setComposingRegion(s, e);
            is.setComposingText(text.substring(s, e));
        } else {
            getCurrentInputConnection().finishComposingText();
            is.clearComposingText();
        }


        Print.et(getExtractedText());
    }


    /** Unused hook kept to satisfy the framework contract. */
    @Override
    public void onExtractingInputChanged(EditorInfo ei) {
    }


    /** Unused hook kept to satisfy the framework contract. */
    @Override
    public void onUpdateExtractedText(int token, ExtractedText text) {
    }


    /**
     * Cursor-anchor updates are ignored — the IME does not render an
     * inline candidate strip, and the needed selection info already
     * arrives through {@link #onUpdateSelection}. Pre-API-21 devices
     * would have used the removed {@code onUpdateCursor(Rect)} path.
     */
    @Override
    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
    }


    /**
     * Fired when the IME disconnects from the editor (user dismisses
     * the keyboard or moves to a field that hides it). Redraws so any
     * per-field UI state is cleared on the next show.
     */
    @Override
    public void onFinishInput() {
        super.onFinishInput();
        //TODO: reset state
        mController.invalidate();
    }


    /**
     * Terminal IME hook invoked when the service is being destroyed.
     * Nothing to release manually today — the view and controller are
     * garbage-collected once the service reference goes away.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Keeps the IME out of fullscreen (extract-view) mode even on
     * landscape devices so the app behind the keyboard stays visible.
     *
     * @return {@code false} always
     */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }


    /**
     * Exposes the system clipboard to internal actions (paste, clipboard
     * menu). Safe to call any time after {@link #onCreate()}.
     */
    public ClipboardManager getClipboard() {
        return clipboard;
    }


    /**
     * Short haptic feedback pulse, gated on the {@code vibrate} user
     * preference.
     *
     * @param milliseconds duration of the pulse in ms
     */
    public void vibrate(long milliseconds) {
        if (Settings.vibrate)
            vibrator.vibrate(milliseconds);
    }


    /**
     * Commits the given text to the editor verbatim.
     * <p>
     * How: finalizes any pending composing text first (so the new text
     * replaces the old composing region cleanly), then calls
     * {@link InputConnection#commitText(CharSequence, int)}.
     *
     * @param text         text to input
     * @param newCursorPos cursor position relative to the committed text
     */
    public void inputText(String text, int newCursorPos) {
        commitComposingText();

        getCurrentInputConnection().commitText(text, newCursorPos);
    }


    /**
     * Pulls an {@link ExtractedText} snapshot of the focused field.
     * Expensive — the framework may marshal large amounts of text
     * across the binder, so callers should avoid calling it on every
     * keystroke.
     */
    public ExtractedText getExtractedText() {
        return getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0);
    }


    /**
     * Returns the currently selected text, or the empty string if the
     * selection is collapsed or the {@link InputConnection} refused to
     * provide it.
     */
    public String getSelectedText() {
        CharSequence cs = getCurrentInputConnection().getSelectedText(0);
        if (cs != null)
            return cs.toString();
        return "";
    }


    /**
     * Returns the effective caps mode at the current cursor position.
     * <p>
     * How: asks the input connection for
     * {@link InputConnection#getCursorCapsMode(int)} when available;
     * otherwise falls back to inspecting the {@link EditorInfo} input
     * type directly and only reporting caps-needed for plain text
     * fields with the caps flag set.
     *
     * @return 1 if caps should be applied, 0 otherwise
     */
    public int getCurrentCapsMode() {
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return (ei.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT
                    && (ei.inputType & 0x4000) == 0 ? 0 : 1;
        else
            return ic.getCursorCapsMode(ei.inputType);
    }


    /**
     * Shifts the current selection by the given deltas, clamping both
     * ends to zero. If the resulting start exceeds end, the two are
     * swapped before being applied so the selection never inverts.
     *
     * @param deltaStart amount to add to the selection start
     * @param deltaEnd   amount to add to the selection end
     */
    public void moveSelection(int deltaStart, int deltaEnd) {
        ExtractedText et = getExtractedText();
        int s = et.selectionStart + deltaStart, e = et.selectionEnd + deltaEnd;
        s = s < 0 ? 0 : s;
        e = e < 0 ? 0 : e;
        if (s <= e)
            getCurrentInputConnection().setSelection(s, e);
        else {
            getCurrentInputConnection().setSelection(e, s);
        }
    }


    /**
     * Sets the selection to an absolute range. The two-step
     * {@code setSelection(start, start)} then {@code setSelection(start, end)}
     * call sequence is intentional — some editors ignore the range
     * unless the cursor is first collapsed to the new anchor.
     *
     * @param start selection anchor
     * @param end   selection end
     */
    public void setSelection(int start, int end) {
        getCurrentInputConnection().setSelection(start, start);
        getCurrentInputConnection().setSelection(start, end);
    }


    /**
     * Replaces the current composing text with the best-guess
     * correction returned by {@link Corrections}, then finalizes the
     * composing region.
     */
    public void commitCorrection() {
        InputConnection ic = getCurrentInputConnection();
        InputState is = mController.getModel().getInputState();
        Corrections corrections = mController.getModel().getCorrections();

        String text = corrections.correction(is.getComposingText());
        // not calling commitReplacementText(text) in case the logic changes later

        is.setComposingText(text);
        ic.setComposingText(text, 1);

        ic.finishComposingText();
        is.clearComposingText();
    }


    /**
     * Replaces the current composing text with a fixed replacement and
     * finalizes the composing region. Used by code paths that want to
     * overwrite the composing run without going through the corrections
     * engine.
     */
    public void commitReplacementText(String text) {
        InputConnection ic = getCurrentInputConnection();
        InputState is = mController.getModel().getInputState();

        is.setComposingText(text);
        ic.setComposingText(text, 1);

        ic.finishComposingText();
        is.clearComposingText();
    }


    /**
     * Commits the current composing text as-is (no correction applied)
     * and clears the model's composing-text cache. Called by
     * {@link #inputText} before inserting new text and by anything else
     * that needs to finalize the composing run without changing it.
     */
    public void commitComposingText() {
        getCurrentInputConnection().finishComposingText();
        mController.getModel().getInputState().clearComposingText();
    }


    /**
     * Adds a floating view to the window manager — experimental
     * support for detaching the keyboard from the IME region. Stored
     * in {@link #mWindows} so {@link #clearWindows()} can tear them all
     * down at once.
     *
     * @param view       view to add as a floating window
     * @param fullscreen whether the window should fill the screen or
     *                   size to its content
     */
    public void addWindow(View view, boolean fullscreen) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                fullscreen ? WindowManager.LayoutParams.MATCH_PARENT :
                        WindowManager.LayoutParams.WRAP_CONTENT,
                fullscreen ? WindowManager.LayoutParams.MATCH_PARENT :
                        WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        mWindows.add(view);
        windowManager.addView(view, params);
    }


    /**
     * Removes every floating view added via {@link #addWindow}. Uses a
     * remove-and-index pattern because the list is mutated in place.
     */
    public void clearWindows() {
        for (int i = 0; i < mWindows.size(); i++) {
            windowManager.removeView(mWindows.remove(i));
        }
    }


    /**
     * Physical-key handler. Currently short-circuited by a
     * {@code if (false)} guard; the original intent was to close the
     * floating window on BACK/HOME/APP_SWITCH once the IME had been
     * undocked from its normal position.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (false) { // should be if undocked
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_APP_SWITCH)
                close();
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * Unreferenced experimental helper that adds the controller's view
     * to the window manager as a WRAP_CONTENT floating phone window.
     */
    private void open() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(mController.getView(), params);
    }


    /**
     * Tear-down counterpart to {@link #open()}. Swallows exceptions
     * since removeView throws if the view was never attached.
     */
    private void close() {
        try {
            windowManager.removeView(mController.getView());
        } catch (Exception e) {
        }
    }

}
