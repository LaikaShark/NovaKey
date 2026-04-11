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

package hyperobject.keyboard.novakey.core.utils;

import android.os.CountDownTimer;

/**
 * Fire-once delayed Runnable built on top of Android's {@link CountDownTimer}.
 * <p>
 * Wraps the "wait N milliseconds, then run this callback" pattern with
 * {@link #cancel()} and {@link #reset()} controls so callers (long-press
 * detectors, tooltip triggers, etc.) can restart the countdown from zero
 * without allocating a new timer. The underlying {@code CountDownTimer}
 * is configured with its tick interval equal to its duration, so only
 * {@code onFinish} fires.
 */
public class CustomTimer {

    private CountDownTimer mTimer;
    private Runnable mEvent;
    private long mTime;


    /**
     * Creates a timer that will invoke {@code event} exactly once after
     * {@code milliseconds} have elapsed from each call to {@link #begin()}.
     * Construction does <em>not</em> start the timer.
     *
     * @param milliseconds delay before {@code event} fires
     * @param event        callback to run on the main thread when the
     *                     delay elapses
     */
    public CustomTimer(long milliseconds, final Runnable event) {
        mEvent = event;
        mTime = milliseconds;
    }


    /**
     * Starts (or restarts) the countdown. Allocates a fresh
     * {@link CountDownTimer} every call; if a previous one is still
     * running it is silently replaced — call {@link #cancel()} first if
     * that matters.
     */
    public void begin() {
        mTimer = new CountDownTimer(mTime, mTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }


            @Override
            public void onFinish() {
                mEvent.run();
            }
        }.start();
    }


    /**
     * Aborts the current countdown if one is running. Safe to call
     * before {@link #begin()} has ever been invoked.
     */
    public void cancel() {
        if (mTimer != null)
            mTimer.cancel();
    }


    /**
     * Convenience for {@code cancel() + begin()}: throws away any
     * in-flight countdown and starts a new one from zero.
     */
    public void reset() {
        cancel();
        begin();
    }
}
