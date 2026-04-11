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
import hyperobject.keyboard.novakey.core.actions.input.SpaceAction;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;

/**
 * The optional arc-shaped space bar that sits along the bottom of the
 * wheel. Tap-only: fires a {@link SpaceAction}, which handles
 * auto-correct commit and auto-capitalization downstream. Long-press is
 * intentionally disabled so a dragged gesture on the wheel never gets
 * hijacked.
 */
public class SpaceButton extends Button {

    /** Installs the pre-rendered {@code space_bar} icon from the shared icon registry. */
    public SpaceButton(ButtonData data) {
        super(data);
        setIcon(Icons.get("space_bar"));
    }


    /** Tap inserts a space via {@link SpaceAction}. */
    @Override
    protected Action onClickAction() {
        return new SpaceAction();
    }


    /** No long-press behavior — returning {@code null} makes it a no-op. */
    @Override
    protected Action onLongPressAction() {
        return null;
    }
}
