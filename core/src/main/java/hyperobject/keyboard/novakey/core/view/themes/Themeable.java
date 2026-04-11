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
 * Marker interface for any UI element whose appearance is controlled by
 * a {@link MasterTheme}. Implementers accept a theme via {@link #setTheme}
 * and consult it (directly or through sub-themes) during their draw pass
 * rather than hard-coding colors/fonts.
 */
public interface Themeable {

    /**
     * Installs the master theme this element should draw itself with.
     * The implementation is expected to retain the reference — subsequent
     * draw calls will pull colors and child themes from it.
     *
     * @param theme the master theme this element should use
     */
    void setTheme(MasterTheme theme);
}
