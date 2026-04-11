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

package hyperobject.keyboard.novakey.core.view.posns;

import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * A hybrid polar {@link RelativePosn}: angle is given, but the radial
 * distance is {@code outerRadius + pixelDistance} rather than a fraction
 * of a radius.
 * <p>
 * Math: the point lies at
 * {@code (d.getX() + cos(angle) * (d.getRadius() + distance),
 *         d.getY() - sin(angle) * (d.getRadius() + distance))}.
 * Useful for elements that want to sit a fixed pixel gap outside the
 * wheel regardless of how the user has resized it — the offset is not
 * scaled with the radius.
 */
public class DeltaRadiusPosn extends RelativePosn {

    private final float mDistance;
    private final double mAngle;


    /**
     * Builds a polar position at angle, a fixed pixel distance beyond
     * the outer radius.
     *
     * @param distance pixel offset past the outer radius (may be
     *                 negative to pull the point inside the ring)
     * @param angle    angle in radians, measured in the wheel's frame
     */
    public DeltaRadiusPosn(float distance, double angle) {
        mDistance = distance;
        mAngle = angle;
    }


    /**
     * Resolves X at {@code (radius + distance)} pixels from the wheel
     * center along {@code angle}.
     */
    @Override
    public float getX(MainDimensions model) {
        return Util.xFromAngle(model.getX(), model.getRadius() + mDistance, mAngle);
    }


    /**
     * Resolves Y at {@code (radius + distance)} pixels from the wheel
     * center along {@code angle} (with the screen-space Y flip handled
     * inside {@link Util#yFromAngle}).
     */
    @Override
    public float getY(MainDimensions model) {
        return Util.yFromAngle(model.getY(), model.getRadius() + mDistance, mAngle);
    }
}
