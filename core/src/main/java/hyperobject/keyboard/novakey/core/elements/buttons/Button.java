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

import android.graphics.Canvas;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.shapes.Shape;
import hyperobject.keyboard.novakey.core.view.posns.RelativePosn;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.button.ButtonTheme;

/**
 * Base class for the fixed buttons that sit around the wheel (space,
 * punctuation, mode-change, etc.). A button has a {@link ButtonData}
 * describing its shape/position/size, an optional icon, and two behaviors:
 * a short-click action and a long-press action, both supplied by subclasses.
 * <p>
 * Touch flow: ACTION_DOWN inside the button's shape starts a long-press
 * timer and claims the gesture. ACTION_MOVE that leaves the shape cancels
 * the long-press and releases the gesture. ACTION_UP fires the click
 * action unless the long-press already fired (which clears the click flag).
 */
public abstract class Button implements Element {

    private Drawable mIcon;

    private CountDownTimer mLongPress;
    private boolean mShouldClick = true;

    private ButtonData mData;


    /**
     * Stores the layout data; subclasses are responsible for supplying
     * the icon and the click/long-press actions.
     */
    public Button(ButtonData data) {
        mData = data;
    }


    /**
     * Subclass hook to install the icon drawn in the center of the button.
     * Called by subclass constructors (or whenever state changes force an
     * icon swap, e.g. shift state icons).
     */
    protected final void setIcon(Drawable icon) {
        mIcon = icon;
    }


    /**
     * Subclass hook for a tap. Returning {@code null} makes the button a
     * no-op on click; otherwise the returned action is fired through the
     * controller in {@link #handle}.
     */
    protected abstract Action onClickAction();


    /**
     * Subclass hook for a long-press. Returning {@code null} disables
     * long-press behavior; otherwise the returned action is fired when
     * the long-press timer finishes.
     */
    protected abstract Action onLongPressAction();


    /**
     * Draws the button back plate followed by the icon (scaled to 70% of
     * the button size). Both draws go through the current {@link ButtonTheme}
     * so styling is consistent across buttons. Must only be called from
     * inside a view's {@code onDraw} pass.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        ButtonTheme buttonTheme = theme.getButtonTheme();

        Shape shape = mData.getShape();
        RelativePosn posn = mData.getPosn();
        float size = mData.getSize();

        buttonTheme.drawBack(shape, posn.getX(d), posn.getY(d), size, canvas);

        if (mIcon != null)
            buttonTheme.drawIcon(mIcon, posn.getX(d), posn.getY(d), size * .7f, canvas);
    }


    /**
     * Routes a touch event through the tap / long-press state machine.
     * <p>
     * How:
     * <ul>
     *   <li>DOWN inside the shape: start long-press timer, claim gesture.</li>
     *   <li>DOWN outside the shape: ignore, return false.</li>
     *   <li>MOVE outside: cancel long-press and drop the gesture.</li>
     *   <li>MOVE inside: keep holding the gesture.</li>
     *   <li>UP: cancel timer and fire the click action if the long-press
     *       hadn't already fired (which would have cleared {@code mShouldClick}).</li>
     * </ul>
     *
     * @return {@code true} while the button wants to keep the gesture,
     *         {@code false} once it's released
     */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        Model model = control.getModel();
        MainDimensions d = model.getMainDimensions();
        Shape shape = mData.getShape();
        RelativePosn posn = mData.getPosn();
        float size = mData.getSize();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (shape.isInside(event.getX(), event.getY(),
                        posn.getX(d), posn.getY(d), size)) {
                    startLongPress(control);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!shape.isInside(event.getX(), event.getY(),
                        posn.getX(d), posn.getY(d), size)) {
                    cancelLongPress();
                    return false;
                }
                return true;
            case MotionEvent.ACTION_UP:
                cancelLongPress();
                if (mShouldClick) {
                    Action a = onClickAction();
                    if (a != null)
                        control.fire(a);
                }
                return false;
        }
        return false;
    }


    /**
     * Arms a one-shot timer for {@link Settings#longPressTime} ms. When
     * it finishes it clears the click flag (so ACTION_UP won't also fire
     * a tap) and dispatches the long-press action. Resets {@code mShouldClick}
     * to {@code true} on every arm so a previous cancelled long-press
     * doesn't suppress the next tap.
     */
    private void startLongPress(Controller control) {
        mShouldClick = true;
        mLongPress = new CountDownTimer(Settings.longPressTime, Settings.longPressTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }


            @Override
            public void onFinish() {
                mShouldClick = false;
                Action a = onLongPressAction();
                if (a != null)
                    control.fire(a);
            }
        };
        mLongPress.start();
    }


    /**
     * Stops the long-press timer if one is currently running. Safe to
     * call when there is none.
     */
    private void cancelLongPress() {
        if (mLongPress != null)
            mLongPress.cancel();
    }

}
