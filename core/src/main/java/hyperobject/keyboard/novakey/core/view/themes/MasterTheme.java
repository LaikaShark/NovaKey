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

import hyperobject.keyboard.novakey.core.view.themes.background.BackgroundTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;
import hyperobject.keyboard.novakey.core.view.themes.button.ButtonTheme;

/**
 * Top-level theme bundle handed to every drawable element. A master theme
 * owns three swappable sub-themes — {@link BackgroundTheme} for the area
 * behind the wheel, {@link BoardTheme} for the circular board itself and
 * its keys, {@link ButtonTheme} for the fixed buttons around the wheel —
 * plus three shared color slots (primary / accent / contrast) and a 3D
 * shadow flag that the children read back when they paint.
 * <p>
 * Setters return {@code this} to support fluent configuration from the
 * {@code ThemeLoader}.
 */
public interface MasterTheme {


    /**
     * Toggles 3D mode. When on, child themes apply soft drop shadows to
     * give the keyboard a raised look; when off, they draw flat.
     *
     * @param is3D true to render with shadow layers, false for flat
     * @return this master theme, for chaining
     */
    MasterTheme set3D(boolean is3D);


    /**
     * @return true if 3D mode (shadow layers) is currently enabled
     */
    boolean is3D();


    /**
     * Replaces all three color slots at once.
     *
     * @param primary  primary color (typically background / dominant area)
     * @param accent   accent color (typically board / line decoration)
     * @param contrast contrast color (typically foreground text/icons)
     * @return this master theme, for chaining
     */
    MasterTheme setColors(int primary, int accent, int contrast);


    /**
     * @return the primary color currently installed on this theme
     */
    int getPrimaryColor();


    /**
     * Overwrites just the primary color slot.
     *
     * @param color new primary color
     */
    void setPrimaryColor(int color);


    /**
     * @return the accent color currently installed on this theme
     */
    int getAccentColor();


    /**
     * Overwrites just the accent color slot.
     *
     * @param color new accent color
     */
    void setAccentColor(int color);


    /**
     * @return the contrast color currently installed on this theme
     */
    int getContrastColor();


    /**
     * Overwrites just the contrast color slot.
     *
     * @param color new contrast color
     */
    void setContrastColor(int color);


    /**
     * Installs a new board sub-theme. Implementations are expected to
     * wire {@code this} as the new child's parent so it can read colors
     * back during draw.
     *
     * @param boardTheme the new board theme
     * @return this master theme, for chaining
     */
    MasterTheme setBoardTheme(BoardTheme boardTheme);


    /**
     * @return the board sub-theme responsible for painting the wheel
     *         and its keys
     */
    BoardTheme getBoardTheme();


    /**
     * Installs a new button sub-theme. Implementations are expected to
     * wire {@code this} as the new child's parent so it can read colors
     * back during draw.
     *
     * @param buttonTheme the new button theme
     * @return this master theme, for chaining
     */
    MasterTheme setButtonTheme(ButtonTheme buttonTheme);


    /**
     * @return the button sub-theme responsible for painting the fixed
     *         buttons around the wheel
     */
    ButtonTheme getButtonTheme();


    /**
     * Installs a new background sub-theme. Implementations are expected
     * to wire {@code this} as the new child's parent so it can read
     * colors back during draw.
     *
     * @param backgroundTheme the new background theme
     * @return this master theme, for chaining
     */
    MasterTheme setBackgroundTheme(BackgroundTheme backgroundTheme);


    /**
     * @return the background sub-theme responsible for painting behind
     *         the wheel
     */
    BackgroundTheme getBackgroundTheme();
}
