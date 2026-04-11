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
 * A {@link RelativePosn} expressed as an absolute pixel offset from the
 * wheel center.
 * <p>
 * Math: {@code x = d.getX() + dx}, {@code y = d.getY() + dy}. Used for
 * things whose position is best described in raw pixels rather than as a
 * fraction of a radius — e.g. the dead-center key at {@code (0, 0)}.
 */
public class DeltaPosn extends RelativePosn {

    private float dx, dy;


    /**
     * Builds a delta position with the given pixel offsets from the
     * wheel center.
     *
     * @param deltaX horizontal offset in pixels (positive = right)
     * @param deltaY vertical offset in pixels (positive = down)
     */
    public DeltaPosn(float deltaX, float deltaY) {
        this.dx = deltaX;
        this.dy = deltaY;
    }


    /**
     * Resolves X as {@code model.getX() + dx} — wheel center shifted by
     * the stored horizontal pixel offset.
     */
    @Override
    public float getX(MainDimensions model) {
        return model.getX() + dx;
    }


    /**
     * Resolves Y as {@code model.getY() + dy} — wheel center shifted by
     * the stored vertical pixel offset.
     */
    @Override
    public float getY(MainDimensions model) {
        return model.getY() + dy;
    }
}
