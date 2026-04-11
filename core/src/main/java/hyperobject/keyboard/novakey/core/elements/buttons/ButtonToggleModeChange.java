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

package hyperobject.keyboard.novakey.core.elements.buttons;

import android.graphics.Canvas;

import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetKeyboardAction;
import hyperobject.keyboard.novakey.core.actions.ToggleKeyboardAction;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.TextDrawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * The mode-switch button: cycles between the alpha keyboard and the
 * punctuation/symbols keyboards. Icon flips based on current mode — it
 * reads "#!" when the alpha keyboard is active (tap to go to punctuation)
 * and "AZ" when a non-alpha keyboard is active (tap to go back to alpha).
 * Long-press jumps straight to the symbols keyboard regardless of mode.
 */
public class ButtonToggleModeChange extends Button {

    /** Delegates layout to the base class; icon is chosen per-frame in {@link #draw}. */
    public ButtonToggleModeChange(ButtonData data) {
        super(data);
    }


    /**
     * Picks the icon based on the currently active keyboard, then lets
     * the base class do the actual drawing. Icon is recreated every frame
     * — cheap because {@code TextDrawable} is a thin wrapper around a
     * short string, and it keeps the button in sync with keyboard changes
     * without needing an observer.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        if (model.getKeyboardCode() == Keyboards.PUNCTUATION ||
                model.getKeyboardCode() == Keyboards.SYMBOLS)
            setIcon(new TextDrawable("AZ"));//TODO: other languages
        else
            setIcon(new TextDrawable("#!"));
        super.draw(model, theme, canvas);
    }


    /** Tap fires {@link ToggleKeyboardAction}, which flips alpha ↔ punctuation. */
    @Override
    protected Action onClickAction() {
        return new ToggleKeyboardAction();
    }


    /** Long-press jumps directly to the symbols keyboard. */
    @Override
    protected Action onLongPressAction() {
        return new SetKeyboardAction(Keyboards.SYMBOLS);
    }
}
