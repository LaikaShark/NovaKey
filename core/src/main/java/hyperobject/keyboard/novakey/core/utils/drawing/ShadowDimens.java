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

import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Immutable bundle describing a drop-shadow's geometry: its blur
 * radius {@code r} and its offset {@code (x, y)} in pixels. Used by
 * the drawing layer to feed {@link android.graphics.Paint#setShadowLayer}
 * with consistent values derived from a light-source angle.
 * <p>
 * The constructor is private — instances are built via
 * {@link #fromAngle(float, float)}, which projects the light-source
 * angle into an (x, y) offset proportional to the blur radius. The
 * resulting shadow is 2r offset at the given angle, producing a softer
 * drop as r increases.
 * <p>
 * Also hosts the project-wide raw-shadow constants that the 3D theme
 * mode feeds straight into {@link android.graphics.Paint#setShadowLayer}
 * without going through the angle-projection path.
 */
public class ShadowDimens {

    /** Blur radius used for the board-theme drop shadow in 3D mode. */
    public static final float BOARD_SHADOW_RADIUS = 100f;

    /** 50%-opaque black used as the shadow color in every 3D shadow. */
    public static final int SHADOW_COLOR = 0x80000000;


    /**
     * Builds a shadow with blur radius {@code r} whose offset is the
     * unit vector at {@code degrees} scaled by {@code 2r} (pixels).
     * <p>
     * How: converts degrees to radians and calls
     * {@link Util#xFromAngle} / {@link Util#yFromAngle} with a "length"
     * of {@code 2r} to compute the offset from the origin. Larger
     * blurs therefore land proportionally farther from their owner,
     * which matches how real light scatters.
     */
    public static ShadowDimens fromAngle(float degrees, float r) {
        float d = r * 2;
        double a = Math.toRadians(degrees);
        return new ShadowDimens(r, Util.xFromAngle(0, d, a), Util.yFromAngle(0, d, a));
    }


    public final float x, y, r;


    /**
     * Private constructor; callers use {@link #fromAngle(float, float)}
     * or the instance method {@link #fromAngle(float)} to build
     * instances so that the {@code (x, y)} invariant (an offset at the
     * requested angle scaled by {@code 2r}) always holds.
     */
    private ShadowDimens(float r, float x, float y) {
        this.r = r;
        this.x = x;
        this.y = y;
    }


    /**
     * Convenience overload: builds a new shadow with the same radius
     * as this one but a different light-source angle. Useful when the
     * caller already has a reference shadow and wants to rotate it.
     */
    public ShadowDimens fromAngle(float degrees) {
        return fromAngle(degrees, this.r);
    }
}
