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

import android.content.Context;
import android.util.AttributeSet;

import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.view.NovaKeyView;

/**
 * {@link NovaKeyView} subclass used as an embeddable, non-IME preview
 * of the keyboard — specifically by
 * {@link hyperobject.keyboard.novakey.settings.StylePreferenceActivity}
 * to show the live effect of a theme/color change.
 * <p>
 * Overrides the normal {@link NovaKeyView} sizing path with a simpler
 * one that fits the wheel into the measured bounds and writes the
 * derived geometry straight into the attached model's
 * {@link MainDimensions}.
 */
public class NovaKeyPreview extends NovaKeyView {

    /** Programmatic constructor. */
    public NovaKeyPreview(Context context) {
        super(context);
    }


    /** XML-inflation constructor. */
    public NovaKeyPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Measure pass: delegates to the superclass for parent-reported
     * sizing, then recomputes and writes the wheel geometry back into
     * {@link MainDimensions} so the preview's theme drawing uses the
     * same coordinates the real IME would.
     * <p>
     * How: picks the wheel radius as half of the smaller of the
     * available width or height (after padding), centers the wheel
     * horizontally, pins it to the top padding, and derives the inner
     * small-radius as {@code r/3}. All five dimensions are written to
     * the model in one shot so
     * {@link NovaKeyView#onDraw} can pick them up on the next frame.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float w = MeasureSpec.getSize(widthMeasureSpec);
        float h = MeasureSpec.getSize(heightMeasureSpec);
        float r = Math.min(h - getPaddingTop() - getPaddingBottom(),
                w - getPaddingRight() - getPaddingLeft());
        r /= 2;
        float x = w / 2;
        float y = getPaddingTop() + r;
        float sr = r / 3;

        MainDimensions dimens = mModel.getMainDimensions();
        dimens.setWidth((int) w);
        dimens.setHeight((int) h);
        dimens.setRadius(r);
        dimens.setSmallRadius(sr);
        dimens.setPadding(getPaddingTop());
        dimens.setX(x);
        dimens.setY(y);
    }
}
