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

package hyperobject.keyboard.novakey.core.view.themes.background;

import android.graphics.Canvas;

import hyperobject.keyboard.novakey.core.view.themes.ChildTheme;

/**
 * Sub-theme responsible for painting the area behind the circular wheel —
 * effectively the flat "wallpaper" of the IME view. The main element
 * calls this before it draws the board so the wheel is composited over
 * whatever shade/gradient/pattern the background theme produces.
 */
public interface BackgroundTheme extends ChildTheme {

    /**
     * Fills the background rectangle for one frame. The rectangle
     * {@code (l, t) -> (rt, b)} is the full canvas extent, while the
     * {@code (x, y, r, sr)} quartet locates the wheel inside it so
     * variants that want to draw concentric rings or masks know where
     * the board sits.
     *
     * @param l      left edge of the rect to fill, in canvas pixels
     * @param t      top edge of the rect to fill
     * @param rt     right edge of the rect to fill
     * @param b      bottom edge of the rect to fill
     * @param x      wheel center X
     * @param y      wheel center Y
     * @param r      wheel outer radius
     * @param sr     wheel inner radius
     * @param canvas canvas to paint into
     */
    void drawBackground(float l, float t, float rt, float b, float x, float y,
                        float r, float sr, Canvas canvas);
}
