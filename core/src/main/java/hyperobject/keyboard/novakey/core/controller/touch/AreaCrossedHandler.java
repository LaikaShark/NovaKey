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

package hyperobject.keyboard.novakey.core.controller.touch;

import android.view.MotionEvent;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.elements.MainElement;

/**
 * Abstract {@link TouchHandler} base class that lifts raw
 * {@link MotionEvent}s into a friendlier down/move/cross/up callback
 * API keyed on the wheel's area grid. Subclasses don't deal with
 * {@code ACTION_*} constants or coordinate math — they override the
 * protected hooks and reason in terms of "user just crossed from area
 * A into area B".
 * <p>
 * The "area" here is the one defined by
 * {@link MainElement#getArea(float, float, hyperobject.keyboard.novakey.core.model.Model)}:
 * 0 for the inner circle, 1–5 for the five outer sectors, -1 for
 * off-wheel. On every MOVE event {@code handle} recomputes the current
 * area and, if it differs from the last one seen, fires {@link #onCross}
 * before updating the stored previous area.
 * <p>
 * Used by {@link TypingHandler} (to collect an area-crossing list for
 * key decoding) and by
 * {@link hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.OnUpMenu
 * OnUpMenu}'s inner handler.
 */
public abstract class AreaCrossedHandler implements TouchHandler {

    private float currX, currY;
    private int currArea, prevArea;


    /**
     * Feeds one raw touch event into the area-cross state machine.
     * <p>
     * How: updates the cached finger position and computes the current
     * area, then dispatches on the event's masked action:
     * <ul>
     *   <li>{@code DOWN}: fires {@link #onDown} and seeds prevArea.</li>
     *   <li>{@code MOVE}: fires {@link #onMove}; if the area changed
     *       since the last event, also fires {@link #onCross} with a
     *       {@link CrossEvent}.</li>
     *   <li>{@code UP}: fires {@link #onUp}.</li>
     * </ul>
     * The overall boolean return is AND-combined from each hook's result
     * — any hook returning false releases this handler.
     */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        currX = event.getX(0);
        currY = event.getY(0);
        currArea = MainElement.getArea(currX, currY, control.getModel());

        boolean result = true;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                result = result & onDown(currX, currY, currArea, control);
                prevArea = currArea;
                break;
            case MotionEvent.ACTION_MOVE:
                result = result & onMove(currX, currY, control);
                if (currArea != prevArea) {
                    result = result & onCross(new CrossEvent(currArea, prevArea), control);
                    prevArea = currArea;
                }
                break;
            case MotionEvent.ACTION_UP:
                result = result & onUp(control);
                break;
        }
        return result;
    }


    /**
     * Called on {@code ACTION_DOWN}. Default is a no-op returning true.
     * Override to react to the initial finger-down (for example, start
     * a long-press timer). Returning false releases this handler
     * immediately.
     */
    protected boolean onDown(float x, float y, int area, Controller controller) {
        return true;
    }


    /**
     * Called on every {@code ACTION_MOVE} before {@link #onCross} is
     * considered. Default is a no-op returning true. Override to react
     * to continuous finger motion, e.g. cache the current position for
     * the draw pass.
     */
    protected boolean onMove(float x, float y, Controller controller) {
        return true;
    }


    /**
     * Called whenever the finger's current area differs from the
     * previously recorded one — i.e. the user has crossed a sector
     * or ring boundary. The {@link CrossEvent} carries both old and new
     * area so the subclass can tell direction of travel without
     * consulting its own state.
     */
    protected abstract boolean onCross(CrossEvent event, Controller controller);


    /**
     * Called on {@code ACTION_UP}. Subclasses typically fire a
     * finalized action here (the key the user meant to type, the menu
     * entry they selected, …) and return false to release the handler.
     */
    protected abstract boolean onUp(Controller controller);

}
