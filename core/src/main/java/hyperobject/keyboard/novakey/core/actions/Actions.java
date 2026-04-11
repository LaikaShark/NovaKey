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
 * Composite {@link Action} that fires a pre-built list of sub-actions
 * in order, so an Element or TouchHandler can queue several side effects
 * from a single call to {@link Controller#fire}. Any return values from
 * the children are discarded — use this only when the composite itself
 * has no meaningful result.
 */
public class Actions implements Action<Void> {

    private final Action[] mActions;


    /**
     * Captures the list of actions to fire in sequence.
     *
     * @param actions the sub-actions, executed in argument order
     */
    public Actions(Action... actions) {
        mActions = actions;
    }


    /**
     * Fires each wrapped action through {@link Controller#fire} in the
     * original order. Because every sub-action goes through {@code fire},
     * each one triggers its own view invalidate — the composite itself
     * does not need to call invalidate.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        for (Action a : mActions) {
            control.fire(a);
        }
        return null;
    }
}
