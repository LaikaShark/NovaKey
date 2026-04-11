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

package hyperobject.keyboard.novakey.core.elements;

import android.graphics.Canvas;

import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Anything drawable on the keyboard view that can also claim touch gestures.
 * <p>
 * Elements are the leaves of the render/input tree: {@code NovaKeyView.onDraw}
 * walks the model's element list and calls {@link #draw} on each, and the
 * {@code Controller} routes {@code MotionEvent}s through them via the
 * inherited {@link TouchHandler#handle} method.
 * <p>
 * Elements never mutate the {@code Model} directly — any state change must
 * be expressed by firing an {@code Action} through the {@code Controller}.
 */
public interface Element extends TouchHandler {

    /**
     * Renders this element onto {@code canvas} using the current {@code model}
     * state and {@code theme} styling.
     * <p>
     * How: implementations read positioning/state from {@code model}, pull
     * colors/fonts/shapes from {@code theme}, and issue the corresponding
     * {@code Canvas} draw calls. Must only be invoked from inside a
     * {@code View.onDraw} pass so the canvas is valid.
     *
     * @param model  current keyboard model (dimensions, shift state, etc.)
     * @param theme  master theme supplying colors, fonts, and sub-themes
     * @param canvas the live canvas provided by the view's draw pass
     */
    void draw(Model model, MasterTheme theme, Canvas canvas);

}
