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

package hyperobject.keyboard.novakey.core.view.themes.button;

import android.graphics.Canvas;
import android.graphics.Paint;

import hyperobject.keyboard.novakey.core.utils.drawing.Font;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.shapes.Shape;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Default implementation of {@link ButtonTheme}: frameless buttons
 * whose icons are painted straight onto whatever sits behind them.
 * Suitable for variants where the button's meaning is obvious from
 * its icon alone and a back-plate would just add visual noise.
 */
public class BaseButtonTheme implements ButtonTheme {

    private MasterTheme mParent;
    private final Paint p;


    /**
     * Builds the reusable paint with anti-aliasing on and the sans
     * serif light typeface preloaded so any text-based icon renders
     * in a consistent weight.
     */
    public BaseButtonTheme() {
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);//smooth edges and Never changes
        p.setTypeface(Font.SANS_SERIF_LIGHT);
    }


    /**
     * No-op: the base variant is frameless and draws nothing for the
     * button back-plate. Subclasses override this to paint a shape.
     */
    @Override
    public void drawBack(Shape shape, float x, float y, float size, Canvas canvas) {
        //no back for base theme
    }


    /**
     * Paints the icon in the parent theme's contrast color. The 3D
     * shadow branch is currently dead ({@code && false}) — a relic of
     * a shadow experiment the author disabled but left readable; the
     * {@code TODO: globalize shadow height} note tracks the intent.
     */
    @Override
    public void drawIcon(Drawable drawable, float x, float y, float size, Canvas canvas) {
        if (mParent.is3D() && false)
            p.setShadowLayer(50, 0, 50, 0x80000000);//TODO: globalize shadow height
        p.setStyle(Paint.Style.FILL);
        p.setColor(mParent.getContrastColor());
        drawable.draw(x, y, size, p, canvas);
        p.clearShadowLayer();
    }


    /** Stores the back-reference to the master theme. */
    @Override
    public void setParent(MasterTheme masterTheme) {
        mParent = masterTheme;
    }
}
