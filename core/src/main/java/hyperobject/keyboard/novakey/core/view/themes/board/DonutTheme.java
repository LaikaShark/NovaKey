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
 * Ring-shaped board variant: fills only the annulus between the inner
 * and outer radius with the accent color, leaving the center visually
 * punched out. Sector divider lines are short tick marks drawn in the
 * primary color rather than full radii.
 */
public class DonutTheme extends BaseTheme {

    /**
     * Paints the donut ring. Uses a stroked circle drawn at the
     * midpoint radius {@code sr + (r - sr) / 2} with a stroke width of
     * {@code r - sr}, which gives a filled annulus with a single
     * {@code drawCircle} call. Adds a drop shadow under the ring when
     * 3D mode is on.
     */
    @Override
    public void drawBoardBack(float x, float y, float r, float sr, Canvas canvas) {
        if (mParent.is3D())
            pB.setShadowLayer(r * .025f, 0, r * .025f, 0x80000000);
        //draw background flat color
        pB.setColor(mParent.getAccentColor());
        pB.setStyle(Paint.Style.STROKE);
        float mem = pB.getStrokeWidth();
        pB.setStrokeWidth(r - sr);
        canvas.drawCircle(x, y, sr + (r - sr) / 2, pB);//main circle
        pB.setStrokeWidth(mem);
        pB.clearShadowLayer();
    }


    /**
     * Draws short divider ticks in the primary color, 1/10 of the ring
     * width long, placed along the ring's radial midline. No inner
     * circle is drawn — the donut's hole is already visible.
     */
    @Override
    public void drawLines(float x, float y, float r, float sr, float w, Canvas canvas) {
        //draw lines and circle
        pB.setColor(mParent.getPrimaryColor());
        pB.setStyle(Paint.Style.STROKE);
        pB.setStrokeWidth(r * w);
        //draw circles & lines
        Draw.lines(x, y, r, sr, (r - sr) / 10, mParent.getPrimaryColor(), pB, canvas);
    }


    /** Falls through to the base implementation. */
    @Override
    public void drawItem(Drawable drawable, float x, float y, float size, Canvas canvas) {
        super.drawItem(drawable, x, y, size, canvas);
    }


    /**
     * Picks the best-contrasting color for items drawn over the ring
     * (which is painted with the accent color).
     */
    protected int outerColor() {
        return Util.bestColor(
                mParent.getPrimaryColor(),
                mParent.getContrastColor(),
                mParent.getAccentColor());
    }


    /**
     * Picks the best-contrasting color for items drawn over the center
     * hole (which shows whatever color is behind the donut — usually
     * the primary color).
     */
    protected int centerColor() {
        return Util.bestColor(
                mParent.getContrastColor(),
                mParent.getAccentColor(),
                mParent.getPrimaryColor());
    }
}
