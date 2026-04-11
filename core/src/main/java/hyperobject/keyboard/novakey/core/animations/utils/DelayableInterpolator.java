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

package hyperobject.keyboard.novakey.core.animations.utils;

import android.animation.TimeInterpolator;

/**
 * {@link TimeInterpolator} decorator that lets a child interpolator
 * play for only a sub-window of a larger animation. Used by
 * {@link MultiValueAnimator} so each target in a group can have its
 * own delay and duration while the underlying {@code ValueAnimator}
 * still runs a single 0 to 1 pass over the whole total duration.
 * <p>
 * Before the sub-window starts, {@link #getInterpolation} reports 0;
 * after the sub-window ends it reports 1; inside the window it
 * rescales the input fraction into {@code [0, 1]} and hands it to the
 * wrapped interpolator.
 */
public class DelayableInterpolator implements TimeInterpolator {

    private float mBegPercent, mEndPercent;
    private TimeInterpolator mBaseInterpolator;


    /**
     * @param delay            how long this sub-animation should stay
     *                         at 0 before starting, in milliseconds
     * @param duration         how long this sub-animation actually
     *                         animates for, in milliseconds
     * @param totalDuration    total length of the parent animation so
     *                         delay/duration can be converted into
     *                         fractions of the parent timeline
     * @param baseInterpolator underlying curve applied inside the
     *                         active window
     * @throws IllegalArgumentException if any of delay/duration are
     *                                  negative, or if delay+duration
     *                                  exceeds totalDuration
     */
    public DelayableInterpolator(long delay, long duration, long totalDuration,
                                 TimeInterpolator baseInterpolator) {
        if (delay < 0 || duration <= 0 || totalDuration <= 0
                || delay > totalDuration || (delay + duration) > totalDuration) {
            throw new IllegalArgumentException("Invalid delay or duration. delay: " +
                    delay + "   duration: " + duration + "   totalDuration: " + totalDuration);
        }

        this.mBaseInterpolator = baseInterpolator;

        this.mBegPercent = (float) delay / (float) duration;
        this.mEndPercent = (float) (totalDuration - duration - delay) / (float) totalDuration;
    }


    /**
     * Rescales the parent-animation input fraction into this
     * sub-animation's local fraction. Returns {@code 0} while the
     * parent has not yet reached the local start, {@code 1} after the
     * local window has ended, and otherwise forwards a rescaled value
     * into the wrapped base interpolator.
     *
     * @param input parent-animation fraction in {@code [0, 1]}
     * @return interpolated fraction (may overshoot {@code [0, 1]}
     * if the wrapped interpolator does)
     */
    @Override
    public float getInterpolation(float input) {
        if (input <= mBegPercent)
            return 0;
        if (input >= (1 - mEndPercent))
            return 1;
        return mBaseInterpolator.getInterpolation(
                (input - mBegPercent) / (1 - mBegPercent - mEndPercent));
    }
}
