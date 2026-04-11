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

package viviano.cantu.novakey.core.actions.input;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import viviano.cantu.novakey.core.controller.Controller;
import viviano.cantu.novakey.core.actions.Action;
import viviano.cantu.novakey.core.NovaKeyService;
import viviano.cantu.novakey.core.model.Model;

/**
 * Created by Viviano on 6/20/2016.
 */
public class EnterAction implements Action<Void> {
    /**
     * Called when the action is triggered
     * Actual logic for the action goes here
     *  @param ime
     * @param control
     * @param model
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
