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
import android.graphics.Paint;

import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.Draw;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * Filled-disc board variant with prominent dividers: paints a solid
 * accent-color circle for the wheel back and then overlays full-length
 * sector radii in the primary color, making each sector read as a
 * clearly separated pie slice.
 */
public class SeparateSectionsTheme extends BaseTheme {

    /**
     * Paints the wheel back as a single filled circle in the accent
     * color. Drops a soft shadow under it when 3D mode is on.
     */
    @Override
    public void drawBoardBack(float x, float y, float r, float sr, Canvas canvas) {
        if (mParent.is3D())
            pB.setShadowLayer(r * .025f, 0, r * .025f, 0x80000000);
        //draw background flat color
        pB.setColor(mParent.getAccentColor());
        pB.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, r, pB);//main circle

        pB.clearShadowLayer();
    }


    /**
     * Paints the inner circle and full-length sector radii in the
     * primary color. The {@code -1} length argument to
     * {@link Draw#lines} tells it to draw each divider all the way from
     * the inner circle out to the rim.
     */
    @Override
    public void drawLines(float x, float y, float r, float sr, float w, Canvas canvas) {
        //draw lines and circle
        pB.setColor(mParent.getPrimaryColor());
        pB.setStyle(Paint.Style.STROKE);
        pB.setStrokeWidth(r * w);// * (9 / 5f)));
        //draw circles & lines
        canvas.drawCircle(x, y, sr, pB);
        Draw.lines(x, y, r, sr, -1, mParent.getPrimaryColor(), pB, canvas);
    }


    /**
     * Currently falls through to the base implementation. The legacy
     * commented-out code left by the original author sketches the same
     * clip-and-recolor approach as the other donut variants; see the
     * {@code TODO: multi color for donut themes} note.
     */
    @Override
    public void drawItem(Drawable drawable, float x, float y, float size, Canvas canvas) {
        //TODO: multi color for donut themes
        super.drawItem(drawable, x, y, size, canvas);
    }


    /**
     * Picks the best-contrasting color for items drawn over the outer
     * sectors (painted with the accent color).
     */
    protected int outerColor() {
        return Util.bestColor(
                mParent.getPrimaryColor(),
                mParent.getContrastColor(),
                mParent.getAccentColor());
    }


    /**
     * Picks the best-contrasting color for items drawn over the inner
     * circle (also the accent color, but usable color ranking differs
     * because the center has different decoration).
     */
    protected int centerColor() {
        return Util.bestColor(
                mParent.getContrastColor(),
                mParent.getAccentColor(),
                mParent.getPrimaryColor());
    }
}
