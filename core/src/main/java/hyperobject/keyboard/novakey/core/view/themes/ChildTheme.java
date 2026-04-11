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

package hyperobject.keyboard.novakey.core.view.themes;

/**
 * Base interface implemented by the three sub-themes (background, board,
 * button) owned by a {@link MasterTheme}. A child theme keeps a reference
 * to its parent so it can pull shared colors (primary / accent / contrast)
 * and the 3D flag out of the master when drawing.
 */
public interface ChildTheme {

    /**
     * Stores a back-reference to the master that owns this child theme.
     * Called by {@link BaseMasterTheme#setBoardTheme} and its siblings
     * immediately after assignment so the child can later read colors
     * from its parent.
     *
     * @param masterTheme the master theme this child belongs to
     */
    void setParent(MasterTheme masterTheme);
}
