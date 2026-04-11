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
import android.animation.ValueAnimator;

import java.util.HashMap;
import java.util.Map;

import hyperobject.keyboard.novakey.core.animations.utils.Animator;
import hyperobject.keyboard.novakey.core.animations.utils.DelayableInterpolator;
import hyperobject.keyboard.novakey.core.animations.utils.MultiValueAnimator;
import hyperobject.keyboard.novakey.core.elements.keyboards.Key;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Abstract {@link BaseAnimation} that animates every {@link Key} on
 * the active keyboard in parallel, using a per-key delay/interpolator
 * combination driven by a {@link MultiValueAnimator}. Concrete
 * subclasses pick the per-key interpolator ({@link #getInterpolatorFor})
 * and the per-key mutation ({@link #getAnimatorFor}); this class
 * handles building the multi-animator and staggering each key's start
 * time according to a style flag.
 * <p>
 * The style is a bitmask using {@link #NONE}, {@link #CENTER},
 * {@link #RANDOM}, {@link #FLIP_X}, {@link #FLIP_Y}, {@link #RIGHT},
 * and {@link #UP} which control whether keys stagger in from the
 * center, in random order, or from one of the four edges of the wheel.
 * The total animation duration is split evenly between "move" and
 * "finish" phases, giving each key a window of {@code duration/2} to
 * play inside the total {@code duration}.
 */
public abstract class CharAnimation extends BaseAnimation {

    public final static int NONE = -1, CENTER = 0, RANDOM = 1,
            FLIP_X = 2, FLIP_Y = 4, RIGHT = 8, UP = 16;

    private final int mStyle;
    private final long mDuration;
    private final Map<Key, Animator<Key>> mAnimators;


    /**
     * Convenience constructor using a default total duration of 500ms.
     */
    public CharAnimation(int style) {
        this(style, 500);
    }


    /**
     * @param style    bitmask selecting the stagger direction (see the
     *                 class-level docs for the flag constants)
     * @param duration total animation duration in milliseconds
     */
    public CharAnimation(int style, long duration) {
        mStyle = style;
        mDuration = duration;
        mAnimators = new HashMap<>();
    }


    /**
     * Builds a {@link MultiValueAnimator} keyed by {@link Key}: for
     * every key on the active keyboard it computes a stagger delay via
     * {@link #getDelay}, wraps the subclass-supplied interpolator in a
     * {@link DelayableInterpolator} spanning {@code [delay, delay + duration/2]}
     * within the total {@code mDuration}, and registers the per-key
     * mutator via {@link #getAnimatorFor}. The multi-animator's update
     * listener then routes each per-key fraction back to the matching
     * {@link Animator} so it can write the new state onto the key.
     */
    @Override
    protected ValueAnimator animator(Model model) {
        MultiValueAnimator<Key> anim = MultiValueAnimator.create();

        for (Key k : model.getKeyboard()) {
            long dur = mDuration / 2;
            long delay = getDelay(model, k, dur);
            anim.addInterpolator(k,
                    new DelayableInterpolator(
                            delay, dur, mDuration, getInterpolatorFor(k)), delay, dur);
            mAnimators.put(k, getAnimatorFor(k));
        }
        anim.setMultiUpdateListener(new MultiValueAnimator.MultiUpdateListener<Key>() {
            @Override
            public void onValueUpdate(ValueAnimator animator, float value, Key key) {
                Animator<Key> anim = mAnimators.get(key);
                if (anim != null)
                    anim.update(key, value);
            }


            @Override
            public void onAllUpdate(ValueAnimator animator, float value) {
                //TODO: need way to update
            }
        });

        return anim;
    }


    /**
     * Subclass hook: return the {@link TimeInterpolator} to use for
     * the given key. Called once per key while building the animation.
     */
    protected abstract TimeInterpolator getInterpolatorFor(Key k);


    /**
     * Subclass hook: return the {@link Animator} that will mutate the
     * given key on each frame, or {@code null} to leave that key
     * untouched. Called once per key while building the animation.
     */
    protected abstract Animator<Key> getAnimatorFor(Key k);


    /**
     * Computes the per-key start delay for the configured stagger style.
     * <p>
     * How: style {@code -1} yields zero delay for every key. With the
     * {@link #RANDOM} bit set, the delay is a uniform random in
     * {@code [0, max)}. Otherwise a reference point (x,y) is picked
     * from the wheel: the center for {@link #CENTER}, shifted right or
     * left by {@link #RIGHT}+{@link #FLIP_X}, or up/down by
     * {@link #UP}+{@link #FLIP_Y}. The delay is then {@code max} scaled
     * by the distance from the key to that reference point, normalized
     * by the wheel diameter — keys farther from the reference point
     * wait longer, producing a wave effect.
     *
     * @return the delay in milliseconds, capped to {@code max}
     */
    private long getDelay(Model model, Key k, long max) {
        MainDimensions d = model.getMainDimensions();
        if (mStyle == -1)
            return 0;

        if ((mStyle & RANDOM) == RANDOM)
            return (long) (Math.random() * max);

        float x = d.getX(), y = d.getY();

        if ((mStyle & RIGHT) == RIGHT)
            x -= d.getRadius() * (((mStyle & FLIP_X) == FLIP_X) ? -1 : 1);
        if ((mStyle & UP) == UP)
            y += d.getRadius() * (((mStyle & FLIP_Y) == FLIP_Y) ? -1 : 1);
        ;

        float dist = Util.distance(x, y, k.getPosn().getX(d), k.getPosn().getX(d));
        float ratio = dist / (d.getRadius() * 2);
        return (long) (max * ratio);
    }
}
