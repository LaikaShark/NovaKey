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
 * Steps the model's cursor mode through its three-value cycle. The
 * cursor mode decides which edge of the selection {@link RenameSelectionAction}
 * moves: {@code 0} = both edges (collapsed caret or full-selection
 * move), {@code -1} = start edge only, {@code 1} = end edge only.
 * <p>
 * User-visible effect: the cursor overlay's affordances switch to
 * reflect which edge is now live. Paired with the cursor-overlay
 * gesture UI.
 */
public class ToggleCursorModeAction implements Action<Void> {


    /**
     * Cycles {@code 0 → -1 → 1 → 1 …}.
     * <p>
     * How: a two-case switch on the current mode. From 0 we go to -1;
     * from -1 we go to 1; any other value (i.e. 1) falls through the
     * switch and stays at 0 — which is how the initial value ({@code res = 0})
     * ends up getting written back, effectively wrapping 1 → 0 to
     * complete the cycle.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        int res = 0;
        switch (model.getCursorMode()) {
            case 0:
                res = -1;
                break;
            case -1:
                res = 1;
        }
        //TODO: Animations
        model.setCursorMode(res);
        return null;
    }
}
