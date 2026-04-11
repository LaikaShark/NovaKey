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
 * A polar {@link RelativePosn} that interpolates between the inner and
 * outer radii.
 * <p>
 * Math: the effective radial distance is
 * {@code smallRadius + fraction * (radius - smallRadius)}, so a fraction
 * of {@code 0} lands on the inner ring, {@code 1} lands on the outer
 * ring, and {@code 0.5} lands on the midline. Values outside [0, 1]
 * extrapolate linearly — {@code 2} lies at {@code 2 * (outer - inner)}
 * past the inner ring, and negative values push inside it. This is the
 * position used for keys on the outer sectors, which are all described
 * in terms of "how far between the two rings."
 */
public class RadiiPosn extends RelativePosn {

    private float distance;
    private double angle;


    /**
     * Builds an inner/outer-radius-interpolated polar position.
     *
     * @param distance fraction between rings: 0 = inner, 1 = outer
     * @param angle    angle in radians, measured in the wheel's frame
     */
    public RadiiPosn(float distance, double angle) {
        this.distance = distance;
        this.angle = angle;
    }


    /**
     * Resolves X by linearly interpolating the radial distance between
     * {@code d.getSmallRadius()} and {@code d.getRadius()} by
     * {@code distance}, then projecting to cartesian at {@code angle}.
     */
    @Override
    public float getX(MainDimensions model) {
        float dist = model.getSmallRadius() +
                ((model.getRadius() - model.getSmallRadius()) * distance);
        return Util.xFromAngle(model.getX(), dist, angle);
    }


    /**
     * Resolves Y by the same radial interpolation, projected via
     * {@link Util#yFromAngle} (which flips the sign so screen-space Y
     * grows downward).
     */
    @Override
    public float getY(MainDimensions model) {
        float dist = model.getSmallRadius() +
                (model.getRadius() - model.getSmallRadius()) * distance;
        return Util.yFromAngle(model.getY(), dist, angle);
    }
}
