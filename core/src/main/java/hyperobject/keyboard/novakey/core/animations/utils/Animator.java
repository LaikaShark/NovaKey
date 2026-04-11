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

/**
 * Functional interface for the per-frame mutation an animation wants
 * to apply to one target object. Implementations read the supplied
 * fraction (0 = animation start, 1 = animation end), compute the
 * corresponding value, and write it onto {@code t}. Used by
 * {@link MultiValueAnimator}, {@link CombineAnimator}, and every
 * subclass of {@code CharAnimation} as the strategy for actually
 * mutating keys each frame — the base animator framework supplies
 * the fraction, the {@code Animator} decides what that fraction
 * means.
 *
 * @param <T> the type of object this animator mutates
 */
public interface Animator<T> {

    /**
     * Applies the interpolated state for {@code fraction} onto
     * {@code t}. Called once per frame while the parent animation is
     * running.
     *
     * @param t        the target to mutate this frame
     * @param fraction {@code [0, 1]} where 0 is the animation start
     *                 and 1 is the end; may overshoot past 1 or under
     *                 0 if the caller's interpolator does so
     */
    void update(T t, float fraction);
}
