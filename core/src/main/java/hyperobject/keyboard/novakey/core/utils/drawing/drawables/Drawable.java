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

/**
 * NovaKey's internal drawable abstraction.
 * <p>
 * Intentionally distinct from {@link android.graphics.drawable.Drawable}:
 * the Android API expects drawables to own bounds/state and to be painted
 * via {@code draw(Canvas)}, which assumes a stateful, long-lived resource.
 * NovaKey's drawing layer is almost entirely stateless — elements walk the
 * model every frame and pass in the center point, desired size, and the
 * shared {@link Paint} at the moment of the draw — so this interface is a
 * single function pointer taking (x, y, size, paint, canvas) with no
 * per-instance geometry. Implementations include {@link BMPDrawable},
 * {@link TextDrawable}, {@link FlatTextDrawable}, and {@link FontIcon},
 * plus the shapes under {@code shapes/} which also implement this
 * interface so they can double as hit-test + paint objects.
 */
public interface Drawable {
    /**
     * Paints this drawable centered at (x, y) sized to {@code size} using
     * the supplied paint and canvas.
     *
     * @param x      center x in canvas pixels
     * @param y      center y in canvas pixels
     * @param size   nominal pixel size (meaning is implementation-specific:
     *               text height, bitmap edge length, shape diameter, etc.)
     * @param p      paint whose color/shadow/typeface state the caller has
     *               already configured; implementations may temporarily
     *               mutate it but must restore anything they change
     * @param canvas destination canvas
     */
    void draw(float x, float y, float size, Paint p, Canvas canvas);
}
