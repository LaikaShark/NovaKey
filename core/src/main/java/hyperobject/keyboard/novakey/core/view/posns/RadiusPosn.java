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
 * A polar {@link RelativePosn} measured against the wheel's outer radius.
 * <p>
 * Math: the point sits at the given angle, a distance of
 * {@code fraction * d.getRadius()} from the wheel center. A fraction of
 * {@code 1} lands exactly on the outer ring, {@code 0.5} halfway in from
 * the center, and {@code 1.2} pushes 20% beyond the outer ring.
 */
public class RadiusPosn extends RelativePosn {
    private float distance;
    private double angle;


    /**
     * Builds a polar position on (or scaled from) the outer radius.
     *
     * @param distance fraction of the outer radius (1 = on the ring)
     * @param angle    angle in radians, measured in the wheel's frame
     */
    public RadiusPosn(float distance, double angle) {
        this.distance = distance;
        this.angle = angle;
    }


    /**
     * Resolves X as
     * {@code d.getX() + cos(angle) * distance * d.getRadius()} via
     * {@link Util#xFromAngle}.
     */
    @Override
    public float getX(MainDimensions model) {
        return Util.xFromAngle(model.getX(), model.getRadius() * distance, angle);
    }


    /**
     * Resolves Y as
     * {@code d.getY() - sin(angle) * distance * d.getRadius()} via
     * {@link Util#yFromAngle} (screen-space Y grows downward, hence the
     * sign flip handled inside the helper).
     */
    @Override
    public float getY(MainDimensions model) {
        return Util.yFromAngle(model.getY(), model.getRadius() * distance, angle);
    }
}
