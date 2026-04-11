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

package hyperobject.keyboard.novakey.core.elements.buttons;

import hyperobject.keyboard.novakey.core.utils.drawing.shapes.Shape;
import hyperobject.keyboard.novakey.core.view.posns.RelativePosn;

/**
 * Mutable layout descriptor for a {@link Button}: its relative position
 * on the wheel, its size, and the {@link Shape} used for both drawing
 * and hit testing. Exposes chainable setters so {@code ElementsLoader}
 * can build instances fluently, e.g.
 * {@code new ButtonData().setPosn(p).setSize(s).setShape(sh)}.
 */
public class ButtonData {

    private RelativePosn mPosn;
    private float mSize;
    private Shape mShape;


    /** Returns the relative position used for drawing and hit testing. */
    public RelativePosn getPosn() {
        return mPosn;
    }


    /** Sets the relative position; returns {@code this} for chaining. */
    public ButtonData setPosn(RelativePosn posn) {
        mPosn = posn;
        return this;
    }


    /** Returns the button size (units match whatever the shape expects). */
    public float getSize() {
        return mSize;
    }


    /** Sets the button size; returns {@code this} for chaining. */
    public ButtonData setSize(float size) {
        mSize = size;
        return this;
    }


    /** Sets the shape used for drawing and hit testing; returns {@code this} for chaining. */
    public ButtonData setShape(Shape shape) {
        mShape = shape;
        return this;
    }


    /** Returns the shape used for drawing and hit testing. */
    public Shape getShape() {
        return mShape;
    }


}
