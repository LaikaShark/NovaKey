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

import hyperobject.keyboard.novakey.core.utils.drawing.Draw;
import hyperobject.keyboard.novakey.core.utils.drawing.ShadowDimens;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Shared base class for every board-theme variant. Produces the plainest
 * wheel NovaKey can draw: no filled back (subclasses opt in via
 * {@link #drawBoardBack}), a thin inner circle, and five dividing lines
 * in the accent color. Most concrete variants extend this class and
 * override one or both of {@code drawBoardBack} / {@code drawLines} to
 * change only the pieces they care about.
 * <p>
 * Holds two reusable {@link Paint} instances: {@code pB} for the board
 * back and lines, {@code pT} for top-of-board item rendering.
 */
public class BaseTheme implements BoardTheme {

    protected final Paint pB, pT;
    protected MasterTheme mParent;


    /**
     * Initializes the shared board and text paints with anti-aliasing
     * flipped on.
     */
    public BaseTheme() {
        pB = new Paint();
        pT = new Paint();

        pB.setFlags(Paint.ANTI_ALIAS_FLAG);//smooth edges and Never changes
        pT.setFlags(Paint.ANTI_ALIAS_FLAG);
    }


    /**
     * Entry point that the main element calls every frame. Paints the
     * board back (subclasses supply the shape) followed by the divider
     * lines.
     */
    @Override
    public void drawBoard(float x, float y, float r, float sr, Canvas canvas) {
        drawBoardBack(x, y, r, sr, canvas);
        drawLines(x, y, r, sr, canvas);
    }


    /**
     * Subclass hook: paints the filled "base" of the wheel (solid disc,
     * donut, half-and-half, etc.). The base class leaves this empty so
     * a subclass can opt in only if it wants a back.
     */
    protected void drawBoardBack(float x, float y, float r, float sr, Canvas canvas) {
        //Does nothing
    }


    /**
     * Convenience overload that draws the divider lines with a default
     * stroke width of {@code 1/72} of the board radius.
     */
    private void drawLines(float x, float y, float r, float sr, Canvas canvas) {
        drawLines(x, y, r, sr, 1 / 72f, canvas);
    }


    /**
     * Paints the wheel's inner circle and the five sector divider
     * lines in the accent color. When 3D mode is on, lays down a soft
     * drop shadow first by drawing shaded lines offset downward and
     * then stamping a shadow layer on the circle before drawing it
     * properly. Subclasses override this to change the divider style.
     *
     * @param w stroke width as a fraction of {@code r}
     */
    protected void drawLines(float x, float y, float r, float sr, float w, Canvas canvas) {
        if (mParent.is3D()) {
            Draw.shadedLines(x, y + r / 72f, r, sr, 0x80000000, pB, canvas);
            pB.setShadowLayer(r / 72f / 2, 0, r / 72f, 0x80000000);
        }
        //draw lines and circle
        pB.setColor(mParent.getAccentColor());
        pB.setStyle(Paint.Style.STROKE);
        pB.setStrokeWidth(r * w);
        //draw circles & lines
        canvas.drawCircle(x, y, sr, pB);
        pB.clearShadowLayer();

        Draw.shadedLines(x, y, r, sr, mParent.getAccentColor(), pB, canvas);
    }


    /**
     * Paints a foreground item (letter, icon) in the parent theme's
     * contrast color, optionally with a drop shadow when 3D is on. The
     * actual rasterization is delegated to {@link Drawable#draw}, which
     * handles whether the drawable is a glyph, path, or bitmap.
     */
    @Override
    public void drawItem(Drawable drawable, float x, float y, float size, Canvas canvas) {
        if (mParent.is3D())
            pT.setShadowLayer(ShadowDimens.BOARD_SHADOW_RADIUS, 0,
                    ShadowDimens.BOARD_SHADOW_RADIUS, ShadowDimens.SHADOW_COLOR);
        pT.setStyle(Paint.Style.FILL);
        pT.setColor(mParent.getContrastColor());
        drawable.draw(x, y, size, pT, canvas);
        pT.clearShadowLayer();
    }


    /** Stores the back-reference to the master theme. */
    @Override
    public void setParent(MasterTheme masterTheme) {
        mParent = masterTheme;
    }


    /**
     * Renders a thumbnail of this board variant for the theme picker.
     * <p>
     * How: fits the wheel inside {@code dimen}, swaps the parent
     * theme's colors to a neutral gray-on-light palette so the preview
     * is legible regardless of the current live theme, then draws the
     * back and divider lines via the same helpers used at runtime. If
     * {@code selected} is true, stamps a blue outline around the
     * preview.
     */
    @Override
    public void drawPickerItem(float x, float y, float dimen, boolean selected,
                               int index, Paint p, Canvas canvas) {
        float r = dimen / 2 * .8f;
        float sr = (dimen / 2 * .8f) / 3;
        mParent.setColors(0xFFF0F0F0, 0xFF616161, 0xFF616161);
        drawBoardBack(x, y, r, sr, canvas);
        drawLines(x, y, r, sr, 1 / 30f, canvas);

        if (selected) {
            p.clearShadowLayer();
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFF58ACFA);
            p.setStrokeWidth(dimen * .1f);
            canvas.drawCircle(x, y, r, p);
            p.setStrokeWidth(0);
            p.setStyle(Paint.Style.FILL);
        }
    }
}
