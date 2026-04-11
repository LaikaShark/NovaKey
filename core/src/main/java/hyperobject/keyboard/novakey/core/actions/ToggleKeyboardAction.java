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
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Flips between the alphabet layout and the punctuation/symbols layout
 * depending on which one is currently active. Dispatches the actual
 * switch through {@link SetKeyboardAction} so layout changes always
 * take the same code path.
 * <p>
 * Rules:
 * <ul>
 *   <li>DEFAULT (alphabet) → PUNCTUATION</li>
 *   <li>PUNCTUATION or SYMBOLS → DEFAULT</li>
 *   <li>Anything else → DEFAULT (the {@code default} branch acts as a
 *       safe fallback to the alphabet)</li>
 * </ul>
 */
public class ToggleKeyboardAction implements Action<Void> {

    /**
     * Reads the current keyboard code, picks the paired code by the
     * rules in the class doc, and fires a {@link SetKeyboardAction} to
     * actually perform the switch.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        switch (model.getKeyboardCode()) {
            case Keyboards.DEFAULT:
                control.fire(new SetKeyboardAction(Keyboards.PUNCTUATION));
                break;
            default:
            case Keyboards.PUNCTUATION:
            case Keyboards.SYMBOLS:
                control.fire(new SetKeyboardAction(Keyboards.DEFAULT));
        }
        return null;
    }
}
