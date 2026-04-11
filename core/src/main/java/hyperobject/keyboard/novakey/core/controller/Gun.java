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

package hyperobject.keyboard.novakey.core.controller;

import hyperobject.keyboard.novakey.core.actions.Action;

/**
 * Minimal interface for anything that can fire an {@link Action}. In practice
 * the {@link Controller} is the only implementation — this interface exists
 * so that elements and touch handlers can be handed something that lets them
 * mutate model state without seeing the full Controller API (or depending on
 * it directly in tests).
 * <p>
 * {@code Gun.fire} is the single choke point for state changes: elements and
 * handlers never mutate the model themselves, they build an Action and hand
 * it to whatever Gun they were given.
 */
public interface Gun {

    /**
     * Invokes the action and returns its typed result.
     *
     * @param action action to execute (may be null, in which case
     *               implementations are expected to no-op)
     * @param <T>    action return type
     * @return whatever the action produced, or {@code null} if the action
     *         was null or returned null
     */
    <T> T fire(Action<T> action);
}
