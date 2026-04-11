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
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * Donut variant whose five sectors are each filled with a different
 * shade of the accent color. No divider lines are drawn — the color
 * boundaries between sectors do the separating visually. Colors can
 * either be auto-generated from the accent color via {@link #setColors()}
 * or supplied explicitly via {@link #setColors(int[])} for themes that
 * want a hand-picked palette.
 */
public class MulticolorDonutTheme extends BaseTheme {

    private int[] colors;
    private boolean autoColor = true;


    /**
     * Regenerates the five sector colors by walking a shade gradient
     * around the accent color (+1, +2, +3, +4, +5). If any step hits
     * pure white (the shade helper's clamp) the walk is restarted in
     * the darker direction so the palette stays visible against the
     * background. No-op if an explicit palette was previously supplied
     * via {@link #setColors(int[])}.
     */
    public void setColors() {
        if (autoColor) {
            //automatic colors here
            colors = new int[5];
            int addTo = 1;
            for (int i = 0; i < colors.length; i++) {
                int test = Util.colorShade(mParent.getAccentColor(), addTo + i);
                if (test == Color.WHITE) {
                    addTo = colors.length * -1;
                    test = Util.colorShade(mParent.getAccentColor(), addTo + i);
                }
                colors[i] = test;
            }
        }
    }


    /**
     * Installs a hand-picked five-color palette and disables
     * auto-generation so {@link #setColors()} becomes a no-op for this
     * instance.
     *
     * @param colors five-element palette, one per sector
     */
    public void setColors(int[] colors) {
        this.colors = colors;
        autoColor = false;
    }


    /**
     * Paints the five colored donut sectors.
     * <p>
     * How: regenerates the palette first (auto mode only), drops a
     * shadow ring under the wheel when 3D mode is on, then builds a
     * filled annular wedge path per sector using two {@link Path#arcTo}
     * segments — one along the outer circle and one back along the
     * inner circle — and fills it with the sector's color. Sector 0
     * starts at 90 degrees (straight up) so the top sector aligns with
     * key group 1.
     */
    @Override
    public void drawBoardBack(float x, float y, float r, float sr, Canvas canvas) {
        setColors();

        if (mParent.is3D()) {
            pB.setShadowLayer(r * .025f, 0, r * .025f, 0x80000000);
            pB.setStrokeWidth(r - sr);
            pB.setStyle(Paint.Style.STROKE);
            pB.setColor(0);
            canvas.drawCircle(x, y, sr + (r - sr) / 2, pB);
            //reset
            pB.setStrokeWidth(0);
            pB.clearShadowLayer();
        }

        pB.setStyle(Paint.Style.FILL);

        Path path = new Path();
        for (int i = 0; i < 5; i++) {
            pB.setColor(colors[i]);
            double angle = Math.PI / 2 + (i * 2 * Math.PI) / 5;
            angle = (angle > Math.PI * 2 ? angle - Math.PI * 2 : angle);
            angle = -angle;

            path.arcTo(new RectF(x - r, y - r, x + r, y + r),
                    (float) Math.toDegrees(angle), -360 / 5);
            path.arcTo(new RectF(x - sr, y - sr, x + sr, y + sr),
                    (float) Math.toDegrees(angle) - 360 / 5, 360 / 5);
            path.close();

            canvas.drawPath(path, pB);
            path.reset();
        }
    }


    /**
     * Intentionally empty: the color boundaries between sectors are
     * the dividers, so no explicit line drawing is needed.
     */
    @Override
    public void drawLines(float x, float y, float r, float sr, float w, Canvas canvas) {
        //do nothing
    }


    /**
     * Currently falls through to the base implementation. The legacy
     * commented-out code left by the original author sketches a future
     * path-clipped two-color render so items over the center hole can
     * use a different color than items over the ring; see the
     * {@code TODO: multi color for donut themes} note.
     */
    @Override
    public void drawItem(Drawable drawable, float x, float y, float size, Canvas canvas) {
        //TODO: multi color for donut themes
        super.drawItem(drawable, x, y, size, canvas);
    }


    /**
     * Picks the best-contrasting color for items drawn over the outer
     * ring (any of the five sector shades).
     */
    protected int outerColor() {
        return Util.bestColor(
                mParent.getPrimaryColor(),
                mParent.getContrastColor(),
                mParent.getAccentColor());
    }


    /**
     * Picks the best-contrasting color for items drawn over the center
     * hole.
     */
    protected int centerColor() {
        return Util.bestColor(
                mParent.getContrastColor(),
                mParent.getAccentColor(),
                mParent.getPrimaryColor());
    }
}
