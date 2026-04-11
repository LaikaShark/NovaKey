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

/**
 * Abstract positional descriptor that resolves to absolute screen
 * coordinates given a {@link MainDimensions}.
 * <p>
 * Elements store one of these instead of a literal (x, y) so their
 * geometry can be described once relative to the wheel's center/radii
 * and then recomputed correctly whenever the user resizes the keyboard
 * or switches orientation. Subtypes cover the common cases:
 * <ul>
 *     <li>{@link DeltaPosn} — absolute pixel offset from the center.</li>
 *     <li>{@link RadiusPosn} — polar (angle, fraction-of-outer-radius).</li>
 *     <li>{@link SmallRadiusPosn} — polar vs the inner radius.</li>
 *     <li>{@link RadiiPosn} — polar, interpolated between inner and outer.</li>
 *     <li>{@link DeltaRadiusPosn} — polar, outer-radius + pixel offset.</li>
 * </ul>
 * Callers typically consume this via {@code posn.getX(d)} / {@code posn.getY(d)}
 * inside a draw pass.
 */
public abstract class RelativePosn {

    /**
     * Resolves this descriptor's X coordinate against the given wheel
     * dimensions.
     *
     * @param model the current main dimensions (wheel center + radii)
     * @return absolute X in view pixels
     */
    public abstract float getX(MainDimensions model);


    /**
     * Resolves this descriptor's Y coordinate against the given wheel
     * dimensions.
     *
     * @param model the current main dimensions (wheel center + radii)
     * @return absolute Y in view pixels
     */
    public abstract float getY(MainDimensions model);
}
