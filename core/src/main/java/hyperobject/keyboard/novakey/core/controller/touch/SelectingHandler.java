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

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.RenameSelectionAction;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.ToggleCursorModeAction;

/**
 * {@link TouchHandler} for the text-selection gesture: while active,
 * rotating the finger clockwise extends the selection (or moves the
 * cursor) one character to the right, counter-clockwise moves it left,
 * and crossing into the inner circle toggles between cursor-move and
 * selection-extend modes.
 * <p>
 * Activated when the typing handler detects the user started rotating
 * around the wheel (see {@link TypingHandler#getRotatingStatus}); stays
 * active until the user lifts, at which point it swaps the overlay back
 * to the keyboard.
 */
public class SelectingHandler extends RotatingHandler {

    private final Action mRight, mLeft;


    /** Caches the two directional actions so they aren't re-allocated per rotate tick. */
    public SelectingHandler() {
        mRight = new RenameSelectionAction(true);
        mLeft = new RenameSelectionAction(false);
    }


    /**
     * Resets the cursor mode to 0 at the start of each scroll gesture so
     * a stale ±1 from a prior gesture doesn't silently turn this one
     * into a selection-extend — a fresh scroll always begins in "move
     * caret, preserve selection length" mode.
     */
    @Override
    protected boolean onDown(float x, float y, int area, Controller controller) {
        controller.getModel().setCursorMode(0);
        return true;
    }


    /**
     * Fires {@link ToggleCursorModeAction} on entry into the inner
     * circle so the user flips between "move cursor" and "extend
     * selection" modes by swinging through the center. Ignores exits.
     */
    @Override
    protected boolean onCenterCross(boolean entered,
                                    Controller controller) {
        if (entered)
            controller.fire(new ToggleCursorModeAction());
        return true;
    }


    /** No-op — selection scrubbing is driven entirely by {@link #onRotate}. */
    @Override
    protected boolean onMove(float x, float y, Controller controller) {
        //Do nothing
        return true;
    }


    /**
     * Drives the selection: one tick per sector crossing. Clockwise fires
     * the "right" rename-selection action, counter-clockwise fires the
     * "left" one. Skipped entirely when the finger is in the center,
     * since inner-circle rotation doesn't represent a selection change.
     */
    @Override
    protected boolean onRotate(boolean clockwise, boolean inCenter,
                               Controller controller) {
        if (!inCenter)
            controller.fire(clockwise ? mRight : mLeft);
        return true;
    }


    /**
     * Releases the handler on finger-up and swaps the overlay back to
     * the current keyboard so the user sees keys again.
     */
    @Override
    protected boolean onUp(Controller controller) {
        controller.fire(new SetOverlayAction(controller.getModel().getKeyboard()));
        return false;
    }
}
