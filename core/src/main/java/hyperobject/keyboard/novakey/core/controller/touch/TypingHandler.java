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

import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.RenameSelectionAction;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.VibrateAction;
import hyperobject.keyboard.novakey.core.actions.input.DeleteAction;
import hyperobject.keyboard.novakey.core.elements.keyboards.Key;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboard;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.CursorOverlay;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.DeleteOverlay;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.InfiniteMenu;
import hyperobject.keyboard.novakey.core.model.Settings;

/**
 * The default {@link TouchHandler} used by every {@link Keyboard}: it
 * collects the list of wheel areas the finger has passed through and,
 * on lift, hands the list to {@link Keyboard#getKey(List)} to decode
 * it into either a key press or a gesture shortcut (space / delete /
 * enter / shift).
 * <p>
 * A typing gesture is much more than "the area the finger ended up in":
 * the sequence of areas matters, because NovaKey encodes one of five
 * characters per sector based on where you came from and where you
 * left (see {@link Keyboard#getLoc(List) Keyboard.getLoc}'s algorithm
 * doc). This handler's job is to feed that decoder a clean
 * area-crossing list and to recognize the special gestures that
 * <em>aren't</em> normal key picks:
 * <ul>
 *   <li><b>Long-press pop-up menu</b> — if the user dwells on a valid
 *       partial gesture for {@link Settings#longPressTime} ms, show the
 *       hidden-keys {@link InfiniteMenu} for whatever key they're
 *       hovering over.</li>
 *   <li><b>Repeat mode</b> — if the area list settles into an A-B-A
 *       pattern, enter "repeat" mode: every subsequent crossing between
 *       A and B fires the same key again and increments the repeat
 *       counter (used for key-repeat visuals / accelerating input).</li>
 *   <li><b>Delete mode</b> — if the crossing list matches a
 *       {@link DeleteAction} gesture, fire one delete and swap the
 *       overlay to a {@link DeleteOverlay}, releasing this handler so
 *       the controller falls through to the overlay's
 *       {@link DeleteHandler}.</li>
 *   <li><b>Cursor/selection mode</b> — if {@link #getRotatingStatus}
 *       detects three sectors in a row (clockwise or counter-clockwise,
 *       i.e. the user started rotating), fire one selection step and
 *       swap in the {@link CursorOverlay}, releasing to its
 *       {@link SelectingHandler}.</li>
 * </ul>
 * Vibrates on every real area crossing (respecting
 * {@link Settings#vibrateLevel}). Each real crossing also restarts the
 * long-press timer — but only while the area list is still short
 * enough to represent a valid pending key pick (3 or fewer sectors).
 */
public class TypingHandler extends AreaCrossedHandler {

    private final List<Integer> mAreas;
    private final Keyboard mKeyboard;

    private CountDownTimer mTimer;

    private Key mRepeatingKey;
    public boolean mRepeating = false;
    private int mArea1, mArea2;


    /**
     * Binds this handler to the keyboard whose keys it will decode into.
     */
    public TypingHandler(Keyboard keyboard) {
        mKeyboard = keyboard;
        mAreas = new ArrayList<>();
    }


