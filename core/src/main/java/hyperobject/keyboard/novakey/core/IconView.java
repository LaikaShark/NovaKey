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

package hyperobject.keyboard.novakey.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * Lightweight Android {@link View} that draws a single NovaKey
 * {@link Drawable} centred in the view's bounds and calls back through
 * its own {@link OnClickListener} on touch-down.
 * <p>
 * Used as a minimal button widget in the tutorial / setup screens —
 * it's not themed and it doesn't participate in accessibility focus or
 * ripple, it just draws an icon and fires a click. The view enables
 * software layer rendering so the vector {@link Drawable} can use
 * features (shadow layers, blur filters) that the hardware layer
 * wouldn't otherwise support.
 * <p>
 * The {@code touched} flag is flipped on down/up and the view
 * invalidates so {@link #onDraw} can lighten the icon tint while
 * the user is pressing.
 */
public class IconView extends View implements View.OnTouchListener {

    private boolean touched = false;
    private Drawable icon;
    private float size = 1.0f;
    private Paint p;
    private int mColor = 0xFFA0A0A0;

    private OnClickListener listener;


    /**
     * Standard XML-inflation ctor. Enables software layer rendering,
     * registers itself as its own {@link View.OnTouchListener}, and
     * allocates an antialiased shared {@link Paint}.
     */
    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setOnTouchListener(this);
        p = new Paint();
        p.setAntiAlias(true);
    }


    /** Replaces the icon drawn inside the view. Caller must invalidate. */
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


    /**
     * Sets the icon's render size as a fraction of the view width. The
     * icon is drawn with a half-diameter of {@code width * size}.
     */
    public void setSize(float size) {
        this.size = size;
    }


    /** Sets the tint applied to the icon glyph. */
    public void setColor(int color) {
        mColor = color;
    }


    /**
     * Draws the icon centred in the view, lightening the tint by two
     * shade steps via {@link Util#colorShade} while the view is being
     * pressed so the user gets visible press feedback.
     */
    @Override
    public void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        p.setColor(touched ? Util.colorShade(mColor, 2) : mColor);
        icon.draw(w / 2, h / 2, w * size, p, canvas);
    }


    /**
     * Touch handler that doubles as the click dispatcher. Fires the
     * {@link OnClickListener} on {@link MotionEvent#ACTION_DOWN}
     * (unlike Android's standard View, which fires on UP), toggles
     * the {@code touched} flag on down/up, and invalidates the view
     * so any pressed state change is redrawn. Always returns
     * {@code true} to claim the gesture.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touched = true;
                if (listener != null)
                    listener.onClick();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touched = false;
                invalidate();
                break;
        }
        return true;
    }


    /** Installs the click callback fired on touch-down. */
    public void setClickListener(OnClickListener listener) {
        this.listener = listener;
    }


    /**
     * Parameterless click callback — no view argument, unlike the
     * platform {@code View.OnClickListener}. Intentional: callers
     * already know which {@link IconView} they hooked up.
     */
    public interface OnClickListener {
        void onClick();
    }
}
