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

package hyperobject.keyboard.novakey.core;

import android.content.ClipboardManager;
import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

/**
 * Abstract {@link InputMethodService} that defines the host-contract
 * every NovaKey IME implementation must satisfy. The rest of the core
 * module talks to the OS through this narrow interface instead of
 * depending directly on {@link InputConnection} / {@link EditorInfo},
 * so actions, elements, and touch handlers can remain platform-agnostic.
 * <p>
 * The concrete subclass lives in the {@code :app} module as
 * {@code MainNovaKeyService} (declared in {@code AndroidManifest.xml})
 * and is the class Android actually instantiates when the user enables
 * "NovaKey" under Languages &amp; input. That subclass is responsible
 * for:
 * <ul>
 *   <li>Routing {@link #inputText}, {@link #commitCorrection} and the
 *       cursor / selection helpers through the current
 *       {@link InputConnection}.</li>
 *   <li>Driving the device vibrator for {@link #vibrate(long)}.</li>
 *   <li>Exposing the system {@link ClipboardManager} via
 *       {@link #getClipboard()}.</li>
 *   <li>Querying {@link #getCurrentCapsMode()} from the underlying
 *       {@link EditorInfo#inputType} so {@code ShiftAction} can decide
 *       when to auto-capitalise.</li>
 * </ul>
 * All of the standard {@code InputMethodService} lifecycle hooks
 * ({@code onCreate}, {@code onStartInput}, {@code onCreateInputView}, …)
 * are inherited unchanged — subclasses override whichever of them they
 * need and leave the rest to the platform.
 */
public abstract class NovaKeyService extends InputMethodService {

    /**
     * Returns the device {@link ClipboardManager} so actions can read
     * or write the system clipboard. Kept abstract so the core module
     * never has to import {@link android.content.Context}.
     *
     * @return this device's clipboard manager
     */
    public abstract ClipboardManager getClipboard();


    /**
     * Fires the device vibrator for the given duration. Used by the
     * {@code VibrateAction} family for haptic feedback on key press.
     * Implementations should respect the user's vibrate-level setting.
     *
     * @param milliseconds amount of time to activate the vibrator for
     */
    public abstract void vibrate(long milliseconds);


    /**
     * Commits {@code text} to the active input field verbatim, as
     * though it had been typed.
     *
     * @param text         text to commit
     * @param newCursorPos position the cursor should end at, using the
     *                     same semantics as
     *                     {@link InputConnection#commitText(CharSequence, int)}
     */
    public abstract void inputText(String text, int newCursorPos);


    /**
     * Pulls the full editable text plus selection bounds out of the
     * target field via an {@link ExtractedTextRequest}. The returned
     * {@link ExtractedText} receives monitor updates if the
     * implementation asked for them at {@code onStartInput} time.
     * <p>
     * This round-trip is expensive and callers should cache the result
     * when possible.
     *
     * @return extracted text, or {@code null} if no field is active
     */
    public abstract ExtractedText getExtractedText();


    /**
     * Returns the text currently highlighted by the caret selection,
     * or {@code null}/empty if nothing is selected.
     *
     * @return the text between {@code selectionStart} and
     *         {@code selectionEnd}
     */
    public abstract String getSelectedText();


    /**
     * Queries the current capitalization mode at the caret, from which
     * {@code UpdateShiftAction} decides whether to auto-shift.
     *
     * @return 1 for caps, 0 for not caps
     */
    public abstract int getCurrentCapsMode();


    /**
     * Shifts the selection endpoints relative to their current values —
     * useful for extending or contracting a selection one character at
     * a time.
     *
     * @param deltaStart delta to add to {@code selectionStart}
     * @param deltaEnd   delta to add to {@code selectionEnd}
     */
    public abstract void moveSelection(int deltaStart, int deltaEnd);


    /**
     * Moves the selection to an absolute character range.
     *
     * @param start index of the selection start
     * @param end   index of the selection end (may equal {@code start}
     *              for a plain caret position)
     */
    public abstract void setSelection(int start, int end);


    /**
     * Commits whatever composing span is active, applying the best
     * available correction (spellcheck suggestion, completion, etc).
     * The composition ends and the corrected text becomes permanent.
     */
    public abstract void commitCorrection();


    /**
     * Replaces the current composing text with {@code text} and
     * commits it, without running any correction logic.
     *
     * @param text replacement text
     */
    public abstract void commitReplacementText(String text);


    /**
     * Commits the current composing text exactly as typed, bypassing
     * corrections. Finishes the composition span.
     */
    public abstract void commitComposingText();
}
