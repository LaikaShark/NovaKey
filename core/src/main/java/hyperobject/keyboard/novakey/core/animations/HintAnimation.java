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

package hyperobject.keyboard.novakey.core.animations;

import android.animation.TimeInterpolator;
import android.view.animation.AccelerateInterpolator;

import hyperobject.keyboard.novakey.core.animations.utils.Animator;
import hyperobject.keyboard.novakey.core.elements.keyboards.Key;
import hyperobject.keyboard.novakey.core.elements.keyboards.KeySizeAnimator;

/**
 * {@link CharAnimation} that previews a touch on one of the wheel's
 * sector groups by shrinking every non-highlighted key toward
 * {@code 0} and nudging the keys in the touched sector larger. Uses
 * an {@link AccelerateInterpolator} so the effect eases in slowly and
 * then snaps, and runs with no stagger delay (style {@code -1}) so
 * the whole keyboard reacts simultaneously. Default duration is
 * 100ms, short enough to feel like live feedback rather than an
 * animation.
 */
public class HintAnimation extends CharAnimation {

    private final TimeInterpolator mInterpolator = new AccelerateInterpolator();
    private final int mArea;


    /**
     * @param area     sector group index to highlight
     * @param duration total animation duration in milliseconds
     */
    public HintAnimation(int area, long duration) {
        super(-1, duration);
        mArea = area;
    }


    /**
     * Convenience constructor using a 100ms duration suitable for
     * live touch feedback.
     */
    public HintAnimation(int area) {
        this(area, 100);
    }


    /**
     * Every key shares the same {@link AccelerateInterpolator}.
     */
    @Override
    protected TimeInterpolator getInterpolatorFor(Key k) {
        return mInterpolator;
    }


    /**
     * Returns a size animator from the key's current size down to
     * {@code 0} for keys outside the highlighted area. Keys inside
     * the highlighted area construct a grow-to-{@code 1.2f} animator
     * but fall through to the shrink animator — note that the branch
     * creates the grow animator as an unused expression, so in its
     * present form every key ends up shrinking.
     */
    @Override
    protected Animator<Key> getAnimatorFor(Key k) {
        if (k.group == mArea) {
            new KeySizeAnimator(k.getSize(), 1.2f);
        }
        return new KeySizeAnimator(k.getSize(), 0);
    }


    /**
     * Helper that picks the "last" area index adjacent to the given
     * group/loc pair — walks forward, back, or to the center depending
     * on the {@code loc}. Currently only referenced by the commented-out
     * initial-state override that was removed during cleanup, so it is
     * effectively dead code kept for future use.
     */
    private static int lastArea(int group, int loc) {
        if (loc == 0)
            return group;
        if (group == 0)
            return loc;
        if (loc == 2)
            return 0;
        if (loc == 1)
            return (group + 1) == 6 ? 1 : (group + 1);
        return (group - 1) == 0 ? 5 : (group - 1);
    }
}
