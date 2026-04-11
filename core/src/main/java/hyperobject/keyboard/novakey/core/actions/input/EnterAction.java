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

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Handles the Enter/Return gesture in a way that does the right thing
 * for both single-line action fields ("Send", "Search", "Done" …) and
 * multi-line composers (chat boxes, notes). Single-line fields get the
 * editor's declared IME action fired; multi-line fields get a literal
 * newline committed so Enter inserts a line break instead of submitting
 * the message.
 * <p>
 * After the insertion, fires an {@link UpdateShiftAction} so the next
 * letter auto-capitalizes at the start of a new sentence/line.
 */
public class EnterAction implements Action<Void> {
    /**
     * Runs the enter logic described in the class doc.
     * <p>
     * How: inspects {@link EditorInfo#inputType} for either of the two
     * multi-line flags ({@code TYPE_TEXT_FLAG_MULTI_LINE} and
     * {@code TYPE_TEXT_FLAG_IME_MULTI_LINE}). If either is set, commits
     * a "\n" through the input connection; otherwise falls back to
     * {@link NovaKeyService#sendDefaultEditorAction(boolean)} which
     * triggers whatever action the field declared. Either way, queues
     * an {@link UpdateShiftAction} to refresh the shift state.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        // Multi-line text fields (e.g. Discord's message composer) expect
        // Enter to insert a newline, not to perform whatever imeAction they
        // declared for the keyboard's action button. sendDefaultEditorAction
        // would unconditionally fire that action — send the message, run the
        // search, etc. — so detect multi-line input and commit "\n" directly.
        EditorInfo ei = ime.getCurrentInputEditorInfo();
        InputConnection ic = ime.getCurrentInputConnection();

        boolean multiLine = ei != null && (ei.inputType
                & (InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)) != 0;

        if (multiLine && ic != null) {
            ic.commitText("\n", 1);
        } else {
            ime.sendDefaultEditorAction(true);
        }

        control.fire(new UpdateShiftAction());
        return null;
    }
}
