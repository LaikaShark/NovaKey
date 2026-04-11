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

package hyperobject.keyboard.novakey.core.utils;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Contract for anything that can render itself as one cell inside a
 * picker widget (colour swatches, font samples, theme previews, …).
 * <p>
 * The picker passes each item a center point and a square "dimension"
 * so the item can draw itself at any size, plus a {@code selected} flag
 * so it can highlight itself when it's the current choice.
 */
public interface PickerItem {

    /**
     * Renders the picker item centered at {@code (x, y)}.
     *
     * @param x        center x position in pixels
     * @param y        center y position in pixels
     * @param dimen    maximum edge length the item should fit inside
     * @param selected whether this item is the currently selected one
     * @param index    sub-index within the item (e.g. shade index in a
     *                 {@link Colors} swatch row)
     * @param p        shared paint object the caller owns and recycles
     * @param canvas   canvas to draw on
     */
    void drawPickerItem(float x, float y, float dimen, boolean selected, int index, Paint p, Canvas canvas);
}
