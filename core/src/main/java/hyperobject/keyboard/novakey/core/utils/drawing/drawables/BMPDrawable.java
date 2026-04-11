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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import hyperobject.keyboard.novakey.core.utils.drawing.Draw;

/**
 * A {@link Drawable} backed by a decoded {@link Bitmap} resource. Used for
 * the cursor icons in {@link hyperobject.keyboard.novakey.core.utils.drawing.Icons}
 * and any other raster art that doesn't fit the font-icon pattern.
 */
public class BMPDrawable implements Drawable {

    Bitmap bmp;


    /**
     * Decodes the drawable resource {@code id} via {@link BitmapFactory}
     * and stores the bitmap for subsequent draws.
     *
     * @param res resources to decode through
     * @param id  drawable resource id
     */
    public BMPDrawable(Resources res, int id) {
        bmp = BitmapFactory.decodeResource(res, id);
    }


    /**
     * Draws the bitmap centered at (x, y), scaled so its width equals
     * {@code size}. Delegates to {@link Draw#bitmap} which computes the
     * scale factor as {@code size / bmp.getWidth()} and preserves the
     * bitmap's aspect ratio.
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        Draw.bitmap(bmp, x, y, size / bmp.getWidth(), p, canvas);
    }
}
