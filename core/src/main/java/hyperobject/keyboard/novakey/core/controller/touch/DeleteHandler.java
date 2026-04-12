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


import java.util.Stack;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.input.DeleteAction;
import hyperobject.keyboard.novakey.core.actions.input.InputAction;

/**
 * Rotation-driven delete/undo handler. Activated when the typing
 * handler detects a swipe-left delete gesture; after that the user can
 * keep rotating their finger around the wheel to delete more characters
 * or back off to re-insert what they just deleted.
 * <p>
 * State machine: starts in "backspacing" mode. Each counter-clockwise
 * sector crossing fires a backspace and pushes the deleted string onto
 * an undo stack; each clockwise crossing pops from the stack and
 * re-inserts the string. If the user pops everything back, the handler
 * flips into "forward-delete" mode — now clockwise rotation deletes
 * characters to the <em>right</em> of the cursor and counter-clockwise
 * restores them. Popping the forward-delete stack empty flips back to
 * backspacing.
 * <p>
 * The overlay is restored to the current keyboard on finger-up so the
 * user sees keys again.
 */
public class DeleteHandler extends RotatingHandler {

    private final Action<String> mDelete, mBackspace;
    private final Stack<String> mStack;
    private boolean mBackspacing = true;//false if deleting
    private boolean mGoingFast = false;


    /**
     * Pre-allocates the slow forward-delete and backspace actions plus
     * the undo stack so the rotate tick path is allocation-free.
     */
    public DeleteHandler() {
        mDelete = new DeleteAction(true);
        mBackspace = new DeleteAction();

        mStack = new Stack<>();
    }


    /** No-op — this handler doesn't care about inner-circle transitions. */
    @Override
    protected boolean onCenterCross(boolean entered, Controller controller) {
        return true;
    }


    /** No-op — deletion is driven entirely by {@link #onRotate}. */
    @Override
    protected boolean onMove(float x, float y, Controller controller) {
        return true;
    }


    /**
     * One tick per sector crossing. Dispatches on the current mode:
     * <ul>
     *   <li><b>Backspacing</b> (default): counter-clockwise fires a
     *       backspace and pushes the deleted chunk; clockwise pops the
     *       last deleted chunk and re-inputs it. Emptying the stack
     *       flips into forward-delete mode.</li>
     *   <li><b>Forward-delete</b>: clockwise fires a forward delete
     *       (delete-to-right-of-cursor) and pushes the deleted chunk;
     *       counter-clockwise pops and re-inputs it with the
     *       "right-of-cursor" flag so the cursor position is preserved.
     *       Emptying the stack flips back into backspacing mode.</li>
     * </ul>
     * The {@code inCenter} hint is ignored — deletion fires on every
     * sector crossing regardless of whether the finger is inside the
     * inner circle.
     */
    @Override
    protected boolean onRotate(boolean clockwise, boolean inCenter, Controller controller) {
        if (mBackspacing) {
            if (!clockwise) {//backspace
                String str = controller.fire(mBackspace);
                if (str != null && str.length() > 0)
                    mStack.add(str);
            } else {//add
                if (mStack.size() >= 1)
                    controller.fire(new InputAction(mStack.pop()));
                if (mStack.size() == 0)
                    mBackspacing = false;
            }
        } else {
            if (clockwise) {//delete
                String str = controller.fire(mDelete);
                if (str != null && str.length() > 0)
                    mStack.add(str);
            } else {//add
                if (mStack.size() >= 1)
                    controller.fire(new InputAction(mStack.pop(), true));
                //inputs to the right of the cursor
                if (mStack.size() == 0)
                    mBackspacing = true;

            }
        }
        return true;
    }


    /**
     * On finger-up, swaps the overlay back to the current keyboard and
     * releases the handler.
     */
    @Override
    protected boolean onUp(Controller controller) {
        controller.fire(new SetOverlayAction(controller.getModel().getKeyboard()));
        return false;
    }
}
