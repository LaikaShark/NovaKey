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
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.input.KeyAction;
import hyperobject.keyboard.novakey.core.controller.touch.RotatingHandler;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.TextDrawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;

/**
 * A carousel-style popup menu for picking from a long list of entries
 * (e.g. hidden keys under a long-press). The user rotates their finger
 * around the wheel to scroll the list; the current entry sits in the
 * center and neighboring entries fan out to either side at
 * exponentially shrinking sizes (1/2, 1/4, 1/8, …) to give the
 * "infinite scroll" look.
 * <p>
 * Commit happens on finger up: whichever entry is currently centered
 * fires its action, then the overlay is restored to the previous
 * keyboard.
 * <p>
 * Also hosts the static {@code HIDDEN_KEYS} registry used by
 * {@link PunctuationButton} and {@link hyperobject.keyboard.novakey.core.elements.keyboards.Key#getHiddenKeys}
 * to look up the right menu for a given parent character.
 */
public class InfiniteMenu implements OverlayElement, Menu {

    private static List<InfiniteMenu> HIDDEN_KEYS;


    private final List<Entry> mEntries;
    private final TouchHandler mHandler;
    private float fingX, fingY;
    private int mIndex = 0;//current entry


    /**
     * Wraps the given entries and installs a {@link RotatingHandler}
     * subclass that advances/rewinds {@code mIndex} as the finger rotates.
     */
    public InfiniteMenu(List<Menu.Entry> entries) {
        mEntries = entries;
        mHandler = new Handler();
    }


    /**
     * Draws the carousel for one frame.
     * <p>
     * How: uses the finger's angle relative to the wheel center to
     * compute a "distance from the sector midline" in [-1, 1]. That
     * value smoothly shifts the entire row of entries left or right
     * between rotation ticks, so visually the carousel glides instead
     * of snapping. Draws the centered entry (index {@code mIndex})
     * plus three entries on each side, with each successive side
     * entry at half the horizontal offset and half the opacity/size
     * of the previous one. Entries past the ends of the list wrap via
     * {@link #indexInBounds}.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        BoardTheme bt = theme.getBoardTheme();
        float x = d.getX();
        float y = d.getY();
        float r = d.getRadius();
        float sr = d.getSmallRadius();

        float size = sr * 1.3f;
        //get distance
        float distanceFromMiddle = 0;
        double angle = Util.getAngle(fingX - x, y - fingY);

        angle = (angle < Math.PI / 2 ? Math.PI * 2 + angle : angle);//sets angle to [90, 450]
        for (int i = 0; i < 5; i++) {
            double angle1 = (i * 2 * Math.PI) / 5 + Math.PI / 2;
            double angle2 = ((i + 1) * 2 * Math.PI) / 5 + Math.PI / 2;
            if (angle >= angle1 && angle < angle2) {
                distanceFromMiddle = (float) (Math.PI / 5 - (angle - angle1));//gets the difference
                // in angle
                distanceFromMiddle = distanceFromMiddle / (float) (Math.PI / 5);//gets the percentage
                break;
            }
        }

        //draw letters
        //center
        if (mEntries.size() > 0) {
            for (int i = 0; i < 4; i++) {
                //----------------------------------DRAW MIDDLE------------------------------------
                if (i == 0) {
                    float factor = (float) (distanceFromMiddle / Math.pow(2, i + 1));
                    if (Math.abs(distanceFromMiddle) < .25)//TODO: infinite menu pretty snap
                        draw(mIndex, x, y, size, bt, canvas);
                    else
                        draw(mIndex, x + r * factor, y + r * (factor * factor / 2),
                                size * (1 - Math.abs(factor)), bt, canvas);
                } else {
                    //----------------------------------DRAW RIGHT---------------------------------
                    float addTo = 0;
                    for (int j = 1; j < i + 1; j++) {
                        addTo += 1 / Math.pow(2, j);
                    }
                    float factor = (float) (addTo + (distanceFromMiddle < 0 ? 0 :
                            distanceFromMiddle / Math.pow(2, i + 1)));
                    draw(indexInBounds(mIndex + i), x + r * factor, y + r * (factor * factor / 2),
                            size * (1 - Math.abs(factor)), bt, canvas);
                    //----------------------------------DRAW LEFT----------------------------------
                    addTo = 0;
                    for (int j = 1; j < i + 1; j++) {
                        addTo -= 1 / Math.pow(2, j);
                    }
                    factor = (float) (addTo + (distanceFromMiddle >= 0 ? 0 :
                            distanceFromMiddle / Math.pow(2, i + 1)));
                    draw(indexInBounds(mIndex - i), x + r * factor, y + r * (factor * factor / 2),
                            size * (1 - Math.abs(factor)), bt, canvas);
                }
            }
        }
    }


    /**
     * Modulo that handles negatives: normalizes any integer into a
     * valid index for {@code mEntries} by adding the list size until
     * positive, then folding with {@code %}.
     */
    private int indexInBounds(int i) {
        while (i < 0) {//add length until positive
            i += mEntries.size();
        }
        return i % mEntries.size();
    }


