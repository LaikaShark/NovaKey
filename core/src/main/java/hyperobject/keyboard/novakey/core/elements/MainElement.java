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
import android.inputmethodservice.Keyboard;
import android.view.MotionEvent;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * The root element of the NovaKey wheel: draws the background and the
 * circular board, then delegates to a single {@link OverlayElement} for
 * the keys/menu layer on top.
 * <p>
 * Also hosts the static geometry helpers that map a raw touch coordinate
 * onto a wheel "area" — the inner-circle center (area 0), one of the five
 * outer sectors (areas 1–5), or nothing (-1). These helpers are used by
 * touch handlers and elements that need to know which sector a gesture
 * is currently over.
 */
public class MainElement implements Element {

    private OverlayElement mOverlay;


    /**
     * Builds a main element with the given initial overlay.
     *
     * @param overlay the keys/menu layer drawn on top of the board
     */
    public MainElement(OverlayElement overlay) {
        mOverlay = overlay;
    }


    /**
     * Swaps the active overlay, e.g. when entering a popup menu or
     * switching between the typing overlay and the cursor/delete overlays.
     *
     * @param overlay the new overlay to draw and route touches through
     */
    public void setOverlay(OverlayElement overlay) {
        mOverlay = overlay;
    }


    /**
     * Draws the full keyboard stack for one frame: first the background
     * (sized to the canvas, not the model, so it also fills the insets
     * edge-to-edge IMEs report on Android 15+), then the circular board,
     * then the overlay on top.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();

        // Use the canvas extents rather than the model's width/height so the
        // background fills any extra space the view reports beyond the
        // drawn keyboard — e.g. the bottom navbar inset MainView appends on
        // Android 15+ edge-to-edge IMEs.
        theme.getBackgroundTheme().drawBackground(0, 0, canvas.getWidth(), canvas.getHeight(),
                d.getX(), d.getY(), d.getRadius(), d.getSmallRadius(), canvas);

        theme.getBoardTheme().drawBoard(d.getX(), d.getY(),
                d.getRadius(), d.getSmallRadius(), canvas);

        if (mOverlay != null)
            mOverlay.draw(model, theme, canvas);
    }


    /**
     * Forwards the touch event straight to the current overlay — the
     * main element itself does not react to touches, only the overlay's
     * keys/menus do.
     *
     * @return whatever the overlay returns: {@code true} to keep the
     *         gesture alive, {@code false} to release it
     */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mOverlay.handle(event, control);
    }


    /**
     * Classifies a touch point on the wheel.
     * <p>
     * How: measures the distance from the wheel center. Inside the inner
     * radius → area 0 (center). Inside the outer radius but outside the
     * center → one of the five sectors via {@link #getSector}. Beyond the
     * outer radius → -1 (outside the wheel entirely).
     *
     * @return 0 for the center, 1–5 for a sector, or -1 if off-wheel
     */
    public static int getArea(float x, float y, Model model) {
        MainDimensions d = model.getMainDimensions();
        if (Util.distance(d.getX(), d.getY(), x, y) <= d.getSmallRadius()) //inner circle
            return 0;
        else if (Util.distance(d.getX(), d.getY(), x, y) <= d.getRadius())
            return getSector(x, y, model);
        return -1;//outside area
    }


    /**
     * Returns the sector index [1, 5] a point falls into, assuming the
     * point is already known to lie inside the wheel's outer radius.
     * Delegates to {@link #getSectorFromCenter} after pulling the wheel
     * center out of the model.
     */
    public static int getSector(float x, float y, Model model) {
        MainDimensions d = model.getMainDimensions();
        return getSectorFromCenter(x, y, d.getX(), d.getY());
    }


    /**
     * Pure-geometry sector lookup.
     * <p>
     * How: translates (x,y) into a frame centered on the wheel (also
     * flipping Y so "up" is positive), computes the angle, and normalizes
     * it into [π/2, 5π/2) so sector 1 always starts at the top. Then
     * walks the five equal wedges of 2π/5 radians each, returning i+1
     * for the first wedge that contains the angle. Returns
     * {@link Keyboard#KEYCODE_CANCEL} as a sentinel if the angle somehow
     * falls outside every wedge (should only happen on degenerate input).
     */
    private static int getSectorFromCenter(float x, float y, float centX, float centY) {
        x -= centX;
        y = centY - y;
        double angle = Util.getAngle(x, y);
        angle = (angle < Math.PI / 2 ? Math.PI * 2 + angle : angle);//sets angle to [90, 450]
        for (int i = 0; i < 5; i++) {
            double angle1 = (i * 2 * Math.PI) / 5 + Math.PI / 2;
            double angle2 = ((i + 1) * 2 * Math.PI) / 5 + Math.PI / 2;
            if (angle >= angle1 && angle < angle2)
                return i + 1;
        }
        return Keyboard.KEYCODE_CANCEL;
    }
}
