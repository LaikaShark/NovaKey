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

package hyperobject.keyboard.novakey.core.elements.keyboards;

import hyperobject.keyboard.novakey.core.animations.utils.Animator;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Animator that linearly interpolates a {@link Key}'s size multiplier
 * between a start and end value over the life of an animation. Plugged
 * into the generic animation framework via {@link Animator}, so a
 * {@code BaseAnimation} driving keys will call {@link #update} every
 * frame with a fraction in [0, 1].
 */
public class KeySizeAnimator implements Animator<Key> {

    private final float mStart, mEnd;


    /**
     * @param start size multiplier at fraction 0
     * @param end   size multiplier at fraction 1
     */
    public KeySizeAnimator(float start, float end) {
        mStart = start;
        mEnd = end;
    }


    /**
     * Writes the interpolated size onto the key. Delegates to
     * {@link Util#fromFrac} for the actual lerp so the interpolation
     * curve matches the rest of the app's animators.
     *
     * @param key      the key whose size to set this frame
     * @param fraction 0 = animation start, 1 = animation end
     */
    @Override
    public void update(Key key, float fraction) {
        key.setSize(Util.fromFrac(mStart, mEnd, fraction));
    }
}
