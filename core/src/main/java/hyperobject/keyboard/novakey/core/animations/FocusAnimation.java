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
import android.view.animation.AnticipateInterpolator;

import hyperobject.keyboard.novakey.core.animations.utils.Animator;
import hyperobject.keyboard.novakey.core.elements.keyboards.Key;
import hyperobject.keyboard.novakey.core.elements.keyboards.KeySizeAnimator;

/**
 * {@link CharAnimation} that hides every key whose character is not
 * in the supplied allow-list by shrinking it from size {@code 1} down
 * to {@code 0} using an {@link AnticipateInterpolator} (which briefly
 * scales up before collapsing inward). Keys whose character IS in the
 * allow-list are left alone — the animator lookup returns {@code null}
 * for them so the underlying {@link CharAnimation} machinery simply
 * skips them each frame.
 * <p>
 * Used to "focus" the keyboard on a small subset of keys, e.g. when
 * showing character variants or a letter-picker overlay.
 */
public class FocusAnimation extends CharAnimation {

    private final TimeInterpolator mInterpolator = new AnticipateInterpolator();
    private final Animator<Key> mAnimator = new KeySizeAnimator(1, 0);
    private Character[] mChars;


    /**
     * @param chars the characters that should remain visible; every
     *              other key on the active keyboard will shrink out
     */
    public FocusAnimation(Character[] chars) {
        super(1);
        mChars = chars;
    }


    /**
     * Every key shares the same {@link AnticipateInterpolator} — only
     * keys outside the allow-list will actually get animated.
     */
    @Override
    protected TimeInterpolator getInterpolatorFor(Key k) {
        return mInterpolator;
    }


    /**
     * Returns the shrink animator for any key whose character is NOT
     * in the allow-list; returns {@code null} (which the base class
     * treats as "no mutation") for keys that should stay visible.
     */
    @Override
    protected Animator<Key> getAnimatorFor(Key k) {
        for (Character c : mChars) {
            if (c.equals(k.getChar()))
                return null;
        }
        return mAnimator;
    }
}
