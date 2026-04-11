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

package hyperobject.keyboard.novakey.core.view.themes.button;

import android.graphics.Canvas;

import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.shapes.Shape;
import hyperobject.keyboard.novakey.core.view.themes.ChildTheme;

/**
 * Sub-theme that styles the fixed buttons placed around the wheel
 * (space, punctuation, mode-change, etc.). Each button asks its theme
 * for a back-plate first (via {@link #drawBack}) and then for its icon
 * rendering (via {@link #drawIcon}), so this interface gets to control
 * both halves of the button look independently.
 */
public interface ButtonTheme extends ChildTheme {

    /**
     * Paints the background shape of a button — e.g. a filled circle
     * or rounded rectangle sitting behind the icon. The base variant
     * leaves this empty for a frameless button look; styled variants
     * can fill or stroke the supplied shape.
     *
     * @param shape  the button's hit-shape, used to determine the
     *               outline to draw
     * @param x      button center X
     * @param y      button center Y
     * @param size   button size in pixels
     * @param canvas canvas to paint into
     */
    void drawBack(Shape shape, float x, float y, float size, Canvas canvas);


    /**
     * Paints the button's icon (letter, glyph, or bitmap) on top of
     * the back-plate, in a color chosen to contrast against the
     * master theme.
     *
     * @param drawable icon to render
     * @param x        icon center X
     * @param y        icon center Y
     * @param size     icon size in pixels
     * @param canvas   canvas to paint into
     */
    void drawIcon(Drawable drawable, float x, float y, float size, Canvas canvas);
}
