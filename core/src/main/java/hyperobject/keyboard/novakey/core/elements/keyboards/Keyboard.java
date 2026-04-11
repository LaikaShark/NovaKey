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

package hyperobject.keyboard.novakey.core.elements.keyboards;

import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.input.DeleteAction;
import hyperobject.keyboard.novakey.core.actions.input.EnterAction;
import hyperobject.keyboard.novakey.core.actions.input.ShiftAction;
import hyperobject.keyboard.novakey.core.actions.input.SpaceAction;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.controller.touch.TypingHandler;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * One concrete keyboard layout (English, Symbols, Punctuation, …). Owns
 * a 2D array of {@link Key}s addressed by {@code [group][loc]}, draws
 * them into the wheel, and decodes touch gestures into either a key
 * press or a shortcut gesture (space/delete/enter/shift).
 * <p>
 * Also doubles as its own {@link Iterator} over all keys (group-major,
 * then loc) so callers can {@code for (Key k : keyboard)} without an
 * intermediate collection. The iterator state is stored on the keyboard
 * itself, so concurrent iteration is <em>not</em> supported — fine
 * because the draw pass is single-threaded.
 */
public class Keyboard implements OverlayElement, Iterator<Key>, Iterable<Key> {

    private final TouchHandler mHandler;
    private final Key[][] keys;
    private final String name;

    private int currG = 0, currL = 0;


    /**
     * Builds the keyboard from a pre-filled {@code [group][loc]} grid
     * and installs a {@link TypingHandler} as the touch strategy.
     *
     * @param name human-readable name ("English", "Symbols", …)
     * @param keys the group/loc grid of keys
     */
    public Keyboard(String name, Key[][] keys) {
        this.keys = keys;
        this.name = name;
        mHandler = new TypingHandler(this);
    }


    /**
     * Resets the iterator cursor and returns {@code this}. Since the
     * cursor lives on the keyboard, calling {@code iterator()} again
     * mid-iteration will silently restart from the beginning.
     */
    @Override
    public Iterator<Key> iterator() {
        currG = 0;
        currL = 0;
        return this;
    }


    /** True while the cursor still points to a valid (group, loc) slot. */
    @Override
    public boolean hasNext() {
        return currG < keys.length && currL < keys[currG].length;
    }


    /**
     * Returns the next key and advances the cursor. When {@code loc}
     * exhausts the current group it wraps to the next group at loc 0.
     * Throws {@link NoSuchElementException} if called past the end.
     */
    @Override
    public Key next() {
        if (currG >= keys.length || currL >= keys[currG].length)
            throw new NoSuchElementException();
        Key k = keys[currG][currL];
        currL++;
        if (currL >= keys[currG].length) {
            currG++;
            currL = 0;
        }
        return k;
    }


    /** Removal is not supported; the backing array is fixed at construction. */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns the key at {@code [group][loc]}, folding any out-of-range
     * loc back to loc 0 so alt-layout groups (which have fewer slots)
     * degrade gracefully instead of throwing.
     */
    private Key getKey(int group, int loc) {
        if (loc > keys[group].length)//for alt layouts
            return keys[group][0];
        return keys[group][loc];
    }


