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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.Themeable;

/**
 * A self-drawing Material-style floating action button used in the
 * setup / tutorial / demo flows.
 * <p>
 * Renders as a circle with a soft drop shadow and an icon glyph on
 * top, rather than relying on the platform {@code FloatingActionButton}.
 * Picks up its size, icon, and colours from XML via the
 * {@code R.styleable.FloatingButton} attr set, and implements
 * {@link Themeable} so the hosting screen can retint it in bulk when
 * the app theme changes by calling {@link #setTheme(MasterTheme)}.
 * <p>
 * Does not handle touches itself — hosting layouts attach an
 * {@code OnClickListener} through {@link View}'s normal API.
 */
public class FloatingButton extends View implements Themeable {

    private int mBackground, mFront;
    private int mHeight, mRealHeight;
    private Drawable mIcon;
    private float mRadius;

    private Paint p;


    /** Single-arg ctor for programmatic instantiation without a style. */
    public FloatingButton(Context context) {
        this(context, null);
    }


    /** Two-arg ctor used by XML inflation without a default style. */
    public FloatingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    /**
     * Full ctor: allocates the shared {@link Paint}, pulls
     * {@code R.dimen.button_radius} for the circle radius, and then
     * reads {@code button_height}, {@code back_color}, {@code front_color},
     * and {@code button_icon} from the {@code FloatingButton} styleable
     * attrs to initialise the cosmetic fields. The styled attributes
     * array is always recycled in the {@code finally} block.
     */
    public FloatingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        p = new Paint();
        p.setAntiAlias(true);
        mRadius = (int) getResources().getDimension(R.dimen.button_radius);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FloatingButton, defStyleAttr, 0);

        try {
            mHeight = a.getInteger(R.styleable.FloatingButton_button_height,
                    (int) getResources().getDimension(R.dimen.default_button_height));
            mBackground = a.getColor(R.styleable.FloatingButton_back_color, 0xFF616161);
            mFront = a.getColor(R.styleable.FloatingButton_front_color, 0xFFF0F0F0);
            String str = a.getString(R.styleable.FloatingButton_button_icon);
            mIcon = Icons.get(str);
        } finally {
            a.recycle();
        }
        mRealHeight = mHeight;
    }


    /**
     * Reports a square measured size of
     * {@code (mHeight * 2 + radius * 2)} in each dimension, leaving
     * slack around the circle for the drop shadow.
     * <p>
     * Note the signature uses {@code onMeasure(int w, int h)} instead
     * of the {@code View.onMeasure(int widthSpec, int heightSpec)}
     * contract, so this override is effectively a new overload —
     * documentation-only pass, left as-is.
     */
    @Override
    public void onMeasure(int w, int h) {
        setMeasuredDimension(mHeight * 2 + (int) mRadius * 2, mHeight * 2 + (int) mRadius * 2);
    }


    /**
     * Draws one frame: a circle at the view centre offset upward by
     * {@code mRealHeight} (so the drop shadow has room below it), a
     * soft shadow layer behind it, and then the icon glyph centred in
     * the same position.
     */
    @Override
    public void onDraw(Canvas canvas) {
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        //Circle
        p.setShadowLayer(mRealHeight + 2, 0, mRealHeight, 0x60000000);
        p.setColor(mBackground);
        canvas.drawCircle(x, y - mRealHeight, mRadius, p);
        p.clearShadowLayer();

        //Icons
        p.setColor(mFront);
        mIcon.draw(x, y - mRealHeight, mRadius, p, canvas);
    }


    /** Returns the ARGB fill colour of the button circle. */
    public int getBackColor() {
        return mBackground;
    }


    /** Sets the ARGB fill colour of the button circle. */
    public void setBackColor(int backColor) {
        this.mBackground = backColor;
    }


    /** Returns the icon {@link Drawable} currently rendered on the button. */
    public Drawable getIcon() {
        return mIcon;
    }


    /** Replaces the icon drawn on top of the button. */
    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }


    /** Returns the elevation value (used for shadow offset, not size). */
    public int getButtonHeight() {
        return mHeight;
    }


    /** Sets the elevation value (used for shadow offset, not size). */
    public void setButtonHeight(int height) {
        this.mHeight = height;
    }


    /** Returns the ARGB colour of the icon glyph. */
    public int getFrontColor() {
        return mFront;
    }


    /** Sets the ARGB colour of the icon glyph. */
    public void setFrontColor(int front) {
        this.mFront = front;
    }


    /**
     * {@link Themeable} hook: repaints the button in the active app
     * theme — primary colour for the background, contrast colour for
     * the icon. The caller is responsible for invalidating the view.
     */
    public void setTheme(MasterTheme theme) {
        setBackColor(theme.getPrimaryColor());
        setFrontColor(theme.getContrastColor());
    }

}
