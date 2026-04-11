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
 * Single-method strategy interface for everything that mutates state
 * in response to a user gesture. In the Element/Action/TouchHandler
 * triad, Actions are the <em>only</em> sanctioned way for Elements and
 * TouchHandlers to change the Model or talk to the {@link NovaKeyService}
 * — they never poke the model directly.
 * <p>
 * Actions are fired through {@link Controller#fire(Action)}, which hands
 * them the live IME service, controller, and model references and then
 * invalidates the view so any state change is repainted next frame.
 * Concrete implementations live alongside this interface (shift/delete/
 * enter/space/key/input under {@code .input}, plus mode-change and
 * clipboard actions at this package level).
 *
 * @param <T> the value the action returns — most actions return
 *            {@link Void}, but some (e.g. {@code DeleteAction},
 *            {@code ClipboardAction}) return the text they acted on
 */
public interface Action<T> {

    /**
     * Runs the action against the live IME context.
     *
     * @param ime     the running {@link NovaKeyService} — used for
     *                input-connection access, text commits, vibrate, etc.
     * @param control the controller, for firing follow-up actions and
     *                invalidating the view
     * @param model   the mutable keyboard model
     * @return an action-specific result, or {@code null} for actions
     *         parameterized as {@code Action<Void>}
     */
    T trigger(NovaKeyService ime, Controller control, Model model);
}
