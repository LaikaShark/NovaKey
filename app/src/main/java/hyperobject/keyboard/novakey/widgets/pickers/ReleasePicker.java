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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.animations.utils.MultiValueAnimator;
import hyperobject.keyboard.novakey.core.utils.PickerItem;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Radial "long-press and drag to pick" picker popup, used as the
 * secondary picker behind {@link ColorPicker} for selecting a shade
 * within a multi-shade color family.
 * <p>
 * On {@link #onStart} the picker lays its sub-items on a semicircle
 * centered on the long-press finger position. The initial arc angle
 * is chosen by {@link #getFixedAngle(float, float)} so the items
 * always fan out toward the closest interior edge of the picker view.
 * As the finger moves, each item's apparent size is computed from the
 * ratio of its distance-from-center to its distance-from-finger — the
 * item nearest the finger grows to 1.5x and becomes the active
 * selection. On release, the listener is fired once with the
 * selected sub-index (or onCancel if none was reached).
 */
public class ReleasePicker extends View {

    private float dimen;
    private Paint p;
    private int mIndex = -1, mPrevIndex = -1;

    private float mCenterX, mCenterY, mRadius;
    private float mFingX, mFingY;

    private PickerItem mItems;
    private int[] mSubIndexes;
    private ItemData[] mData;

    private OnItemListener mListener;


    /**
     * XML-inflation constructor. Sets up the shadow-backed paint,
     * reads the picker dimension from resources, and forces software
     * rendering so SVG drawables render correctly.
     */
    public ReleasePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        p.setShadowLayer(10, 0, 10, 0x80000000);
        dimen = context.getResources().getDimension(R.dimen.picker_dimen);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//allows svgs
    }


    /**
     * Touch handler invoked via {@link HorizontalPicker}'s forwarding
     * logic while the release picker is active. MOVE updates the
     * finger position and recomputes item sizes; UP fires the
     * listener with the current {@link #mIndex} (or cancel if no
     * item is currently highlighted) and hides the picker.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mFingX = event.getX();
                mFingY = event.getY();
                updateData();
                break;
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    if (mIndex == -1)
                        mListener.onCancel();
                    else
                        mListener.onItemSelected(mIndex);
                }
                invalidate();
                setVisibility(GONE);
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * Draws every item in order, but paints the active item last so
     * its scale-up isn't clipped by neighbours.
     */
    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < mSubIndexes.length; i++) {
            if (i != mIndex)
                drawItem(i, canvas);
        }
        if (mIndex != -1) {
            drawItem(mIndex, canvas);
        }
    }


    /**
     * Draws a single picker item at its current polar position
     * around the center, with its size scaled by both the start-up
     * animation scale and the finger-distance multiplier.
     */
    private void drawItem(int i, Canvas canvas) {
        mItems.drawPickerItem(
                Util.xFromAngle(mCenterX, mData[i].distance, mData[i].angle),
                Util.yFromAngle(mCenterY, mData[i].distance, mData[i].angle),
                dimen * mData[i].scale * mData[i].scaleMultiplier,
                mIndex == i, mSubIndexes[i], p, canvas);
    }


    /**
     * Recomputes every item's {@code scaleMultiplier} based on the
     * current finger position.
     * <p>
     * How: for each item, computes the cube root of
     * {@code distance-from-center / distance-to-finger}. Any item
     * whose multiplier exceeds 1.5 is clamped to 1.5 and becomes the
     * active {@link #mIndex}, with {@link OnItemListener#onItemUpdated}
     * fired on transition. After the pass, invalidate() redraws.
     */
    private void updateData() {
        mIndex = -1;
        for (int i = 0; i < mData.length; i++) {
            float x = Util.xFromAngle(mCenterX, mData[i].distance, mData[i].angle),
                    y = Util.yFromAngle(mCenterY, mData[i].distance, mData[i].angle);

            float dist = Util.distance(x, y, mCenterX, mCenterY);

            float size = (float) Math.pow(dist / Util.distance(x, y, mFingX, mFingY), 1.0 / 3);
            if (size >= 1.5f) {
                if (i != mPrevIndex) {
                    mPrevIndex = i;
                    if (mListener != null)
                        mListener.onItemUpdated(mSubIndexes[i]);
                }
                mIndex = i;
                size = 1.5f;
            }
            mData[i].scaleMultiplier = size;
        }
        invalidate();
    }


    /**
     * Activation entry point, called by the parent picker's
     * long-press handler.
     * <p>
     * How: anchors the center on the finger, builds one
     * {@link ItemData} per sub-index at evenly spaced angles along
     * the arc returned by {@link #getFixedAngle(float, float)},
     * populates the initial state via {@link #updateData()}, and
     * kicks off the per-item scale-up animation.
     *
     * @param startX     finger X when the long-press fired
     * @param startY     finger Y when the long-press fired
     * @param items      the shared item renderer (a single
     *                   {@link PickerItem} drawn multiple times)
     * @param subIndexes ordered list of sub-indexes to display
     */
    public void onStart(float startX, float startY, PickerItem items, int[] subIndexes) {
        mCenterX = startX;
        mCenterY = startY;
        mFingX = startX;
        mFingY = startY;
        mItems = items;
        mSubIndexes = subIndexes;
        mRadius = dimen * 2;
        mData = new ItemData[mSubIndexes.length];

        double startAngle = getFixedAngle(startX, startY);
        double angleDiv = Math.PI / (mSubIndexes.length + 1);
        for (int i = 0; i < mData.length; i++) {
            double a = angleDiv * (i + 1);
            mData[i] = new ItemData(0, 0, startAngle + a);
        }
        updateData();
        animateItems();
    }


    /**
     * Decides which way the arc should fan out, sampling eight
     * candidate directions on the side toward the view center and
     * returning the first that stays inside the view. Falls back to
     * straight up (90 degrees from the finger), mirrored if the
     * finger is on the right half.
     */
    private double getFixedAngle(float startX, float startY) {
        boolean clockwise = startX < getWidth() / 2;
        double div = Math.PI / 2 / 8;

        for (int i = 0; i < 8; i++) {
            double checkA = div * i * (clockwise ? -1 : 1);
            float checkX = Util.xFromAngle(startX, mRadius * 1.1f * (clockwise ? -1 : 1), checkA);
            float checkY = Util.yFromAngle(startY, mRadius * 1.1f * (clockwise ? -1 : 1), checkA);

            if (inside(checkX, checkY)) {
                return checkA;
            }
        }
        return Math.PI / 2 * (clockwise ? -1 : 1);
    }


    /**
     * True if {@code (x, y)} lies within this view's bounds. Used by
     * {@link #getFixedAngle} to probe candidate fan directions.
     */
    private boolean inside(float x, float y) {
        return x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight();
    }


    /**
     * Staggered per-item scale-up animation. Each item gets its own
     * 500 ms {@link OvershootInterpolator}, delayed by 50 ms times
     * its index so the items pop out in sequence. Scale and distance
     * animate together from 0 to their final values.
     */
    private void animateItems() {
        MultiValueAnimator<Integer> anim = MultiValueAnimator.create();

        long singleDur = 500;
        long delay = 50;
        for (int i = 0; i < mData.length; i++) {
            anim.addInterpolator(i, new OvershootInterpolator(), delay * i, singleDur);
        }

        anim.setMultiUpdateListener(new MultiValueAnimator.MultiUpdateListener<Integer>() {
            @Override
            public void onValueUpdate(ValueAnimator animator, float value, Integer index) {
                mData[index].scale = value;
                mData[index].distance = Util.fromFrac(0, mRadius, value);
            }


            @Override
            public void onAllUpdate(ValueAnimator animator, float value) {
                invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }


            @Override
            public void onAnimationEnd(Animator animation) {
                for (int i = 0; i < mData.length; i++) {
                    mData[i].scale = 1;
                }
            }


            @Override
            public void onAnimationCancel(Animator animation) {
            }


            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }


    /**
     * Alternative single-animator variant that grows every item
     * simultaneously instead of staggering them. Not currently
     * referenced but kept as a simpler fallback.
     */
    private void animateBoringItems() {
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1)
                .setDuration(400);
        anim.setInterpolator(new OvershootInterpolator(2f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float frac = (float) animation.getAnimatedValue();
                for (int i = 0; i < mData.length; i++) {
                    mData[i].scale = frac;
                    mData[i].distance = Util.fromFrac(0, dimen * 2, frac);
                }
                invalidate();
            }
        });
        anim.start();
    }


    /**
     * Per-item state bag: the start-up animation scale, the finger
     * proximity scale multiplier, the polar distance from center,
     * and the angle along the arc.
     */
    private class ItemData {
        float scaleMultiplier, distance, scale;
        double angle;


        /**
         * Builds an ItemData with the given initial scale, polar
         * distance, and angle. {@code scaleMultiplier} defaults to 1.
         */
        public ItemData(float scale, float distance, double angle) {
            this.scaleMultiplier = 1;
            this.scale = scale;
            this.distance = distance;
            this.angle = angle;
        }
    }


    /** Registers the listener called during and after the drag. */
    public void setOnItemListener(OnItemListener listener) {
        mListener = listener;
    }


    /**
     * Callback interface bridging release-picker events back into
     * {@link HorizontalPicker}. All three methods fire with the
     * item's sub-index (not its slot index inside the release
     * picker).
     */
    public interface OnItemListener {
        /** Fired every time the active item changes during the drag. */
        void onItemUpdated(int index);


        /** Fired once on release when an item is selected. */
        void onItemSelected(int index);


        /** Fired once on release when no item is selected. */
        void onCancel();
    }
}
