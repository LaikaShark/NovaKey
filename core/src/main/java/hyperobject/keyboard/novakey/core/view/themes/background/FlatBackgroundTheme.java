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
import android.graphics.Paint;

import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Default background: a solid rectangle filled with the parent master
 * theme's primary color. This is the flat, opaque "wallpaper" behind
 * the wheel — no gradient, no pattern, no concentric decoration.
 */
public class FlatBackgroundTheme implements BackgroundTheme {

    private final Paint p;
    private MasterTheme mParent;


    /**
     * Builds the reusable paint used by {@link #drawBackground} with
     * anti-aliasing enabled. Color and style are set per draw.
     */
    public FlatBackgroundTheme() {
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);//smooth edges and Never changes
    }


    /**
     * Fills the supplied rectangle with the parent theme's primary
     * color. Ignores the wheel-geometry arguments — this variant draws
     * the same flat fill regardless of where the board sits.
     */
    @Override
    public void drawBackground(float l, float t, float rt, float b,
                               float x, float y, float r, float sr, Canvas canvas) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(mParent.getPrimaryColor());
        canvas.drawRect(l, t, rt, b, p);
    }


    /** Stores the back-reference to the master theme. */
    @Override
    public void setParent(MasterTheme masterTheme) {
        mParent = masterTheme;
    }
}
