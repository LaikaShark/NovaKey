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

/**
 * Axis-aligned square {@link Shape}. The {@code size} parameter is the
 * edge length (not half-edge), so a square and a circle with the same
 * {@code size} share the same bounding box and can be swapped on a
 * button without rescaling its position/size data.
 */
public class Square implements Shape {

    /**
     * Hit-tests the finger point against an axis-aligned square of edge
     * {@code size} centered at {@code (x, y)}.
     * <p>
     * How: inclusive bounds check on both axes against
     * {@code [x-size/2, x+size/2]} and {@code [y-size/2, y+size/2]}.
     * Points exactly on the edge count as inside.
     */
    @Override
    public boolean isInside(float fingX, float fingY, float x, float y, float size) {
        return fingX >= x - size / 2 && fingX <= x + size / 2 &&
                fingY >= y - size / 2 && fingY <= y + size / 2;
    }


    /**
     * Paints a filled square of edge {@code size} centered at
     * {@code (x, y)} via {@link Canvas#drawRect}. Paint color/style/
     * shadow are assumed to have been configured by the caller.
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        canvas.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, p);
    }
}