    /**
     * Renders one entry at the given screen position and size. Handles
     * the three supported payload types:
     * <ul>
     *   <li>{@link Drawable}: drawn directly via the board theme.</li>
     *   <li>{@link Character} / {@link String}: wrapped in a
     *       {@link TextDrawable}. Long strings are shrunk to 1/4 size,
     *       soft-wrapped at 12 chars per line, and truncated with an
     *       ellipsis after 5 lines to keep the popup readable.</li>
     * </ul>
     */
    private void draw(int index, float x, float y, float size, BoardTheme theme, Canvas canvas) {
        Object o = mEntries.get(index).data;
        if (o == null)
            return;
        if (o instanceof Drawable) {
            theme.drawItem((Drawable) o, x, y, size, canvas);
        } else {
            String s = "";
            if (o instanceof Character)
                s = Character.toString((Character) o);
            if (o instanceof String)
                s = (String) o;
            else
                try {
                    s = s.toString();
                } catch (Exception ex) {
                }

            int MAX = 12;
            if (s.length() > 4) {
                size /= 4;
                s = Util.toMultiline(s, MAX);
                if (s.split("\n").length > 6) {
                    int last = Util.nthIndexOf(s, 5, '\n');
                    if (last < 3)//Prevents a negative length in the substring method
                        last = 3;
                    s = s.substring(0, last - 3) + "...";
                }
            }
            theme.drawItem(new TextDrawable(s), x, y, size, canvas);
        }
    }


    /** Forwards the touch to the inner rotation handler. */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }


    /**
     * Inner rotation handler: translates sector crosses into index
     * changes, tracks the finger position for the draw pass to use
     * as its interpolation input, and commits the centered entry on up.
     */
    private class Handler extends RotatingHandler {

        /** Center crosses are irrelevant to the carousel — keep the gesture alive. */
        @Override
        protected boolean onCenterCross(boolean entered, Controller controller) {
            //do nothing
            return true;
        }


        /**
         * Caches the latest finger position for the next draw pass and
         * invalidates the view so the smooth-scroll animation advances.
         */
        @Override
        protected boolean onMove(float x, float y, Controller controller) {
            fingX = x;
            fingY = y;
            controller.invalidate();
            return true;
        }


        /**
         * Advances {@code mIndex} one step — clockwise rotates to the
         * previous entry, counter-clockwise rotates to the next one —
         * and wraps around the ends of the list. Invalidates so the
         * carousel redraws on the new index (since the parent
         * {@code RotatingHandler} fires {@code onRotate} after
         * {@code onMove}, the draw pass needs a second invalidate).
         */
        @Override
        protected boolean onRotate(boolean clockwise, boolean inCenter, Controller controller) {
            if (clockwise) {
                mIndex--;
                if (mIndex < 0)
                    mIndex = mEntries.size() - 1;
            } else {
                mIndex++;
                if (mIndex >= mEntries.size())
                    mIndex = 0;
            }
            controller.invalidate();//because onRotate is after on move
            return true;
        }


        /**
         * Commits the centered entry's action, restores the previous
         * keyboard as the overlay, and resets the carousel to index 0
         * for the next time the menu opens.
         */
        @Override
        protected boolean onUp(Controller controller) {
            controller.fire(mEntries.get(mIndex).action);
            controller.fire(new SetOverlayAction(controller.getModel().getKeyboard()));
            mIndex = 0;//reset index
            return false;
        }
    }


    /**
     * Populates the static hidden-keys registry from the loaded
     * string-array resource. Each non-empty row becomes one menu whose
     * first entry is the parent key and whose remaining entries are
     * the variants that long-press exposes. The parent key has to be
     * first because {@link #getHiddenKeys(char)} uses it as the lookup
     * key.
     */
    public static void setHiddenKeys(String[] arr) {
        HIDDEN_KEYS = new ArrayList<>();
        for (String s : arr) {
            if (s.length() > 0) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < s.length(); i++) {
                    entries.add(new Entry(
                            s.charAt(i),
                            new KeyAction(s.charAt(i))));
                }
                HIDDEN_KEYS.add(new InfiniteMenu(entries));
                //TODO: make hidden keys not an array but a string array instead
            }
        }
    }


    /**
     * Looks up the hidden-keys menu for a given parent character by
     * linear-scanning the registry and matching the first entry. Returns
     * {@code null} if no menu is registered for that parent.
     */
    public static InfiniteMenu getHiddenKeys(char parent) {
        for (InfiniteMenu menu : HIDDEN_KEYS) {
            if (((Character) menu.mEntries.get(0).data).charValue() == parent) {
                return menu;
            }
        }
        return null;
    }
}
