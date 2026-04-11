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

/**
 * Strategy object for an in-progress gesture. TouchHandlers are the
 * third leg of NovaKey's Element/Action/TouchHandler triad: while an
 * {@link hyperobject.keyboard.novakey.core.elements.Element Element}
 * draws something and handles the initial routing, a TouchHandler
 * owns the gesture <em>state machine</em> once the gesture has been
 * claimed.
 * <p>
 * The {@link Controller} keeps at most one "active" handler at a time
 * (the last one whose {@code handle} returned true). When
 * {@code handle} returns false the Controller releases it and falls
 * back to element-walking routing for the next event — typically this
 * happens on {@code ACTION_UP}.
 * <p>
 * Concrete handlers in this package: {@link TypingHandler} (the
 * default key-picker), {@link SelectingHandler} (text-selection mode),
 * {@link RotatingHandler} (abstract base for any gesture that reacts
 * to sector rotation around the wheel), {@link DeleteHandler}
 * (rotation-driven delete/undo), and {@link AreaCrossedHandler}
 * (abstract base that dispatches into down/move/cross/up callbacks).
 */
public interface TouchHandler {

    /**
     * Feeds one raw {@link MotionEvent} into the gesture state machine.
     *
     * @param event   the touch event as delivered to the view
     * @param control the controller, used both as context (getModel,
     *                invalidate) and as the {@link hyperobject.keyboard.novakey.core.controller.Gun
     *                Gun} the handler fires actions through
     * @return {@code true} to stay active (the Controller will route
     *         the next event here too), {@code false} to release this
     *         handler and resume element-walking routing
     */
    boolean handle(MotionEvent event, Controller control);
}
