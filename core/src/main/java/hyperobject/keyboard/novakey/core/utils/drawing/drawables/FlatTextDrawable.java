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

import hyperobject.keyboard.novakey.core.utils.drawing.Draw;

/**
 * A text {@link Drawable} that renders horizontally-centered text at the
 * text baseline y (no vertical centering).
 * <p>
 * Differs from {@link TextDrawable} in that it does not apply the
 * ascent/descent correction that visually centers the glyph box on y —
 * this variant positions on the font baseline, which is what you want
 * when the text sits above something (e.g. a label above an icon) and
 * the baseline itself is the reference line.
 */
public class FlatTextDrawable implements Drawable {

    private final String mText;


    /**
     * Stores the text to render. Font/typeface is taken from whatever
     * paint is passed in at draw time.
     */
    public FlatTextDrawable(String text) {
        mText = text;
    }


    /**
     * Horizontally centers the text at {@code x} and draws it on the
     * baseline at {@code y} with the given pixel text size, via
     * {@link Draw#textFlat(String, float, float, float, Paint, Canvas)}.
     * The paint's prior text size is preserved by the delegate.
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        Draw.textFlat(mText, x, y, size, p, canvas);
    }
}
