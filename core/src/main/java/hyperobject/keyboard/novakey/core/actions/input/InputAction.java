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

package hyperobject.keyboard.novakey.core.actions.input;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Commits an arbitrary string into the current editor through the
 * input connection. The "universal insert" action used for paste,
 * emoji insertion, auto-replacement of selected text, and anywhere
 * else a block of text (as opposed to a single key press) needs to
 * land in the field.
 * <p>
 * Before committing, this action calls {@code finishComposingText()}
 * and clears the model's composing-text buffer so the new string
 * replaces any half-finished word rather than being appended to it.
 */
public class InputAction implements Action<Void> {

    private final String mText;
    private final int mCursorPos;


    /**
     * Commits {@code text} and leaves the cursor at the end of it
     * (equivalent to {@code new InputAction(text, false)}).
     */
    public InputAction(String text) {
        this(text, true);
    }


    /**
     * @param text         the literal string to insert
     * @param beforeCursor {@code true} to leave the cursor <em>before</em>
     *                     the inserted text (cursorPos 0); {@code false}
     *                     to leave it <em>after</em> (cursorPos 1). These
     *                     map directly onto the {@code newCursorPosition}
     *                     argument of
     *                     {@link android.view.inputmethod.InputConnection#commitText}
     */
    public InputAction(String text, boolean beforeCursor) {
        mCursorPos = beforeCursor ? 0 : 1;
        mText = text;
    }


    /**
     * Finishes any in-progress composing text, clears the model's
     * composing buffer, then commits {@code mText} through the input
     * connection at the configured cursor offset. No-op if
     * {@code mText} is {@code null}.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        if (mText != null) {
            ime.getCurrentInputConnection().finishComposingText();
            model.getInputState().clearComposingText();

            ime.getCurrentInputConnection().commitText(mText, mCursorPos);
        }
        return null;
    }
}
