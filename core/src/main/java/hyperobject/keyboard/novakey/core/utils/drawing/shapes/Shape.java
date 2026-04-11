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

import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * A geometric primitive that can be both painted and hit-tested.
 * <p>
 * Extending {@link Drawable} means a {@code Shape} can fill both roles
 * in a button's definition: it is the back-plate that gets drawn every
 * frame, and it is the hit region that the touch handler tests against.
 * {@link hyperobject.keyboard.novakey.core.elements.buttons.Button}
 * uses exactly this pattern — it calls {@link #isInside} on DOWN to
 * decide whether to claim a gesture, and lets the theme call
 * {@link Drawable#draw} to render the back.
 * <p>
 * The shape is positioned and sized by the caller at paint/test time,
 * so implementations are stateless and can be reused across buttons.
 */
public interface Shape extends Drawable {

    /**
     * Returns whether the touch point {@code (fingX, fingY)} falls
     * inside a shape centered at {@code (x, y)} with nominal size
     * {@code size}.
     *
     * @param fingX finger x coordinate to test (canvas pixels)
     * @param fingY finger y coordinate to test (canvas pixels)
     * @param x     shape center x (canvas pixels)
     * @param y     shape center y (canvas pixels)
     * @param size  shape size (diameter for circles, edge length for
     *              squares); the exact interpretation is up to the
     *              implementation but must match what {@link Drawable#draw}
     *              uses so hit-test matches what the user sees
     * @return {@code true} if the point is inside the shape
     */
    boolean isInside(float fingX, float fingY, float x, float y, float size);
}
