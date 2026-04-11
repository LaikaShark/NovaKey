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
 * Switches the active keyboard layout (alphabet, punctuation, symbols,
 * emoji, etc.) to a specific keyboard code. The code is one of the
 * constants in {@code Keyboards}; the model resolves it to the actual
 * {@code Keyboard} instance during {@link Model#setKeyboard(int)}.
 * <p>
 * User-visible effect: the next frame draws a different set of keys
 * over the wheel. Paired with {@link ToggleKeyboardAction} when the
 * direction is implicit.
 */
public class SetKeyboardAction implements Action<Void> {

    private final int mKeyboardCode;


    /**
     * @param keyboardCode one of the {@code Keyboards} constants
     *                     identifying the layout to switch to
     */
    public SetKeyboardAction(int keyboardCode) {
        mKeyboardCode = keyboardCode;
    }


    /**
     * Writes the new keyboard code into the model and invalidates the
     * view so the new layout gets drawn on the next frame.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        //TODO: Animations
        model.setKeyboard(mKeyboardCode);
        control.invalidate();
        return null;
    }
}
