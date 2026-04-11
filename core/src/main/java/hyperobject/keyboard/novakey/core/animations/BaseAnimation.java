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

import android.animation.ValueAnimator;

import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Skeleton {@link Animation} wired on top of a single Android
 * {@link ValueAnimator}. Subclasses only need to build the actual
 * {@code ValueAnimator} in {@link #animator(Model)}; this class handles
 * the plumbing for start-delay, end/update callbacks, and cancellation.
 * <p>
 * If {@link #animator(Model)} returns {@code null} the animation is
 * treated as a no-op and the on-end listener fires immediately, so
 * subclasses can short-circuit trivial cases (e.g. nothing to animate
 * this frame).
 */
public abstract class BaseAnimation implements Animation {

    private long mDelay = 0;
    private ValueAnimator mAnim;
    private OnEndListener mOnEnd;
    private OnUpdateListener mOnUpdate;


    /**
     * Builds the backing {@link ValueAnimator} via
     * {@link #animator(Model)}, stacks the configured delay on top of
     * whatever the subclass set, and wires the end/update callbacks
     * before kicking it off. A {@code null} animator is treated as a
     * no-op and immediately fires the end listener.
     */
    @Override
    public void start(Model model) {
        mAnim = animator(model);
        if (mAnim == null) {
            if (mOnEnd != null)
                mOnEnd.onEnd();
            return;
        }
        mAnim.setStartDelay(mAnim.getStartDelay() + mDelay);

        mAnim.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
            }


            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (mOnEnd != null)
                    mOnEnd.onEnd();
            }


            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }


            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mOnUpdate != null)
                    mOnUpdate.onUpdate();
            }
        });
        mAnim.start();
    }


    /**
     * Cancels the backing {@link ValueAnimator} if it was ever built
     * and is still running. Safe to call before {@link #start(Model)}.
     */
    @Override
    public void cancel() {
        if (mAnim != null)
            mAnim.cancel();
    }


    /**
     * Subclass hook: build the actual {@link ValueAnimator} to play.
     * Implementations are free to also call {@code model.update()}
     * inside their own listeners to invalidate the view on each frame.
     *
     * @return the animator to run, or {@code null} to skip this pass
     */
    protected abstract ValueAnimator animator(Model model);


    /** Stores the start delay applied inside {@link #start(Model)}. */
    @Override
    public Animation setDelay(long delay) {
        mDelay = delay;
        return this;
    }


    /** Stores the end listener fired by the backing animator. */
    @Override
    public Animation setOnEndListener(OnEndListener listener) {
        mOnEnd = listener;
        return this;
    }


    /** Stores the update listener fired on each frame of the animator. */
    @Override
    public Animation setOnUpdateListener(OnUpdateListener listener) {
        mOnUpdate = listener;
        return this;
    }
}
