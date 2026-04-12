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

import android.view.inputmethod.InputConnection;

import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.ShiftState;

/**
 * Wipes per-session state when the IME disconnects from an editor.
 * Resets shift state to {@link ShiftState#LOWERCASE}, swaps the active
 * overlay back to the current keyboard (so any in-flight popup menu /
 * cursor overlay / delete overlay does not survive into the next
 * field), releases the controller's active touch handler (so a gesture
 * mid-flight when the field disconnects does not leak into the next
 * field's first touch), and clears any composing-text region on both
 * the local {@link hyperobject.keyboard.novakey.core.model.InputState}
 * and the editor's {@link InputConnection}.
 * <p>
 * Fired from {@code NovaKeyService.onFinishInput} so the next session
 * starts clean instead of inheriting the previous field's state.
 */
public class ResetStateAction implements Action<Void> {

    /**
     * Runs the four reset steps in order. The
     * {@link InputConnection#finishComposingText} call is null-guarded
     * because {@code getCurrentInputConnection} can return null in
     * window-tear-down race conditions.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        model.setShiftState(ShiftState.LOWERCASE);
        model.setOverlayElement(model.getKeyboard());
        control.releaseTouchHandler();
        model.getInputState().clearComposingText();
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic != null) {
            ic.finishComposingText();
        }
        return null;
    }
}
