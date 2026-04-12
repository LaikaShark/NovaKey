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

package hyperobject.keyboard.novakey.core.utils.drawing.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Circular {@link Shape}. The {@code size} parameter is interpreted as
 * the full diameter so that a circle and a square with the same
 * {@code size} occupy the same bounding box — this lets callers swap
 * shapes on a button without rescaling.
 */
public class Circle implements Shape {

    /**
     * Hit-tests the finger point against a disk of radius {@code size/2}
     * centered at {@code (x, y)}.
     * <p>
     * How: computes the euclidean distance between the finger point and
     * the center via {@link Util#distance} and returns {@code true} if
     * it is less than or equal to the radius. Note this is inclusive
     * on the boundary, matching the visual edge of the drawn circle.
     */
    @Override
    public boolean isInside(float fingX, float fingY, float x, float y, float size) {
        return Util.distance(fingX, fingY, x, y) <= size / 2;
    }


    /**
     * Paints a filled circle of radius {@code size/2} centered at
     * {@code (x, y)} using the supplied paint. The paint's color,
     * style, and shadow layer are assumed to have been configured by
     * the caller (typically the button theme).
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        canvas.drawCircle(x, y, size / 2, p);
    }
}