    /**
     * Arms the long-press timer. On finish, re-decodes the current
     * area list into a key and, if it resolves to a {@link Key} with
     * hidden alternates, swaps the overlay to the hidden-keys
     * {@link InfiniteMenu}. Uses {@link Settings#longPressTime} as both
     * interval and total duration so there's exactly one {@code onFinish}
     * tick.
     */
    private void start(Controller controller) {
        mTimer = new CountDownTimer(Settings.longPressTime, Settings.longPressTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }


            @Override
            public void onFinish() {
                //TODO: fix getting of hidden keys...make shift state aware
                Action a = mKeyboard.getKey(mAreas);
                if (a instanceof Key) {
                    InfiniteMenu newMenu = ((Key) a).getHiddenKeys(
                            controller.getModel().getShiftState());
                    if (newMenu != null)
                        controller.fire(new SetOverlayAction(newMenu));
                }
            }
        };
        if (true)//TODO: make this flag a in tutorial and longpress is disabled
            mTimer.start();
    }


    /** Cancels the long-press timer if one is running. */
    private void cancel() {
        if (mTimer != null)
            mTimer.cancel();
    }


    /**
     * Clears any state left over from the previous gesture: empties the
     * area list, drops repeat mode, seeds the list with the starting
     * area (unless the finger came down off-wheel, indicated by area
     * -1), and arms the long-press timer.
     */
    @Override
    protected boolean onDown(float x, float y, int area,
                             Controller controller) {
        mAreas.clear();
        mRepeating = false;
        if (area != -1)
            mAreas.add(area);
        start(controller);
        return true;
    }


    /**
     * Core of the typing state machine — fires once per area crossing.
     * <p>
     * How, step by step:
     * <ol>
     *   <li>Drop no-op crossings: if the new area matches the last one
     *       we recorded, or if it's -1 (off-wheel), ignore it and stay
     *       alive.</li>
     *   <li>Vibrate for haptic feedback and cancel any in-flight
     *       long-press timer — the gesture is now moving again.</li>
     *   <li>Append the new area and, if we're still in the "pending
     *       key pick" window (≤3 areas and not yet in repeat mode),
     *       re-arm the long-press timer so the user can dwell on the
     *       <em>current</em> partial gesture to pop up hidden keys.</li>
     *   <li>If we're already in repeat mode, a crossing back into one
     *       of the two recorded endpoints fires the cached repeating
     *       key again and bumps the repeat counter.</li>
     *   <li>Otherwise, once we have three areas, look for state
     *       transitions:
     *       <ul>
     *         <li><b>Repeat entry:</b> if the third area equals the
     *             first (A-B-A), stash the pair as the repeating
     *             endpoints, look up the key for the A-B pick, set
     *             {@code mRepeating}, reset the repeat counter, and
     *             fire the key twice (once for the initial A-B pick,
     *             once for the B-A loop back) with the counter
     *             incrementing between each.</li>
     *         <li><b>Delete gesture:</b> if the list now matches a
     *             delete swipe (see {@link Keyboard#getGesture}), fire
     *             one delete, swap in the {@link DeleteOverlay}, and
     *             release (return false) so the overlay's
     *             {@link DeleteHandler} picks up subsequent events.</li>
     *         <li><b>Selection rotation:</b> if
     *             {@link #getRotatingStatus} spots a rotating pattern,
     *             fire one {@link RenameSelectionAction} in the correct
     *             direction, swap in the {@link CursorOverlay}, and
     *             release.</li>
     *       </ul>
     *   </li>
     * </ol>
     */
    @Override
    protected boolean onCross(CrossEvent event, Controller controller) {
        if (mAreas.size() > 0 && event.newArea == mAreas.get(mAreas.size() - 1) ||
                event.newArea == -1)
            return true;
        controller.fire(new VibrateAction(Settings.vibrateLevel));
        cancel();
        mAreas.add(event.newArea);
        //no need to start long press for non-keys
        if (mAreas.size() <= 3 && !mRepeating)
            start(controller);

        if (mRepeating && (event.newArea == mArea1 || event.newArea == mArea2)) {
            controller.fire(mRepeatingKey);
            controller.getModel().getInputState().incrementRepeat();
        } else if (mAreas.size() >= 3 && !mRepeating) {
            if (mAreas.size() == 3 &&
                    mAreas.get(0) == mAreas.get(2)) {
                //switch to repeat handler
                mArea1 = mAreas.get(0);
                mArea2 = mAreas.get(1);

                List<Integer> areas = new ArrayList<>(mAreas);
                areas.remove(areas.size() - 1);

                Action a = mKeyboard.getKey(Arrays.asList(mArea1, mArea2));
                if (a instanceof Key) {
                    mRepeatingKey = (Key) a;
                    mRepeating = true;
                    cancel();
                    controller.getModel().getInputState().resetRepeat();
                    controller.fire(mRepeatingKey);
                    controller.getModel().getInputState().incrementRepeat();
                    controller.fire(mRepeatingKey);
                    controller.getModel().getInputState().incrementRepeat();
                }
                return true;
            }
            if (Keyboard.getGesture(mAreas) instanceof DeleteAction) {
                //switch to delete handler
                // First rotating event must be fired initially
                controller.fire(new DeleteAction());
                controller.fire(new SetOverlayAction(new DeleteOverlay()));
                cancel();
                return false;
            }
            int rotatingStatus = getRotatingStatus(mAreas);
            if (rotatingStatus != 0) {
                // First rotating event must be fired initially
                controller.fire(new RenameSelectionAction(rotatingStatus == -1));
                controller.fire(new SetOverlayAction(new CursorOverlay()));
                cancel();
                return false;
            }
        }
        return true;
    }


    /**
     * Finalizes the gesture: cancels the long-press timer, and — unless
     * we were in repeat mode (where each crossing already fired the key
     * and the user is just lifting) — hands the accumulated area list
     * to {@link Keyboard#getKey(List)} and fires whatever it decodes
     * (either a key press or a shortcut gesture). Clears state and
     * releases the handler.
     */
    @Override
    protected boolean onUp(Controller controller) {
        cancel();
        if (!mRepeating) {
            controller.fire(mKeyboard.getKey(mAreas));
        }
        mAreas.clear();
        mRepeating = false;
        return false;
    }


    /**
     * Scans the area list for an A-?-A pattern — any area whose duplicate
     * sits exactly two positions later, signifying the user swiped back
     * and forth across two sectors.
     *
     * @return the index of the first such area, or -1 if none found
     */
    private int repeatingIndex() {
        for (int i = 0; i < mAreas.size() - 2; i++) {
            if (mAreas.get(i) == mAreas.get(i + 2))
                return i;
        }
        return -1;
    }


    /**
     * Classifies an area list as "rotating" or not.
     * <p>
     * How: skips an initial off-wheel (-1) entry if present so the same
     * pattern is detected regardless of whether the finger came down on
     * or off the wheel. Requires exactly three meaningful sectors in a
     * row — no center (0), no entry in sector 3 as the middle (3 is the
     * diameter used by shortcut gestures and would cause false
     * positives), and each adjacent pair must differ by exactly 1
     * sector modulo 5. Sectors are numbered starting at the top of the
     * wheel and increasing counter-clockwise, so an ascending triple
     * ({@code 1 → 2 → 3}) is a counter-clockwise swipe and a descending
     * triple is clockwise.
     *
     * @param areasCrossed the area-crossing list to classify
     * @return 0 if not rotating, {@code 1} for counter-clockwise,
     *         {@code -1} for clockwise
     */
    private int getRotatingStatus(List<Integer> areasCrossed) {
        int checkIdx = 0;
        if (areasCrossed.get(0) == -1)
            checkIdx = 1;

        //if  either the user began from the inside,
        // or the outside
        if ((checkIdx == 0 && areasCrossed.size() == 3) ||
                (checkIdx == 1 && areasCrossed.size() == 4)) {
            int one = areasCrossed.get(checkIdx + 0);
            int two = areasCrossed.get(checkIdx + 1);
            int three = areasCrossed.get(checkIdx + 2);
            boolean hasZero = one == 0 || two == 0 || three == 0;
            if (two != 3 && !hasZero) {
                if ((one + 1) % 5 == two % 5 && (two + 1) % 5 == three % 5)
                    return 1;
                if ((three + 1) % 5 == two % 5 && (two + 1) % 5 == one % 5)
                    return -1;
            }
        }
        return 0;
    }
}
