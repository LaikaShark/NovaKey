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

import java.util.ArrayList;
import java.util.List;

/**
 * Composite {@link Animator} that fans a single {@code (target, fraction)}
 * update out to a list of child animators. Lets callers stack several
 * independent per-frame mutations (e.g. size + color + position) on
 * the same target and drive them all from a single animation.
 *
 * @param <T> the type of object each child animator mutates
 */
public class CombineAnimator<T> implements Animator<T> {

    private final List<Animator<T>> mList;


    /** Wraps a pre-built list of child animators. */
    public CombineAnimator(List<Animator<T>> list) {
        mList = list;
    }


    /** Creates an empty combine animator; build it up with {@link #add}. */
    public CombineAnimator() {
        mList = new ArrayList<>();
    }


    /**
     * Appends a child animator. Package-private because callers
     * normally build via the list constructor. Returned for fluent
     * chaining.
     */
    CombineAnimator<T> add(Animator<T> animator) {
        mList.add(animator);
        return this;
    }


    /**
     * Forwards the frame update to every child animator in insertion
     * order, so each one gets a chance to write its own property onto
     * the same target.
     */
    @Override
    public void update(T t, float fraction) {
        for (Animator<T> a : mList) {
            a.update(t, fraction);
        }
    }
}
