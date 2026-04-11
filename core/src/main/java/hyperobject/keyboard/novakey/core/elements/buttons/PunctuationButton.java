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

import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.input.KeyAction;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.InfiniteMenu;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.FlatTextDrawable;

/**
 * The period button. Tap inserts a literal {@code '.'}; long-press opens
 * an {@link InfiniteMenu} populated with the hidden punctuation marks
 * associated with {@code '.'} (comma, question mark, etc.) so the user
 * can pick one without switching keyboards.
 */
public class PunctuationButton extends Button {

    /** Wires up the fixed "." icon; the period never changes per state. */
    public PunctuationButton(ButtonData data) {
        super(data);
        setIcon(new FlatTextDrawable("."));
    }


    /** Tap inserts the period character via a {@link KeyAction}. */
    @Override
    protected Action onClickAction() {
        return new KeyAction('.');
    }


    /**
     * Long-press swaps the active overlay for an {@link InfiniteMenu}
     * of punctuation marks hidden under {@code '.'}. The user picks one
     * by dragging and lifting; the menu then restores the previous overlay.
     */
    @Override
    protected Action onLongPressAction() {
        return new SetOverlayAction(InfiniteMenu.getHiddenKeys('.'));
    }
}
