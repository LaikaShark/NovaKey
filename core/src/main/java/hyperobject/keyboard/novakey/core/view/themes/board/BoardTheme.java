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

package hyperobject.keyboard.novakey.core.view.themes.board;

import android.graphics.Canvas;

import hyperobject.keyboard.novakey.core.utils.PickerItem;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.view.themes.ChildTheme;

/**
 * Sub-theme that paints the circular wheel itself — the board back,
 * the sector divider lines, the inner circle — and the per-key
 * glyphs/icons drawn on top of it. Concrete variants (donut, material,
 * multicolor, separate sections, icon) override the two draw methods
 * to produce different visual styles.
 * <p>
 * Also extends {@link PickerItem} so a board theme can render itself as
 * a thumbnail in the theme-picker grid.
 */
public interface BoardTheme extends PickerItem, ChildTheme {

    /**
     * Paints the wheel for one frame: whatever board-back shape the
     * variant uses (filled disc, donut ring, half-and-half, …) plus
     * the sector divider lines and the small inner circle.
     *
     * @param x      wheel center X
     * @param y      wheel center Y
     * @param r      wheel outer radius
     * @param sr     wheel inner radius
     * @param canvas canvas to paint into
     */
    void drawBoard(float x, float y, float r, float sr, Canvas canvas);


    /**
     * Paints a single item (letter, icon, glyph) on top of the wheel,
     * using whatever foreground color the variant picks to stay
     * readable against its board back.
     *
     * @param drawable the foreground object to paint
     * @param x        item center X
     * @param y        item center Y
     * @param size     item size in pixels
     * @param canvas   canvas to paint into
     */
    void drawItem(Drawable drawable, float x, float y, float size, Canvas canvas);
}
