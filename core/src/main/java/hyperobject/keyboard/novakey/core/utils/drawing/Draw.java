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

package hyperobject.keyboard.novakey.core.utils.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * Low-level canvas helpers shared across the drawing layer. Every
 * method is a pure static function that takes the {@link Paint} and
 * {@link Canvas} to draw onto, so {@code Draw} holds no state and can
 * be called from anywhere inside an {@code onDraw} pass.
 * <p>
 * Covers three families of primitives:
 * <ul>
 *   <li>Radial lines fanning out between the inner and outer radii of
 *       the wheel — {@link #lines}/{@link #line} for flat lines and
 *       {@link #shadedLines}/{@link #shadedLine} for the gradient
 *       variants that fade at both ends.</li>
 *   <li>Text helpers — {@link #text} (vertically centered, multiline
 *       aware) and {@link #textFlat} (baseline-aligned), each with an
 *       overload that temporarily sets the paint's text size.</li>
 *   <li>Misc widgets — {@link #colorItem} (swatch circle with optional
 *       checkmark), {@link #floatingButton} (material floating action
 *       button), and {@link #bitmap} (centered scaled bitmap draw).</li>
 * </ul>
 */
public class Draw {

    /**
     * Draws five flat lines fanning out from the wheel center, one per
     * sector boundary.
     * <p>
     * How: walks sectors 0..4, computes each boundary angle as
     * {@code i * 2π/5 + π/2} (so the first boundary sits at the top),
     * normalizes any overflow past 2π, and delegates to {@link #line}
     * with a start/end radius inset from {@code sr}/{@code r} by
     * {@code gap} to keep the line from touching the center circle or
     * the outer rim.
     */
    public static void lines(float x, float y, float r, float sr, float gap, int color,
                             Paint p, Canvas canvas) {
        for (int i = 0; i < 5; i++) {
            double angle = (i * 2 * Math.PI) / 5 + Math.PI / 2;
            angle = (angle > Math.PI * 2 ? angle - Math.PI * 2 : angle);
            line(x, y, sr + gap, r - gap, angle, color, p, canvas);
        }
    }


    /**
     * Draws a single straight line originating from the wheel center.
     * <p>
     * How: the line runs from radius {@code start} to radius {@code end}
     * at {@code angle} (standard math convention: +x right, +y up, so
     * sine is subtracted from y). Sets the paint color then calls
     * {@link Canvas#drawLine}.
     */
    public static void line(float x, float y, float start, float end, double angle, int color,
                            Paint p, Canvas canvas) {
        p.setColor(color);
        canvas.drawLine(x + (float) Math.cos(angle) * start,
                y - (float) Math.sin(angle) * start,
                x + (float) Math.cos(angle) * end,
                y - (float) Math.sin(angle) * end, p);
    }


    /**
     * Draws the five sector boundaries as soft-ended gradient lines.
     * <p>
     * How: derives an internal {@code gap = (r - sr) / 10} to keep the
     * lines from butting against the rims, then for each of the five
     * boundaries computes the angle and delegates to
     * {@link #shadedLine}. Identical structure to {@link #lines} — only
     * the inner call differs.
     */
    public static void shadedLines(float x, float y, float r, float sr,
                                   int color, Paint p, Canvas canvas) {
        float gap = (r - sr) / 10;
        for (int i = 0; i < 5; i++) {
            double angle = (i * 2 * Math.PI) / 5 + Math.PI / 2;
            angle = (angle > Math.PI * 2 ? angle - Math.PI * 2 : angle);
            shadedLine(x, y, sr + gap, r - gap, angle, color, p, canvas);
        }
    }


    /**
     * Draws a radial line with a gradient that fades to transparent at
     * both ends, producing a soft-tipped stroke.
     * <p>
     * How: builds a {@link RadialGradient} centered on the midpoint of
     * the line with a radius equal to half the line length; the
     * gradient runs from {@code color} at the center to
     * {@code color & 0x00FFFFFF} (fully transparent but same RGB) at
     * the edge. Installs it as the paint's shader, draws the line
     * exactly the same way as {@link #line}, then clears the shader so
     * subsequent paint users aren't polluted.
     */
    public static void shadedLine(float x, float y, float start, float end, double angle,
                                  int color, Paint p, Canvas canvas) {
        p.setShader(new RadialGradient(
                x + (float) Math.cos(angle) * ((end - start) / 2 + start),
                y - (float) Math.sin(angle) * ((end - start) / 2 + start),
                (end - start) / 2,
                color, color & 0x00FFFFFF, Shader.TileMode.CLAMP));
        canvas.drawLine(x + (float) Math.cos(angle) * start,
                y - (float) Math.sin(angle) * start,
                x + (float) Math.cos(angle) * end,
                y - (float) Math.sin(angle) * end, p);
        p.setShader(null);
    }


    /**
     * Draws baseline-aligned text centered horizontally at {@code x},
     * at a temporarily-overridden pixel text size.
     * <p>
     * How: stashes the paint's current text size, sets {@code size},
     * calls the no-size overload, and restores the previous size.
     */
    public static void textFlat(String s, float x, float y, float size, Paint p, Canvas canvas) {
        float temp = p.getTextSize();
        p.setTextSize(size);
        textFlat(s, x, y, p, canvas);
        p.setTextSize(temp);
    }


    /**
     * Draws baseline-aligned text centered horizontally at {@code x},
     * using the paint's current text size. {@code y} is the text
     * baseline, not the visual center — use {@link #text} if you want
     * vertical centering.
     */
    public static void textFlat(String s, float x, float y, Paint p, Canvas canvas) {
        canvas.drawText(s, x - p.measureText(s) / 2, y, p);
    }


    /**
     * Draws text visually centered at (x, y), splitting on newlines for
     * multi-line strings.
     * <p>
     * How: single-line strings are shifted by
     * {@code -(ascent+descent)/2} so y lands on the middle of the glyph
     * box (since ascent is negative, this moves the baseline downward).
     * Multi-line strings derive a line height of {@code textSize*10/8},
     * then recursively draw each line at a y offset relative to the
     * group's vertical center, with an extra half-line nudge for
     * even-count lines.
     *
     * <p>TODO: draw text containing emoji (the single-line path uses
     * Paint.drawText which only handles a single font fallback chain).
     */
    public static void text(String s, float x, float y, Paint p, Canvas canvas) {
        String[] S = s.split("\n");
        if (S.length <= 1) {//TODO: draw text containting emoji
            canvas.drawText(s, x - p.measureText(s) / 2, y - (p.ascent() + p.descent()) / 2, p);
        } else {
            float l = p.getTextSize() * (10 / 8);
            for (int i = 0; i < S.length; i++) {
                text(S[i], x, ((y - (S.length / 2 * l)) + i * l) - (S.length % 2 != 0 ? l / 2 : 0) + l / 2,
                        p, canvas);
            }
        }
    }


    /**
     * Draws visually centered text at a temporarily-overridden pixel
     * text size; like {@link #textFlat(String, float, float, float, Paint, Canvas)}
     * but routed through the centering variant.
     */
    public static void text(String s, float x, float y, float size, Paint p, Canvas canvas) {
        float temp = p.getTextSize();
        p.setTextSize(size);
        text(s, x, y, p, canvas);
        p.setTextSize(temp);
    }


    /**
     * Draws a solid-color swatch circle of the given radius at (x, y).
     * Used by the color picker and anywhere else that needs a simple
     * filled dot.
     */
    public static void colorItem(int color, float x, float y, float radius, Paint p, Canvas canvas) {
        p.setColor(color);
        canvas.drawCircle(x, y, radius, p);
    }


    /**
     * Same as {@link #colorItem(int, float, float, float, Paint, Canvas)}
     * plus an optional "selected" checkmark stamped on top.
     * <p>
     * How: draws the swatch, then if {@code selected} is true fetches
     * the "check" icon and picks its stroke color by contrast ratio —
     * if white-on-{@code color} contrast is below 1.1 (too low), the
     * checkmark draws in black, otherwise white — so the checkmark is
     * visible against both light and dark swatches. Shadow layer is
     * explicitly cleared before drawing the icon so the check doesn't
     * pick up a drop shadow from the caller's paint.
     */
    public static void colorItem(int color, float x, float y, float radius, boolean selected,
                                 Paint p, Canvas canvas) {
        colorItem(color, x, y, radius, p, canvas);
        if (selected) {
            float rw = Util.contrastRatio(Color.WHITE, color);
            Drawable ic = Icons.get("check");
            p.setColor(rw < 1.1f ? Color.BLACK : Color.WHITE);
            p.clearShadowLayer();
            ic.draw(x, y, radius * 1.6f, p, canvas);
        }
    }


    /**
     * Draws a material-style floating action button: a shadowed circle
     * lifted by {@code height} pixels with a tinted bitmap icon on top.
     * <p>
     * How: configures a drop shadow via {@code setShadowLayer} that
     * blurs by {@code height + 2} and offsets downward by {@code height},
     * draws the filled back circle raised by {@code -height} (so the
     * combined shadow lands where the caller expected the button), then
     * clears the shadow layer. The icon is tinted to {@code front} by
     * installing a {@link LightingColorFilter} and delegating to
     * {@link #bitmap}, which centers and scales at 1×, then the filter
     * is cleared.
     */
    public static void floatingButton(float x, float y, float radius, Bitmap icon, int back, int front,
                                      float height, Paint p, Canvas canvas) {
        p.setShadowLayer(height + 2, 0, height, 0x60000000);
        p.setColor(back);
        canvas.drawCircle(x, y - height, radius, p);
        p.clearShadowLayer();

        p.setColorFilter(new LightingColorFilter(front, 0));
        bitmap(icon, x, y - height, 1, p, canvas);
        p.setColorFilter(null);
    }


    /**
     * Draws a bitmap centered at (x, y) scaled by {@code scale}.
     * <p>
     * How: computes the destination rect from the bitmap's intrinsic
     * dimensions times the scale, temporarily installs a
     * {@link LightingColorFilter} using the paint's current color as
     * the multiplier (so mask-style PNGs pick up the paint color), calls
     * {@link Canvas#drawBitmap} with that rect, then clears the filter.
     */
    public static void bitmap(Bitmap bmp, float x, float y, float scale, Paint p, Canvas canvas) {
        float width = bmp.getWidth() * scale;
        float height = bmp.getHeight() * scale;

        p.setColorFilter(new LightingColorFilter(p.getColor(), 0));
        canvas.drawBitmap(bmp, null,
                new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2), p);
        p.setColorFilter(null);
    }
}