    /**
     * Draws every key in the keyboard, scaled to the current board
     * radius, unless the model says letters should be hidden (user
     * preference) or the field is a password and password-letter-hiding
     * is on. Key size is scaled by {@code radius / (16/3)} so keys stay
     * proportional to the board across resolutions.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        if (!(model.getKeyboardCode() > 0 && //on an alphabet
                Settings.hideLetters || (Settings.hidePassword
                && model.getInputState().onPassword()))) {
            //CODE4: Drawing keys
            for (Key k : this) {
                theme.getBoardTheme().drawItem(
                        k.getDrawable(model.getShiftState()),
                        k.getPosn().getX(d),
                        k.getPosn().getY(d),
                        k.getSize() * (d.getRadius() / (16 / 3)), canvas);
            }
        }
    }


    /**
     * Forwards the touch event to the {@link TypingHandler} installed
     * at construction, which collects the area-crossing list and
     * eventually calls back into {@link #getKey(List)}.
     */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }


    /**
     * Decodes a full list of wheel areas crossed by a typing gesture
     * into the action to fire. Tries gesture shortcuts first (see
     * {@link #getGesture}); if none match, maps the areas onto a
     * {@code (group, loc)} address and returns the corresponding key.
     * Returns {@code null} if the list is empty or the first area was
     * off-wheel.
     */
    public Action getKey(List<Integer> areasCrossed) {
        if (areasCrossed.size() <= 0)
            return null;
        //regular areas
        //gets first and last of list
        int firstArea = areasCrossed.get(0);
        //Inside circle
        if (firstArea >= 0) {
            Action act = getGesture(areasCrossed);
            if (act != null) {
                return act;
            }
            Location l = getLoc(areasCrossed);
            if (l != null)
                return getKey(l.x, l.y);
        }
        return null;
    }


    /**
     * Maps an area-crossing list to a {@code (group, loc)} key address.
     * <p>
     * How: the first area is the gesture's starting sector. It checks
     * the last area first, then the second area, so a user who briefly
     * dipped through a neighboring sector still gets the intended key.
     * <ul>
     *   <li>First == 0 (center start): loc is the target sector.</li>
     *   <li>Check == first: loc 0 (stayed in the sector).</li>
     *   <li>Check == first+1 (with wrap): loc 1 (clockwise neighbor).</li>
     *   <li>Check == 0: loc 2 (dipped into the center).</li>
     *   <li>Check == first-1 (with wrap): loc 3 (counter-clockwise).</li>
     *   <li>Check == -1 with exactly two areas: loc 4 (went off-wheel).</li>
     * </ul>
     * Returns {@code null} if the starting area was off-wheel or no
     * rule matched.
     */
    private Location getLoc(List<Integer> areasCrossed) {
        if (areasCrossed.size() <= 0)
            return null;
        //regular areas
        //gets first and last of list
        int firstArea = areasCrossed.get(0);
        int lastArea = areasCrossed.get(areasCrossed.size() > 1 ? areasCrossed.size() - 1 : 0);
        //sets to last or first if there is only one value
        int secondArea = areasCrossed.get(areasCrossed.size() > 1 ? 1 : 0);
        //sets to second value or first if there is only one value

        //Inside circle
        if (firstArea >= 0) {
            //loops twice checks first and last area first, then checks first and second area
            int check = lastArea;
            for (int i = 0; i < 2; i++) {
                if (firstArea == 0 && check >= 0)//center
                    return new Location(0, check);
                else {
                    if (firstArea == check)
                        return new Location(firstArea, 0);
                    else if (check == firstArea + 1 || (firstArea == 5 && check == 1))
                        return new Location(firstArea, 1);
                    else if (check == 0)
                        return new Location(firstArea, 2);
                    else if (check == firstArea - 1 || (firstArea == 1 && check == 5))
                        return new Location(firstArea, 3);
                    else if (check == -1 && areasCrossed.size() == 2)
                        return new Location(firstArea, 4);
                }
                check = secondArea;
            }
        }
        return null;
    }


    /** Plain (group, loc) pair used as the return type of {@link #getLoc}. */
    public static class Location {
        final int x, y;


        Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    /**
     * Recognizes the four wheel-wide gesture shortcuts that bypass the
     * keyboard grid. All four require 3–5 areas crossed, passing through
     * either the center (area 0) or the opposite sector (area 3):
     * <ul>
     *   <li>Sector 2 → 4/5 (swipe right through center/3) = space</li>
     *   <li>Sector 4 → 2 (swipe left through center/3) = delete</li>
     *   <li>Sector 1 or 5 → 3 (swipe down through center) = enter</li>
     *   <li>Sector 3 → 1 or 5 (swipe up through center) = shift</li>
     * </ul>
     * Returns {@code null} if no gesture matches.
     */
    public static Action getGesture(List<Integer> areasCrossed) {
        int size = areasCrossed.size();
        if (size < 3 || size > 5)
            return null;

        int first = areasCrossed.get(0), last = areasCrossed.get(size - 1);
        boolean hasZero = areasCrossed.contains(0),
                hasThree = areasCrossed.contains(3);
        if (first == 2 && (hasZero || hasThree) && (last == 4 || last == 5))//swipe right
            return new SpaceAction();
        if (first == 4 && (hasZero || hasThree) && last == 2)//swipe left
            return new DeleteAction();
        if ((first == 1 || first == 5) && hasZero && last == 3)//swipe down
            return new EnterAction();
        if (first == 3 && hasZero && (last == 1 || last == 5))//swipe up
            return new ShiftAction();
        return null;
    }
}
