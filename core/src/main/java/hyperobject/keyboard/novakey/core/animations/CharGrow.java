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
import android.view.animation.OvershootInterpolator;

import hyperobject.keyboard.novakey.core.animations.utils.Animator;
import hyperobject.keyboard.novakey.core.elements.keyboards.Key;
import hyperobject.keyboard.novakey.core.elements.keyboards.KeySizeAnimator;

/**
 * {@link CharAnimation} that makes every key on the active keyboard
 * pop into view by scaling its size from {@code 0} up to {@code 1}
 * with an {@link OvershootInterpolator} (so keys momentarily bounce
 * past their final size before settling). The per-key stagger follows
 * whatever style flag was passed to the constructor — this is what
 * produces the "characters blooming from the center" effect when a
 * new keyboard is shown.
 */
public class CharGrow extends CharAnimation {

    private final TimeInterpolator mInterpolator = new OvershootInterpolator();
    private final Animator<Key> mAnimator = new KeySizeAnimator(0, 1);


    /**
     * @param style    stagger-direction bitmask (see {@link CharAnimation})
     * @param duration total animation duration in milliseconds
     */
    public CharGrow(int style, long duration) {
        super(style, duration);
    }


    /**
     * Uses the default 500ms total duration from {@link CharAnimation}.
     */
    public CharGrow(int style) {
        super(style);
    }


    /**
     * All keys share the same {@link OvershootInterpolator} instance
     * — the bounce curve is the same everywhere, only the stagger
     * delay differs.
     */
    @Override
    protected TimeInterpolator getInterpolatorFor(Key k) {
        return mInterpolator;
    }


    /**
     * All keys share the same {@link KeySizeAnimator} going from size
     * {@code 0} to {@code 1}, i.e. full grow-in.
     */
    @Override
    protected Animator<Key> getAnimatorFor(Key k) {
        return mAnimator;
    }
}
