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
import hyperobject.keyboard.novakey.core.actions.SetShiftStateAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.model.ShiftState;

/**
 * Recomputes the shift state after anything that might have changed
 * the caret's position (a key press, space, enter, delete, etc.) and
 * applies the result through a {@link SetShiftStateAction}. This is
 * the auto-capitalize implementation: if the IME reports a non-zero
 * caps mode (meaning Android thinks the next character should be
 * capitalized — start of sentence, etc.) and the user has auto-cap
 * enabled, snap the state to UPPERCASE; otherwise fall back to
 * LOWERCASE. CAPS_LOCKED is left alone so the user's explicit lock
 * isn't overwritten.
 * <p>
 * Only fires on alphabet keyboards (keyboard code {@code >= 0}); on
 * symbol layouts there's nothing to update.
 */
public class UpdateShiftAction implements Action<Void> {
    /**
     * Runs the auto-capitalize logic described in the class doc.
     * <p>
     * How: bails early if the current keyboard code is negative
     * (a non-alphabet layout where shift doesn't case letters). Then
     * switches on the current shift state — if it's LOWERCASE or
     * UPPERCASE, overwrites it via a {@link SetShiftStateAction},
     * picking UPPERCASE when auto-capitalize is enabled and the IME
     * reports a non-zero caps mode, LOWERCASE otherwise. CAPS_LOCKED
     * falls through untouched.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        if (model.getKeyboardCode() >= 0) {
            switch (model.getShiftState()) {
                case LOWERCASE:
                case UPPERCASE:
                    if (Settings.autoCapitalize && ime.getCurrentCapsMode() != 0)
                        control.fire(new SetShiftStateAction(ShiftState.UPPERCASE));
                    else
                        control.fire(new SetShiftStateAction(ShiftState.LOWERCASE));
                    break;
                case CAPS_LOCKED:
                    break;
            }
        }
        return null;
    }
}
