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

package hyperobject.keyboard.novakey.widgets.pickers;

import android.content.Context;
import android.util.AttributeSet;

import hyperobject.keyboard.novakey.core.utils.PickerItem;
import hyperobject.keyboard.novakey.core.utils.Colors;

/**
 * {@link HorizontalPicker} concrete subclass that picks a {@link Colors}
 * entry. Each color "island" may have multiple shades; long-pressing an
 * island pops the {@link ReleasePicker} so the user can drag to select
 * a specific shade before releasing.
 * <p>
 * Emits the selected color through the inherited
 * {@link HorizontalPicker.OnItemSelectedListener}. The
 * {@code subIndex} argument of the callback is the chosen shade index
 * within the selected {@link Colors} family.
 */
public class ColorPicker extends HorizontalPicker {

    /**
     * XML-inflation constructor. Also seeds {@link #mSubIndexes} so
     * that the initial shade shown for each color island is its
     * {@link Colors#mainIndex()}.
     */
    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSubIndexes = new int[Colors.ALL.length];
        for (int i = 0; i < mSubIndexes.length; i++) {
            mSubIndexes[i] = Colors.ALL[i].mainIndex();
        }
    }


    /** Returns the full {@link Colors#ALL} array as the picker's items. */
    @Override
    protected PickerItem[] initializeItems() {
        return Colors.ALL;
    }


    /**
     * Long-press handler: if the tapped color has more than one
     * shade, starts the {@link ReleasePicker} popup centered on the
     * finger so the user can radially pick a shade. Multi-shade
     * colors pass all their shade indices to the release picker;
     * single-shade colors silently no-op.
     */
    @Override
    protected void onItemLongPress(int index, float startX, float startY) {
        if (mReleasePicker != null) {
            Colors color = (Colors) mItems[index];

            if (color.size() > 1) {
                mOnReleasePicker = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                mReleasePicker.setVisibility(VISIBLE);
                int[] subIndex = new int[color.size()];
                for (int i = 0; i < color.size(); i++) {
                    subIndex[i] = i;
                }
                mReleasePicker.onStart(startX, startY, mItems[index], subIndex);
            }
        }
    }
}
