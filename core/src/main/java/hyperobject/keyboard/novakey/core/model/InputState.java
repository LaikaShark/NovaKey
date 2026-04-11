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

package hyperobject.keyboard.novakey.core.model;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

/**
 * Snapshot of the current typing session: what kind of field the user
 * is on (password / email / URI / plain text, plus the broad class —
 * text, number, phone, datetime), the composing-text buffer, selection
 * and candidate spans, and a few stateful counters used by input logic
 * such as repeat-close tracking and "return to default keyboard after
 * space."
 * <p>
 * Written by the IME on {@code onStartInput} (via {@link #updateEditorInfo})
 * and on every {@code onUpdateSelection} (via {@link #updateSelection}).
 * Read by actions and touch handlers that need to know the field type or
 * the current cursor/composing region.
 */
public class InputState {

    private boolean mOnPassword = false;
    private boolean mOnEmailAddress = false;
    private boolean mOnURI = false;
    private Type mType = Type.TEXT;

    private int mRepeatCount = 0;//current count of closing chars
    private StringBuilder mComposing = new StringBuilder();
    private boolean mReturnAfterSpace;

    private int mOldSelelectionStart;
    private int mOldSelectionEnd;
    private int mSelectionStart;
    private int mSelectionEnd;
    private int mCandidatesStart;
    private int mCandidatesEnd;


    /**
     * Refreshes the field-type flags from a fresh {@link EditorInfo}.
     * Branches on {@code inputType}'s class/variation masks to set one
     * of the four {@link Type} values and toggle the password/email/URI
     * booleans, then clears any leftover composing text from the
     * previous session.
     */
    public void updateEditorInfo(EditorInfo editorInfo) {
        mComposing.setLength(0);

        int inputType = editorInfo.inputType;

        int var = inputType & InputType.TYPE_MASK_VARIATION,
                flags = inputType & InputType.TYPE_MASK_FLAGS;

        switch (inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_TEXT:
                mType = Type.TEXT;
                switch (inputType & InputType.TYPE_MASK_VARIATION) {
                    case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                    case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        mOnPassword = true;
                        break;
                    case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        mOnEmailAddress = true;
                        break;
                    case InputType.TYPE_TEXT_VARIATION_URI:
                        mOnURI = true;
                        break;
                }
                break;
            case InputType.TYPE_CLASS_NUMBER:
                mType = Type.NUMBER;
                if (var == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                    mOnPassword = true;
                break;
            case InputType.TYPE_CLASS_DATETIME:
                mType = Type.DATETIME;
                break;
            case InputType.TYPE_CLASS_PHONE:
                mType = Type.PHONE;
                break;
        }
    }


    /**
     * Mirrors the editor's selection/candidates spans into this state
     * bag. Called from the IME's {@code onUpdateSelection} so cursor-mode
     * code and composing-text logic can read the current positions
     * without re-asking the InputConnection.
     */
    public void updateSelection(int oldSelStart, int oldSelEnd,
                                int newSelStart, int newSelEnd,
                                int candidatesStart, int candidatesEnd) {
        mOldSelelectionStart = oldSelStart;
        mOldSelectionEnd = oldSelEnd;
        mSelectionStart = newSelStart;
        mSelectionEnd = newSelEnd;
        mCandidatesStart = candidatesStart;
        mCandidatesEnd = candidatesEnd;
    }


    /** Returns the current composing-text buffer as a plain {@link String}. */
    public String getComposingText() {
        return mComposing.toString();
    }


    /** Empties the composing-text buffer without touching anything else. */
    public void clearComposingText() {
        mComposing.setLength(0);
    }


    /**
     * Replaces the composing-text buffer with {@code text}. Resets length
     * to zero first so old characters do not leak through.
     */
    public void setComposingText(String text) {
        mComposing.setLength(0);
        mComposing.append(text);
    }


    /**
     * Returns whether the keyboard should snap back to the default
     * alphabet layout after the next space — flagged when a single-shot
     * punctuation/symbol visit was triggered mid-word.
     */
    public boolean returnAfterSpace() {
        return mReturnAfterSpace;
    }


    /** Sets the "return to default after space" flag. */
    public void setReturnAfterSpace(boolean returnAfterSpace) {
        mReturnAfterSpace = returnAfterSpace;
    }


    /**
     * Increments the repeat-close counter. Used by punctuation auto-close
     * logic to track how many identical closing chars have been emitted
     * in a row.
     */
    public void incrementRepeat() {
        mRepeatCount++;
    }


    /** Resets the repeat-close counter back to zero. */
    public void resetRepeat() {
        mRepeatCount = 0;
    }


    /** Returns the current value of the repeat-close counter. */
    public int getRepeatCount() {
        return mRepeatCount;
    }


    /** True if the active field is flagged as a password variant. */
    public boolean onPassword() {
        return mOnPassword;
    }


    /** True if the active field is flagged as an email-address variant. */
    public boolean onEmailAddress() {
        return mOnEmailAddress;
    }


    /** True if the active field is flagged as a URI variant. */
    public boolean onURI() {
        return mOnURI;
    }


    /**
     * True if auto-correct and composing-text tracking should be used for
     * the active field — suppressed on passwords, emails, and URIs so the
     * IME doesn't garble credentials or addresses.
     */
    public boolean shouldAutoCorrect() {
        return !onPassword() && !onEmailAddress() && !onURI();
    }


    /** Returns the broad input-type class of the active field. */
    public Type getType() {
        return mType;
    }


    /**
     * Broad input-type classes NovaKey cares about when picking a
     * starting keyboard layout for a field.
     */
    public enum Type {
        TEXT,
        NUMBER,
        PHONE,
        DATETIME
    }


    /** Previous selection start, as reported by the last {@code onUpdateSelection}. */
    public int getOldSelelectionStart() {
        return mOldSelelectionStart;
    }


    /** Previous selection end, as reported by the last {@code onUpdateSelection}. */
    public int getOldSelectionEnd() {
        return mOldSelectionEnd;
    }


    /** Current selection start (or caret position when start == end). */
    public int getSelectionStart() {
        return mSelectionStart;
    }


    /** Current selection end (or caret position when start == end). */
    public int getSelectionEnd() {
        return mSelectionEnd;
    }


    /** Start of the composing-text region in the editor, or -1 when none. */
    public int getCandidatesStart() {
        return mCandidatesStart;
    }


    /** End of the composing-text region in the editor, or -1 when none. */
    public int getCandidatesEnd() {
        return mCandidatesEnd;
    }
}
