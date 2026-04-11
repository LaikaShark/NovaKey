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

package hyperobject.keyboard.novakey.core.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Full-width, zero-height placeholder view used as the IME's candidates
 * view slot. NovaKey does not ship a suggestion strip, so this view only
 * reserves the screen width and occupies no vertical space, keeping the
 * framework happy without pushing the keyboard up.
 */
public class ControlView extends View {

    /**
     * Builds the placeholder in the given context. No measurement state
     * is cached — dimensions are pulled fresh each measure pass.
     */
    public ControlView(Context context) {
        super(context);
    }


    /**
     * Reports a size of {@code (screenWidth, 0)} regardless of the
     * incoming specs: width comes from {@link DisplayMetrics#widthPixels}
     * so the view spans the display, height is zero so nothing is drawn
     * above the keyboard.
     */
    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        setMeasuredDimension(metrics.widthPixels, 0);
    }
}
