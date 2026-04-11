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

package hyperobject.keyboard.novakey.core.utils.drawing.drawables;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import hyperobject.keyboard.novakey.core.utils.drawing.Draw;

/**
 * A {@link Drawable} that paints a single glyph from an icon font such
 * as Material Icons or NovaKey's custom icon font. Each {@code FontIcon}
 * binds a human-readable {@code name} (e.g. "check", "shift") to the
 * unicode codepoint that the font maps to that icon, plus the
 * {@link Typeface} to draw with.
 * <p>
 * Populated by {@link hyperobject.keyboard.novakey.core.utils.drawing.Icons#load},
 * which parses the codepoints text files shipped alongside the font
 * assets and registers one {@code FontIcon} per line.
 */
public class FontIcon implements Drawable {

    private String name, code;
    private Typeface font;


    /**
     * Binds an icon name to a glyph string and its source typeface.
     *
     * @param name lookup key used by {@link #equals(Object)}
     * @param code the glyph string (typically one codepoint produced
     *             from the codepoints file via {@code appendCodePoint})
     * @param font the icon font to render {@code code} against
     */
    public FontIcon(String name, String code, Typeface font) {
        this.name = name;
        this.code = code;
        this.font = font;
    }


    /**
     * Draws the icon glyph centered at (x, y) sized to {@code size}.
     * <p>
     * How: saves the paint's current typeface and text size, installs
     * this icon's font + the requested size, delegates to
     * {@link Draw#text}, then restores the previous paint state so the
     * caller's paint is left untouched.
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        Typeface tempTTF = p.getTypeface();
        float tempSize = p.getTextSize();

        p.setTypeface(font);
        p.setTextSize(size);
        Draw.text(code, x, y, p, canvas);

        p.setTypeface(tempTTF);
        p.setTextSize(tempSize);
    }


    /**
     * Equality-by-name: matches against either the registered {@code name}
     * or the glyph {@code code} when compared to a {@link String}. Lets
     * {@link hyperobject.keyboard.novakey.core.utils.drawing.Icons#get(String)}
     * iterate the registry and {@code equals} each entry against the
     * requested name without a separate map. Falls back to identity
     * equality for non-String comparisons.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof String) {
            String str = (String) o;
            return name.equalsIgnoreCase(str) || code.equalsIgnoreCase(str);
        }
        return super.equals(o);
    }
}
