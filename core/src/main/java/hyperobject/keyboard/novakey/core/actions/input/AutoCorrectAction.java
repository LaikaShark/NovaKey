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

import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;

/**
 * Attempts to autocorrect the current composing text. Fired by the
 * long-press timer when the user completes a shift gesture and holds
 * the finger down for {@link Settings#longPressTime} ms.
 * <p>
 * How: checks whether autocorrect is enabled, the field supports it
 * (not a password/email/URI), and the caret sits at the end of the
 * composing region. If all conditions are met, commits the correction
 * via {@link NovaKeyService#commitCorrection()}. Otherwise falls back
 * to a normal {@link ShiftAction} so the hold is not wasted.
 */
public class AutoCorrectAction implements Action<Void> {

    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        InputState state = model.getInputState();

        boolean caretAtComposingEnd =
                state.getSelectionStart() == state.getSelectionEnd()
                        && state.getCandidatesEnd() >= 0
                        && state.getSelectionEnd() == state.getCandidatesEnd();

        if (Settings.autoCorrect && state.shouldAutoCorrect() && caretAtComposingEnd) {
            ime.commitCorrection();
        } else {
            control.fire(new ShiftAction());
        }
        return null;
    }
}
