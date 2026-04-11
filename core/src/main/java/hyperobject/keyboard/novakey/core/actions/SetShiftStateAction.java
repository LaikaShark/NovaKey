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

package hyperobject.keyboard.novakey.core.actions;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.ShiftState;

/**
 * Forces the model's {@link ShiftState} to an exact value
 * (LOWERCASE / UPPERCASE / CAPS_LOCKED), bypassing the usual "cycle"
 * stepping. Used by {@link ShiftAction} and {@link UpdateShiftAction}
 * after they've decided what the next state should be, and by
 * auto-capitalize logic that snaps the state back to UPPERCASE at the
 * start of a sentence.
 * <p>
 * User-visible effect: the key labels are redrawn in the new case on
 * the next frame (keys fetch their drawable via
 * {@code getDrawable(shiftState)}).
 */
public class SetShiftStateAction implements Action<Void> {

    private final ShiftState mSetTo;


    /**
     * @param setTo the exact {@link ShiftState} to assign to the model
     */
    public SetShiftStateAction(ShiftState setTo) {
        this.mSetTo = setTo;
    }


    /**
     * Writes the new shift state into the model and invalidates the
     * view so the redraw picks up the updated key labels.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        //TODO: ANIMATIONS
        model.setShiftState(mSetTo);
        control.invalidate();
        return null;
    }
}
