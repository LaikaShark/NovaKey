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

import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Base contract for every animation in the keyboard. An animation is a
 * stateful object that, once {@link #start} is called, drives per-frame
 * mutations on the {@link Model} (or on objects owned by it) until it
 * either completes or is cancelled.
 * <p>
 * Concrete implementations include {@link BaseAnimation} (a single
 * {@link android.animation.ValueAnimator}-backed pass) and
 * {@link ChainAnimation} (sequences several animations one after the
 * other). Animations expose fluent setters for start delay and
 * begin/update/end callbacks so they can be composed by the elements
 * that kick them off during their draw pass.
 */
public interface Animation {

    /**
     * Begins playback. Implementations initialize any per-run state
     * (the backing {@code ValueAnimator}, target snapshots, etc.) here
     * and register whatever listeners are needed to tick the model
     * forward each frame.
     */
    void start(Model model);


    /**
     * Aborts an in-flight animation. No-op if the animation never
     * started or has already finished.
     */
    void cancel();


    /**
     * Sets the delay applied before the animation actually begins
     * ticking, in milliseconds. Returned for fluent chaining.
     */
    Animation setDelay(long delay);


    /**
     * Installs a listener fired exactly once when the animation
     * completes (or is short-circuited because there is nothing to do).
     * Returned for fluent chaining.
     */
    Animation setOnEndListener(OnEndListener listener);


    /**
     * Callback for end-of-animation notifications.
     */
    interface OnEndListener {
        /**
         * Invoked after the final frame has been applied, or
         * immediately if the animation produces no work.
         */
        void onEnd();
    }


    /**
     * Installs a listener fired on every animation tick, so callers
     * can invalidate the view or react to in-progress state changes.
     * Returned for fluent chaining.
     */
    Animation setOnUpdateListener(OnUpdateListener listener);


    /**
     * Callback for per-frame update notifications.
     */
    interface OnUpdateListener {
        /**
         * Invoked on every frame the animation produces a new value.
         */
        void onUpdate();
    }
}
