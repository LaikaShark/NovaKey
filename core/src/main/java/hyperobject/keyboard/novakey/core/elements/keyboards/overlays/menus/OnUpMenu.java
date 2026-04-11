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

package hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus;

import android.graphics.Canvas;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import java.util.List;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.controller.touch.AreaCrossedHandler;
import hyperobject.keyboard.novakey.core.controller.touch.CrossEvent;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.TextDrawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;

/**
 * A radial popup menu that lays its entries around the wheel in five
 * evenly-spaced slots starting from the top. Unlike {@link InfiniteMenu},
 * the user doesn't rotate a carousel — they drag their finger toward
 * the desired entry and lift. The selected entry's action fires on up;
 * holding in place over an entry for {@link Settings#longPressTime}
 * fires the entry's alternate long-press action if it's an
 * {@link OnUpMenu.Entry} with one.
 * <p>
 * Visually, the entry nearest the finger grows and the others shrink,
 * so the user always sees which slot they're about to pick.
 */
public class OnUpMenu implements OverlayElement, Menu {

    private final List<Menu.Entry> mEntries;
    private final TouchHandler mHandler;
    public float fingerX, fingerY;


    /**
     * Wraps the entries and installs the {@link AreaCrossedHandler}
     * subclass that handles selection + long-press timer.
     */
    public OnUpMenu(List<Menu.Entry> entries) {
        mEntries = entries;
        mHandler = new Handler();
    }


    /**
     * Lays out the entries on a circle halfway between the inner and
     * outer radii, starting at the top of the wheel and marching
     * counter-clockwise by 72° per entry (2π/5). Individual entry
     * draws go through {@link #draw(int, float, float, float, float, BoardTheme, Canvas)}
     * which handles the proximity-based size scaling.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        BoardTheme bt = theme.getBoardTheme();
        float x = d.getX();
        float y = d.getY();
        float r = d.getRadius();
        float sr = d.getSmallRadius();

        float dist = (r - sr) / 2 + sr;
        double a = Math.PI / 2 - Math.PI * 2 / 5 / 2;
        for (int j = 0; j < mEntries.size(); j++) {
            a += Math.PI * 2 / 5;
            draw(j, x + (float) Math.cos(a) * dist, y - (float) Math.sin(a) * dist,
                    sr, dist, bt, canvas);
        }
    }


    /**
     * Renders a single entry scaled by how close the finger is to it.
     * The {@code (dist / fingerDistance)^(1/3)} term grows the nearest
     * entry without blowing up the far ones, and the result is capped
     * at 5/6 of {@code dist} so an entry can't overrun its neighbors.
     * <p>
     * Payload handling mirrors {@link InfiniteMenu}'s: drawables draw
     * as-is, characters/strings wrap in a {@link TextDrawable}.
     */
    private void draw(int index, float x, float y, float size, float dist,
                      BoardTheme theme, Canvas canvas) {
        size *= Math.pow(dist / Util.distance(x, y, fingerX, fingerY), 1.0 / 3);
        size = size >= dist * 5 / 6 ? dist * 5 / 6 : size;

        //TODO: abstract this behavior(same in infinite menu)
        Object o = mEntries.get(index).data;
        if (o == null)
            return;
        if (o instanceof Drawable)
            theme.drawItem((Drawable) o, x, y, size, canvas);
        else {
            String s = "";
            if (o instanceof Character)
                s = Character.toString((Character) o);
            else
                try {
                    s = s.toString();
                } catch (Exception e) {
                }
            theme.drawItem(new TextDrawable(s), x, y, size, canvas);
        }
    }


    /** Forwards the touch to the inner area-crossing handler. */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }


    /**
     * Inner handler: tracks the current area, arms a long-press timer
     * on every sector cross, and commits the active entry on up.
     */
    private class Handler extends AreaCrossedHandler {

        private CountDownTimer mTimer;
        private int mArea = 0;


        /**
         * Arms a single-shot long-press timer. On finish it fires the
         * current entry's {@code longPress} action if the entry is an
         * {@link OnUpMenu.Entry} (entries without a long-press extension
         * get no-op behavior).
         */
        private void startTimer(Controller control) {
            mTimer = new CountDownTimer(Settings.longPressTime, Settings.longPressTime) {
                @Override
                public void onTick(long millisUntilFinished) {

                }


                @Override
                public void onFinish() {
                    Menu.Entry entry = mEntries.get(mArea - 1);
                    if (entry instanceof OnUpMenu.Entry)
                        control.fire(((OnUpMenu.Entry) entry).longPress);
                }
            }.start();
        }


        /** Stops the long-press timer if one is currently running. */
        private void cancelTimer() {
            if (mTimer != null)
                mTimer.cancel();
        }


        /**
         * Caches the finger position so the draw pass can size entries
         * by proximity, then invalidates so the new sizes render.
         */
        @Override
        protected boolean onMove(float x, float y, Controller controller) {
            fingerX = x;
            fingerY = y;
            controller.invalidate();
            return true;
        }


        /**
         * Records the new active area (areas are 1-indexed; subtract 1
         * to get the entry index) and resets the long-press timer so
         * the user must dwell on the <em>current</em> entry, not any
         * previous one.
         */
        @Override
        protected boolean onCross(CrossEvent event, Controller controller) {
            mTimer.cancel();
            mTimer.start();
            mArea = event.newArea;
            return true;
        }


        /**
         * Commits the currently-hovered entry's action and restores
         * the previous keyboard as the overlay. Cancels the long-press
         * timer so it doesn't fire after up.
         */
        @Override
        protected boolean onUp(Controller controller) {
            mTimer.cancel();
            controller.fire(mEntries.get(mArea - 1).action);
            controller.fire(new SetOverlayAction(controller.getModel().getKeyboard()));
            return false;
        }
    }

    /**
     * Extended entry type that adds a second action fired on long-press
     * (while the finger dwells on the entry past
     * {@link Settings#longPressTime}), alongside the tap action that
     * fires on up.
     */
    public static class Entry extends Menu.Entry {
        public final Action longPress;


        /**
         * @param data      drawable/string/character to render
         * @param action    action fired on finger up (tap commit)
         * @param longPress action fired if the finger dwells long enough
         */
        public Entry(Object data, Action action, Action longPress) {
            super(data, action);
            this.longPress = longPress;
        }
    }
}
