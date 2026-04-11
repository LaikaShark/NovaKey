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
 * Abstract {@link TouchHandler} base class that models a gesture as
 * "rotation around the wheel" — the user drags their finger around the
 * board and the handler reports clockwise/counter-clockwise sector
 * crossings. Sibling to {@link AreaCrossedHandler} but aimed at
 * continuous spinning motions rather than discrete area picks.
 * <p>
 * Tracks two independent partitions of the wheel:
 * <ul>
 *   <li><b>Area</b> — 0 for inner circle, 1–5 for outer sectors, -1 off.
 *       Used only to fire {@link #onCenterCross} when the finger enters
 *       or exits the inner circle.</li>
 *   <li><b>Sector</b> — always 1–5, even when the finger is inside the
 *       inner circle. This is what drives {@link #onRotate}: whenever the
 *       sector changes from the last frame, the handler decides whether
 *       the motion was clockwise or counter-clockwise and fires the
 *       callback.</li>
 * </ul>
 * Concrete subclasses: {@link DeleteHandler} (rotation-driven
 * delete/undo) and {@link SelectingHandler} (text-selection scrubbing).
 */
public abstract class RotatingHandler implements TouchHandler {

    private float currX, currY;
    private int currArea, prevArea;
    private int currSector = -1, prevSector = -1;


    /**
     * Feeds one raw touch event into the rotation state machine.
     * <p>
     * How: refreshes the cached finger position, area, and sector, then:
     * <ul>
     *   <li>{@code DOWN}: fires {@link #onDown} and seeds both prev-area
     *       and prev-sector.</li>
     *   <li>{@code MOVE}: fires {@link #onMove}; if the area crossed
     *       into or out of the inner circle fires {@link #onCenterCross};
     *       if the sector changed (and we're past the initial frame, so
     *       there's actually a previous sector to compare to) computes
     *       clockwise-ness by checking whether
     *       {@code (prevSector - 1) == (currSector mod 5)} — i.e. the
     *       new sector is one step below the old modulo 5 — and fires
     *       {@link #onRotate} with that and whether the finger is
     *       currently in the center.</li>
     *   <li>{@code UP}: fires {@link #onUp}.</li>
     * </ul>
     * All hook returns are AND-combined into the final result.
     */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        currX = event.getX(0);
        currY = event.getY(0);
        currArea = MainElement.getArea(currX, currY, control.getModel());
        currSector = MainElement.getSector(currX, currY, control.getModel());

        boolean result = true;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                result = result & onDown(currX, currY, currArea, control);
                prevArea = currArea;
                prevSector = currSector;
                break;
            case MotionEvent.ACTION_MOVE:
                result = result & onMove(currX, currY, control);
                if (currArea != prevArea) {
                    if (currArea == 0)
                        result = result & onCenterCross(true, control);
                    else if (prevArea == 0)
                        result = result & onCenterCross(false, control);
                }
                prevArea = currArea;
                if (currSector != prevSector && prevSector != -1) {//if not initial handling
                    boolean clockwise = (prevSector - 1) == (currSector % 5);
                    result = result & onRotate(clockwise, currArea == 0, control);
                }
                prevSector = currSector;
                break;
            case MotionEvent.ACTION_UP:
                result = result & onUp(control);
                break;
        }
        return result;
    }


    /**
     * Called on {@code ACTION_DOWN}. Default no-op returning true.
     * Override to react to the initial finger-down; returning false
     * releases this handler immediately.
     */
    protected boolean onDown(float x, float y, int area, Controller controller) {
        return true;
    }


    /**
     * Called exactly once each time the finger crosses the inner-circle
     * boundary. Orthogonal to {@link #onMove} — both fire on the same
     * event when applicable.
     *
     * @param entered true if the finger just entered the center, false
     *                if it just left
     */
    protected abstract boolean onCenterCross(boolean entered, Controller controller);


    /**
     * Called on every {@code ACTION_MOVE} before the rotation / center
     * checks run. Default no-op returning true. Override to react to
     * continuous motion (for example, to update a cached finger position
     * that the draw pass consumes).
     */
    protected boolean onMove(float x, float y, Controller controller) {
        return true;
    }


    /**
     * Called once for every sector crossing.
     *
     * @param clockwise {@code true} if the finger rotated clockwise from
     *                  the previous sector into the current one,
     *                  {@code false} for counter-clockwise
     * @param inCenter  {@code true} if the finger is currently inside
     *                  the inner circle
     */
    protected abstract boolean onRotate(boolean clockwise, boolean inCenter, Controller controller);


    /**
     * Called on {@code ACTION_UP}. Subclasses typically fire a finalized
     * action, restore the previous overlay, and return false to release
     * the handler.
     */
    protected abstract boolean onUp(Controller controller);
}
