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

package hyperobject.keyboard.novakey.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * {@link HorizontalScrollView} subclass that exposes scroll position
 * changes via an {@link OnScrollListener}. Used by
 * {@link hyperobject.keyboard.novakey.widgets.pickers.HorizontalPicker}
 * so that scrolling the enclosing picker dismisses the release-picker
 * popup and cancels any in-flight long-press timer.
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView {

    OnScrollListener mListener;


    /** Programmatic constructor. */
    public ObservableHorizontalScrollView(Context context) {
        super(context);
    }


    /** XML-inflation constructor. */
    public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /** Three-arg constructor for styled inflation. */
    public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Four-arg constructor; API 21+. Kept under a
     * {@link TargetApi} annotation from before the module's
     * {@code minSdk} was bumped to 21 in the 2026 modernization pass.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * Forwards every scroll event to the attached listener after
     * letting the superclass update its own scroll state.
     */
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mListener != null) {
            mListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }


    /** Registers the scroll listener. Pass {@code null} to clear. */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mListener = listener;
    }


    /** Callback interface matching the arguments of {@link #onScrollChanged(int, int, int, int)}. */
    public interface OnScrollListener {
        /**
         * Fired after the superclass commits a scroll position change.
         *
         * @param view source view
         * @param x    new horizontal scroll offset
         * @param y    new vertical scroll offset (always 0 for a
         *             horizontal scroll view)
         * @param oldx previous horizontal offset
         * @param oldy previous vertical offset
         */
        void onScrollChanged(View view, int x, int y, int oldx, int oldy);
    }
}
