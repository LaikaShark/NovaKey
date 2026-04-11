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
import android.animation.ValueAnimator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link ValueAnimator} extension that animates many keyed values in
 * parallel over a single {@code [0, 1]} pass, each with its own
 * delay/duration sub-window and interpolator. Targets register via
 * {@link #addInterpolator}; when {@link #start} is called the
 * animator totals up the longest {@code delay + duration} to pick an
 * overall length, wraps every registered interpolator in a
 * {@link DelayableInterpolator} over that total, then ticks a single
 * Android {@code ValueAnimator} from 0 to 1. Per-tick, the registered
 * {@link MultiUpdateListener} is fired once per target (with that
 * target's locally-rescaled fraction) and then once globally.
 * <p>
 * Concrete callers must always build instances via {@link #create()}
 * so the base {@code ValueAnimator} is configured with the required
 * {@code ofFloat(0, 1)} value range.
 *
 * @param <K> type used to key each sub-value (usually
 *            {@link hyperobject.keyboard.novakey.core.elements.keyboards.Key})
 */
public class MultiValueAnimator<K> extends ValueAnimator {

    /**
     * Factory that builds a new animator pre-configured with the
     * {@code ofFloat(0, 1)} value range — required so the
     * {@code DelayableInterpolator}s see fractions in the right range.
     */
    public static <K> MultiValueAnimator<K> create() {
        MultiValueAnimator<K> anim = new MultiValueAnimator<>();
        anim.setFloatValues(0, 1);
        return anim;
    }


    private Map<K, InterpolatorData> mInterpolatorData;//builder map

    private Map<K, DelayableInterpolator> mInterpolators;//filled with delayable interpolators
    private MultiUpdateListener mUpdateListener;


    /**
     * Allocates the builder map and installs the per-frame update
     * listener that fans each tick out to the
     * {@link MultiUpdateListener}. Do not call directly — use
     * {@link #create()} so the value range is set up correctly.
     */
    public MultiValueAnimator() {
        super();
        mInterpolatorData = new HashMap<>();

        this.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) getAnimatedValue();
                if (mUpdateListener != null) {

                    for (Map.Entry<K, DelayableInterpolator> e : mInterpolators.entrySet()) {
                        float actual = e.getValue().getInterpolation(v);
                        mUpdateListener.onValueUpdate(animation, actual, e.getKey());
                    }
                    mUpdateListener.onAllUpdate(animation, v);
                }
            }
        });
    }


    /**
     * Figures out the longest {@code delay + duration} across every
     * registered target, sets that as the overall animator duration,
     * then rebuilds the per-target map with {@link DelayableInterpolator}s
     * stretched against that total. Finally kicks off the underlying
     * {@link ValueAnimator}.
     */
    @Override
    public void start() {
        long totalDuration = 0;
        //add up all the durations
        for (Map.Entry<K, InterpolatorData> e : mInterpolatorData.entrySet()) {
            InterpolatorData id = e.getValue();
            long test = id.delay + id.duration;
            if (test > totalDuration)
                totalDuration = test;
        }
        setDuration(totalDuration);

        //set actual map
        mInterpolators = new HashMap<>();
        for (Map.Entry<K, InterpolatorData> e : mInterpolatorData.entrySet()) {
            InterpolatorData id = e.getValue();
            mInterpolators.put(e.getKey(),
                    new DelayableInterpolator(id.delay, id.duration,
                            totalDuration, id.interpolator));
        }
        super.start();
    }


    /**
     * Registers a target's sub-interpolator. Must be called before
     * {@link #start()}; the triple {@code (interpolator, delay,
     * duration)} is stashed until start time and then wrapped in a
     * {@link DelayableInterpolator}.
     *
     * @param key              target identifier
     * @param timeInterpolator curve applied inside this target's
     *                         active window
     * @param delay            offset from t=0 before this target
     *                         begins animating, in milliseconds
     * @param duration         how long this target animates for, in
     *                         milliseconds
     */
    public void addInterpolator(K key, TimeInterpolator timeInterpolator, long delay, long duration) {
        mInterpolatorData.put(key, new InterpolatorData(timeInterpolator, delay, duration));
    }


    /**
     * Struct holding a target's interpolator plus its delay and
     * duration between {@link #addInterpolator} and {@link #start()}.
     */
    private class InterpolatorData {
        TimeInterpolator interpolator;
        long delay, duration;


        /**
         * Stores the three registration fields.
         */
        public InterpolatorData(TimeInterpolator interpolator, long delay, long duration) {
            this.interpolator = interpolator;
            this.delay = delay;
            this.duration = duration;
        }
    }


    /**
     * Returns the set of registered target keys after {@link #start()}
     * has built the final interpolator map. Calling before start will
     * throw {@link NullPointerException}.
     */
    public Set<K> getKeys() {
        return mInterpolators.keySet();
    }


    /**
     * Installs the per-tick listener that will receive both the
     * per-target and end-of-frame callbacks.
     */
    public void setMultiUpdateListener(MultiUpdateListener updateListener) {
        mUpdateListener = updateListener;
    }


    /**
     * Per-frame callback contract for {@link MultiValueAnimator}.
     * Implementations receive one {@link #onValueUpdate} call per
     * registered target per frame, followed by a single
     * {@link #onAllUpdate} call marking the end of the frame.
     */
    public interface MultiUpdateListener<K> {
        /**
         * Fired once per registered target on every frame, with that
         * target's locally-interpolated fraction.
         *
         * @param animator driving animator
         * @param value    sub-fraction for this target; 0 until the
         *                 delay elapses, 1 once the sub-window ends
         * @param key      target whose fraction this is
         */
        void onValueUpdate(ValueAnimator animator, float value, K key);


        /**
         * Fired once per frame after all {@link #onValueUpdate}
         * callbacks for the same frame have been delivered, carrying
         * the raw parent fraction.
         *
         * @param animator driving animator
         * @param value    the raw parent fraction in {@code [0, 1]}
         */
        void onAllUpdate(ValueAnimator animator, float value);
    }
}
