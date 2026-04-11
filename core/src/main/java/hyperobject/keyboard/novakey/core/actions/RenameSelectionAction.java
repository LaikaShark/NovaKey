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

/**
 * Nudges the active text selection by one character in one direction,
 * respecting the current cursor mode. Used by the cursor overlay so the
 * user can fine-tune a selection after establishing it: the mode decides
 * whether the start edge, end edge, or both move.
 * <p>
 * Does not touch the model — all state change is delegated to
 * {@link NovaKeyService#moveSelection(int, int)}, which the IME applies
 * via the input connection.
 */
public class RenameSelectionAction implements Action<Void> {

    private boolean mMoveRight;


    /**
     * @param moveRight {@code true} to shift the selection one character
     *                  to the right, {@code false} to shift it left
     */
    public RenameSelectionAction(boolean moveRight) {
        mMoveRight = moveRight;
    }


    /**
     * Computes a {@code (ds, de)} start/end delta pair from the cursor
     * mode and asks the IME to apply it.
     * <p>
     * How: cursor mode {@code <= 0} means the start edge is live, so
     * move it by ±1; cursor mode {@code >= 0} means the end edge is
     * live, so move it by ±1. Mode 0 (both edges live) therefore moves
     * both edges in lockstep, preserving the selection length.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        int ds = 0, de = 0;
        if (model.getCursorMode() <= 0)
            ds = mMoveRight ? 1 : -1;
        if (model.getCursorMode() >= 0)
            de = mMoveRight ? 1 : -1;
        ime.moveSelection(ds, de);

        return null;
    }
}
