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
 * A mutable text {@link Drawable} with vertical centering. Used by
 * {@link hyperobject.keyboard.novakey.core.elements.keyboards.Key} to
 * render each letter on the wheel — the key caches a single instance
 * and swaps its text/font in place every frame based on shift state,
 * which is why this class exposes {@link #setText} and {@link #setFont}
 * rather than being immutable.
 * <p>
 * Unlike {@link FlatTextDrawable}, draws use {@link Draw#text} which
 * shifts the glyph box by {@code (ascent + descent) / 2} so that y
 * denotes the visual center of the glyphs, not the baseline.
 */
public class TextDrawable implements Drawable {

    private String mText;
    private Typeface mFont;


    /**
     * Builds a text drawable with an explicit typeface. The typeface
     * will be installed on the paint immediately before drawing.
     */
    public TextDrawable(String text, Typeface font) {
        mText = text;
        mFont = font;
    }


    /**
     * Builds a text drawable that inherits whatever typeface the paint
     * already has at draw time.
     */
    public TextDrawable(String text) {
        this(text, null);
    }


    /**
     * Draws the text centered at (x, y) at the given pixel text size.
     * If a typeface was set, it is applied to the paint before the
     * delegate call (and never restored — callers are expected to use
     * {@link #setFont} deliberately rather than lean on paint state).
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        if (mFont != null)
            p.setTypeface(mFont);
        Draw.text(mText, x, y, size, p, canvas);
    }


    /**
     * Replaces the typeface used for future draws.
     *
     * @param font a {@link Typeface}, or {@code null} to fall back to
     *             whatever typeface the paint carries at draw time
     */
    public void setFont(Typeface font) {
        mFont = font;
    }


    /**
     * Replaces the text rendered by this drawable.
     *
     * @param text new text; must not be null ({@link NullPointerException}
     *             is thrown to catch accidental resets)
     */
    public void setText(String text) {
        if (text == null)
            throw new NullPointerException("Text cannot be null");
        mText = text;
    }
}
