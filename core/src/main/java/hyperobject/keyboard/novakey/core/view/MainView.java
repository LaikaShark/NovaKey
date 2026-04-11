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
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

public class MainView extends NovaKeyView {

    public MainView(Context context) {
        this(context, null);

    }


    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }


    /**
     * Android 15 (target SDK 35) renders IME windows edge-to-edge, so the
     * input view's bottom edge is anchored to the bottom of the screen —
     * directly behind the gesture/3-button navigation bar. The keyboard is
     * drawn at {@code y = r + p} from the top of this view, so growing the
     * reported height by the navbar inset is enough to push the drawn keys
     * out from under the navbar: the extra height lands as empty space at
     * the bottom of the view, which the framework then aligns with the
     * navbar region.
     *
     * Insets are queried from the live root window insets at measure time
     * rather than from the {@code navigation_bar_height} platform dimen
     * resource, which is cached at boot and reports 0 on gesture-nav
     * devices on API 35+.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                mModel.getMainDimensions().getWidth(),
                mModel.getMainDimensions().getHeight() + bottomNavInset(this));
    }


    /**
     * Reads the bottom navigation-bar inset from the given view's root window
     * insets. Intended for IME input views that need to reserve space under
     * the navbar on Android 15+ edge-to-edge IME windows.
     *
     * Returns 0 on API &lt; 30 (where the legacy IME layout already keeps the
     * input view above the navbar) and 0 if the view isn't yet attached
     * (insets arrive shortly after, and the caller should re-measure then).
     */
    public static int bottomNavInset(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return 0;
        }
        WindowInsets insets = view.getRootWindowInsets();
        if (insets == null) {
            return 0;
        }
        return insets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()).bottom;
    }


    // Insets can arrive after the first measure pass; re-measure when they
    // change so the reserved navbar area tracks the live inset.
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        requestLayout();
        return super.onApplyWindowInsets(insets);
    }

}







