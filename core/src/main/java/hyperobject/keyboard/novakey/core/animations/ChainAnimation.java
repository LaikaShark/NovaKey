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

import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Composite {@link Animation} that plays a sequence of children
 * strictly one after the other. The chain installs an end-listener on
 * each child pointing at its successor, so once step {@code i}
 * completes it triggers {@code i+1}'s {@link Animation#start}. The
 * outer chain's own on-end listener is hung on the final child.
 * <p>
 * Update notifications from the currently-running child are forwarded
 * to the chain's own update listener; the chain-level delay is applied
 * to the first child only.
 */
public class ChainAnimation implements Animation {

    private long mDelay = 0;
    private OnEndListener mOnEnd;
    private OnUpdateListener mOnUpdate;

    private final List<Animation> mAnimations;


    /** Creates an empty chain; build it up with {@link #add(Animation)}. */
    public ChainAnimation() {
        mAnimations = new ArrayList<>();
    }


    /** Creates a chain pre-populated with the given ordered list of steps. */
    public ChainAnimation(List<Animation> animations) {
        mAnimations = animations;
    }


    /**
     * Appends a new step to the end of the chain. Returned for fluent
     * building.
     */
    public ChainAnimation add(Animation animation) {
        mAnimations.add(animation);
        return this;
    }


    /**
     * Wires every child up so each one kicks off the next when it
     * ends, hangs the chain's own on-end listener on the final child,
     * propagates the chain-level delay onto the first child, then
     * starts that first child. All subsequent children will cascade
     * from their predecessors' end callbacks.
     */
    @Override
    public void start(Model model) {
        mAnimations.get(0).setDelay(mDelay);
        for (int i = 0; i < mAnimations.size() - 1; i++) {
            final int finalI = i;
            mAnimations.get(i).setOnEndListener(() -> mAnimations.get(finalI + 1).start(model));
            mAnimations.get(i).setOnUpdateListener(mOnUpdate);
        }
        mAnimations.get(mAnimations.size() - 1).setOnEndListener(mOnEnd);
        mAnimations.get(0).start(model);
    }


    /**
     * Cancels every child in the chain. Children that have not yet
     * started will simply be no-ops when their {@code cancel} fires.
     */
    @Override
    public void cancel() {
        for (Animation a : mAnimations) {
            a.cancel();
        }
    }


    /**
     * Stores the chain-level start delay. Applied to the first child
     * at {@link #start(Model)} time.
     */
    @Override
    public Animation setDelay(long delay) {
        mDelay = delay;
        return this;
    }


    /**
     * Stores the chain-level end listener. Hooked onto the last child
     * at {@link #start(Model)} time so it fires exactly when the whole
     * sequence has finished.
     */
    @Override
    public Animation setOnEndListener(OnEndListener listener) {
        mOnEnd = listener;
        return this;
    }


    /**
     * Stores the chain-level update listener. Forwarded to every
     * non-final child so the caller sees ticks while any step is
     * running.
     */
    @Override
    public Animation setOnUpdateListener(OnUpdateListener listener) {
        mOnUpdate = listener;
        return this;
    }
}
