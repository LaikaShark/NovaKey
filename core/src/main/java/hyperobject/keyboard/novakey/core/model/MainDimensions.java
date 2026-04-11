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

package hyperobject.keyboard.novakey.core.model;

/**
 * Concrete {@link Dimensions} bag for the root NovaKey wheel: its center
 * {@code (x,y)}, outer/inner radii, surrounding padding, and the total
 * view width/height. Populated by {@link hyperobject.keyboard.novakey.core.model.loaders.MainDimensionsLoader}
 * from {@code SharedPreferences} on each prefs sync and mutated live by
 * the resize gesture; read every frame by {@link hyperobject.keyboard.novakey.core.elements.MainElement}
 * and friends to lay out and draw the board.
 * <p>
 * The string keys are exposed as {@code public static} so the backing
 * {@code HashMap} stays shareable with test/setup tooling, but day-to-day
 * code should use the typed getters below.
 */
public class MainDimensions extends Dimensions {

    public static String
            X = "x",
            Y = "y",
            RADIUS = "radius",
            SMALL_RADIUS = "smallRadius",
            PADDING = "padding",
            WIDTH = "width",
            HEIGHT = "height";


    /**
     * Stores every dimension in the base map up front so later
     * {@code getF}/{@code getI} calls always see a non-null value.
     * {@code smallRadius} here is the resolved pixel value, not the
     * divisor stored in prefs — see {@code MainDimensionsLoader} for
     * the conversion.
     */
    public MainDimensions(float x, float y, float radius, float smallRadius,
                          float padding, int width, int height) {
        set(X, x);
        set(Y, y);
        set(RADIUS, radius);
        set(SMALL_RADIUS, smallRadius);
        set(PADDING, padding);
        set(WIDTH, width);
        set(HEIGHT, height);
    }


    /** Wheel center X in view pixels. */
    public float getX() {
        return getF(X);
    }


    /** Updates the wheel center X (used by the live resize gesture). */
    public void setX(float x) {
        set(X, x);
    }


    /** Wheel center Y in view pixels. */
    public float getY() {
        return getF(Y);
    }


    /** Updates the wheel center Y (used by the live resize gesture). */
    public void setY(float y) {
        set(Y, y);
    }


    /** Outer radius of the wheel in pixels. */
    public float getRadius() {
        return getF(RADIUS);
    }


    /** Updates the outer radius (used by the live resize gesture). */
    public void setRadius(float radius) {
        set(RADIUS, radius);
    }


    /**
     * Inner-circle radius in pixels (the hub that maps to area 0). Stored
     * here as the already-resolved pixel value; prefs stash it as a
     * divisor of {@link #getRadius()}.
     */
    public float getSmallRadius() {
        return getF(SMALL_RADIUS);
    }


    /** Updates the inner-circle radius (used by the live resize gesture). */
    public void setSmallRadius(float smallRadius) {
        set(SMALL_RADIUS, smallRadius);
    }


    /** Padding between the wheel edge and the view bounds, in pixels. */
    public float getPadding() {
        return getF(PADDING);
    }


    /** Updates the wheel padding (used by the live resize gesture). */
    public void setPadding(float padding) {
        set(PADDING, padding);
    }


    /** Current view width in pixels — set from the display metrics on load. */
    public int getWidth() {
        return getI(WIDTH);
    }


    /** Updates the view width (used when the view is relaid out). */
    public void setWidth(int width) {
        set(WIDTH, width);
    }


    /** Current view height in pixels — influences the IME's reported height. */
    public int getHeight() {
        return getI(HEIGHT);
    }


    /** Updates the view height (used by the live resize gesture). */
    public void setHeight(int height) {
        set(HEIGHT, height);
    }
}
