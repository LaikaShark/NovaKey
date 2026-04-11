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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.ShadowDimens;

/**
 * App-icon-style board variant: the wheel is split vertically in half
 * with the two halves filled in slightly different shades of the primary
 * color (so the wheel reads as a two-tone circular app icon) and the
 * divider lines are drawn as thick lozenge-shaped pills instead of plain
 * strokes. Uses {@link ShadowDimens} to offset the drop shadows on each
 * divider so the shadow direction rotates around the wheel.
 */
public class IconTheme extends BaseTheme {

    /**
     * Fills the two semicircular halves of the wheel with shaded
     * variants of the primary color.
     * <p>
     * How: shades the primary by +4 (lighter) for the right half and
     * uses the plain primary for the left. If the shade helper clamped
     * to white (i.e. the primary was already near-white) it flips
     * direction, shading by -4 for the right half instead and swapping
     * the halves so the darker side is on the left. Both halves are
     * drawn as {@link Path#addArc} calls over the wheel's bounding
     * rectangle.
     */
    @Override
    public void drawBoardBack(float x, float y, float r, float sr, Canvas canvas) {
        pB.setStyle(Paint.Style.FILL);

        RectF rect = new RectF(x - r, y - r, x + r, y + r);
        Path p = new Path();

        boolean leftIsDark = false;
        int c = Util.colorShade(mParent.getPrimaryColor(), 4);
        if (c == Color.WHITE) {
            leftIsDark = true;
            c = Util.colorShade(mParent.getPrimaryColor(), -4);
        }

        p.addArc(rect, 90, 180 * (leftIsDark ? -1 : 1));
        p.close();
        pB.setColor(mParent.getPrimaryColor());
        canvas.drawPath(p, pB);

        p.reset();
        p.addArc(rect, 90, 180 * (leftIsDark ? 1 : -1));
        p.close();
        pB.setColor(c);
        canvas.drawPath(p, pB);
    }


    /**
     * Paints thick rounded-end divider "pills" and the inner and outer
     * circles of the wheel in the accent color.
     * <p>
     * How: builds one oval path stretched between the inner and outer
     * radius at the 12 o'clock position, then walks around the wheel
     * rotating the canvas by {@code 360/5} degrees per step and
     * stamping the path four more times. When 3D mode is on, the drop
     * shadow is recomputed per tick via
     * {@link ShadowDimens#fromAngle(float, float)} so each pill appears
     * lit from the same virtual light source regardless of its angle.
     */
    @Override
    public void drawLines(float x, float y, float r, float sr, float w, Canvas canvas) {
        float sw = r * w * 4;
        ShadowDimens sd = ShadowDimens.fromAngle(270, sw / 4);

        if (mParent.is3D()) {
            pB.setShadowLayer(sd.r, sd.x, sd.y, 0x80000000);
        }
        //draw lines and circle
        pB.setColor(mParent.getAccentColor());
        pB.setStyle(Paint.Style.STROKE);
        pB.setStrokeWidth(sw);
        //draw circles
        canvas.drawCircle(x, y, sr, pB);
        canvas.drawCircle(x, y, r, pB);

        //draw lines
        Path p = new Path();
        RectF rect = new RectF(x - sw / 4, y - r + (r - sr) / 6f,
                x + sw / 4, y - sr - (r - sr) / 6f);
        p.addOval(rect, Path.Direction.CW);

        pB.setStyle(Paint.Style.FILL_AND_STROKE);
        pB.setStrokeWidth(sw / 2);
        pB.setStrokeCap(Paint.Cap.ROUND);
        try {
            canvas.save();
            canvas.drawPath(p, pB);
            for (int i = 1; i <= 4; i++) {
                if (mParent.is3D()) {
                    sd = sd.fromAngle(270 + (360 / 5 * i));
                    pB.setShadowLayer(sd.r, sd.x, sd.y, 0x80000000);
                }
                canvas.rotate(360 / 5, x, y);
                canvas.drawPath(p, pB);
            }
            canvas.restore();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        pB.clearShadowLayer();
    }
}
