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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.utils.PickerItem;
import hyperobject.keyboard.novakey.widgets.ObservableHorizontalScrollView;

/**
 * Abstract base for the style picker widgets. Renders a row of
 * {@link PickerItem}s laid out horizontally inside an
 * {@link ObservableHorizontalScrollView}, and translates touches into
 * three distinct outcomes:
 * <ul>
 *   <li>A short tap selects the tapped item and fires
 *       {@link OnItemSelectedListener#onItemSelected(PickerItem, int)}.</li>
 *   <li>A long press triggers {@link #onItemLongPress(int, float, float)},
 *       which subclasses can use to open the linked {@link ReleasePicker}
 *       for sub-index selection.</li>
 *   <li>Scroll events on the wrapping horizontal scroll view cancel
 *       both the long-press timer and any open release picker.</li>
 * </ul>
 * Subclasses supply the concrete item set through
 * {@link #initializeItems()} (called from this constructor) and
 * implement {@link #onItemLongPress(int, float, float)} to wire long
 * presses to whatever secondary picker they want.
 */
public abstract class HorizontalPicker extends View implements View.OnTouchListener {

    private float dimen;
    private Paint p;
    private boolean isClick = true;

    private CountDownTimer mLongPressTimer;
    private float pickerX, pickerY;
    private int mIndex = 0, mTempIndex = 0;

    private ObservableHorizontalScrollView mScrollView;

    protected boolean mOnReleasePicker = false;

    protected OnItemSelectedListener mListener;
    protected PickerItem[] mItems;

    protected int[] mSubIndexes;

    protected ReleasePicker mReleasePicker;


    /**
     * XML-inflation constructor. Invokes {@link #initializeItems()}
     * (which subclasses override), seeds all sub-indexes to zero,
     * caches the configured item dimension, switches the view to
     * software rendering (so SVG drawables render correctly), and
     * installs this instance as its own touch listener.
     */
    public HorizontalPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mItems = initializeItems();
        mSubIndexes = new int[mItems.length];
        for (int i = 0; i < mSubIndexes.length; i++) {
            mSubIndexes[i] = 0;
        }

        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        dimen = context.getResources().getDimension(R.dimen.picker_dimen);
        //removes hardware acceleration to allow svgs
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setOnTouchListener(this);
    }


    /**
     * Measure pass: captures the parent {@link ObservableHorizontalScrollView}
     * and attaches a listener that dismisses the release picker and
     * cancels the long-press timer whenever the user scrolls. Then
     * sizes this view to {@code dimen * itemCount} wide by {@code dimen}
     * tall, or defers to the superclass if items haven't been
     * initialized yet.
     */
    @Override
    public void onMeasure(int w, int h) {
        mScrollView = (ObservableHorizontalScrollView) getParent();
        mScrollView.setOnScrollListener(new ObservableHorizontalScrollView.OnScrollListener() {
            @Override
            public void onScrollChanged(View view, int x, int y, int oldx, int oldy) {
                //Safe lock
                if (mReleasePicker != null)
                    mReleasePicker.setVisibility(GONE);
                cancelLongPressTimer();
            }
        });
        if (mItems == null)
            super.onMeasure(w, h);
        else
            setMeasuredDimension((int) dimen * mItems.length, (int) dimen);
    }


    /**
     * Touch handler for the picker row.
     * <p>
     * How: on DOWN, starts a click-window timer and (if a release
     * picker is attached) a long-press timer for the pressed item.
     * While the release picker is active, subsequent MOVE/UP events
     * are translated into its coordinate space and forwarded to it.
     * Otherwise, MOVE outside the view cancels the long-press; UP
     * within the click window selects the tapped item and fires the
     * listener.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ScrollView sView = ((ScrollView) getParent().getParent().getParent());
        pickerX = event.getX() - mScrollView.getScrollX();
        pickerY = event.getY() + mScrollView.getY()
                + sView.getY() - sView.getScrollY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startClickTimer();
                if (mReleasePicker != null) {
                    startLongPressTimer((int) (event.getX() / dimen));
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mReleasePicker != null && mOnReleasePicker) {
                    event.setLocation(pickerX, pickerY);
                    return mReleasePicker.onTouchEvent(event);
                } else {
                    if (event.getY() < 0 || event.getY() > getHeight())
                        cancelLongPressTimer();
                }
                break;
            case MotionEvent.ACTION_UP:
                cancelLongPressTimer();
                if (isClick) {
                    int selectedIndex = (int) (event.getX() / dimen);
                    mIndex = selectedIndex;

                    if (mListener != null)
                        mListener.onItemSelected(mItems[selectedIndex], mSubIndexes[selectedIndex]);
                    invalidate();
                }
                if (mReleasePicker != null && mOnReleasePicker) {
                    event.setLocation(pickerX, pickerY);
                    return mReleasePicker.onTouchEvent(event);
                }
                mOnReleasePicker = false;
                break;
        }
        return true;
    }


    /**
     * Starts a 200 ms countdown that flips {@link #isClick} to
     * {@code false}. The ACTION_UP handler consults this flag to
     * decide whether to treat the gesture as a click or a drag.
     */
    private void startClickTimer() {
        isClick = true;
        new CountDownTimer(200, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
            }


            @Override
            public void onFinish() {
                isClick = false;
            }
        }.start();
    }


    /**
     * Starts a 400 ms long-press countdown. When it fires without
     * being cancelled (by scroll or ACTION_UP), it stores the pressed
     * index in {@link #mTempIndex}, installs the release-picker
     * listener, and dispatches to {@link #onItemLongPress(int, float, float)}.
     */
    private void startLongPressTimer(final int currIndex) {
        mLongPressTimer = new CountDownTimer(400, 400) {
            @Override
            public void onTick(long millisUntilFinished) {
            }


            @Override
            public void onFinish() {
                mTempIndex = currIndex;
                setItemListener();
                onItemLongPress(currIndex, pickerX, pickerY);
            }

        }.start();
    }


    /** Null-safe cancel for {@link #mLongPressTimer}. */
    private void cancelLongPressTimer() {
        if (mLongPressTimer != null) {
            mLongPressTimer.cancel();
        }
    }


    /**
     * Paint pass: asks each {@link PickerItem} to render itself at
     * its slot center, passing along the currently selected item
     * index so the item can highlight itself if chosen.
     */
    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < mItems.length; i++) {
            mItems[i].drawPickerItem(i * dimen + dimen / 2, dimen / 2, dimen, mIndex == i,
                    mSubIndexes[i], p, canvas);
        }
    }


    /**
     * Installs the inner listener that bridges {@link ReleasePicker}
     * selection events back into the picker's own
     * {@link OnItemSelectedListener}:
     * <ul>
     *   <li>onItemUpdated — live update during the drag.</li>
     *   <li>onItemSelected — finalize the selection and commit
     *       {@link #mIndex} to the temp index.</li>
     *   <li>onCancel — refire the previous selection so listeners
     *       always see a definitive state.</li>
     * </ul>
     */
    private void setItemListener() {
        mReleasePicker.setOnItemListener(new ReleasePicker.OnItemListener() {
            @Override
            public void onItemUpdated(int index) {
                if (mListener != null)
                    mListener.onItemSelected(mItems[mTempIndex], index);
                invalidate();
            }


            @Override
            public void onItemSelected(int index) {
                if (mListener != null)
                    mListener.onItemSelected(mItems[mTempIndex], index);
                mIndex = mTempIndex;
                invalidate();
            }


            @Override
            public void onCancel() {
                if (mListener != null)
                    mListener.onItemSelected(mItems[mIndex], mSubIndexes[mIndex]);
                invalidate();
            }
        });
    }


    /**
     * Attaches the release picker the long-press handler should
     * activate. Must be set before the picker is touched or
     * long-press will be a no-op.
     */
    public void setReleasePicker(ReleasePicker releasePicker) {
        mReleasePicker = releasePicker;
    }


    /**
     * Programmatically selects an item and scrolls the parent
     * {@link HorizontalScrollView} so the selection lands roughly
     * centered. The smooth-scroll is posted to the parent's handler
     * to wait for the next layout pass.
     */
    public void setItem(final int index) {
        mIndex = index;
        invalidate();
        final HorizontalScrollView parent = (HorizontalScrollView) getParent();
        parent.post(new Runnable() {
            public void run() {
                parent.smoothScrollTo(getDesiredX(index, parent.getWidth()), 0);
            }
        });
    }


    /**
     * Converts an item index into the horizontal scroll position
     * that centers that item within the visible window, clamping to
     * the scrollable range.
     *
     * @param index item index
     * @param width viewport width
     * @return desired scroll X
     */
    private int getDesiredX(int index, float width) {
        float indexX = index * dimen + dimen / 2;
        indexX -= width / 2;
        if (indexX < 0)
            return 0;
        if (indexX > getWidth() - width)
            return (int) (getWidth() - width);
        return (int) indexX;
    }


    /**
     * Subclass hook: builds the array of {@link PickerItem}s the
     * picker displays. Called once from the constructor, so it must
     * not depend on any subclass state initialized later.
     */
    protected abstract PickerItem[] initializeItems();


    /**
     * Subclass hook fired when an item is long-pressed. Must set
     * {@link #mOnReleasePicker} to {@code true} and start
     * {@link ReleasePicker#onStart(float, float, PickerItem, int[])}
     * if it wants the release picker to take over touches.
     *
     * @param index  long-pressed item index
     * @param startX finger X in picker-local coordinates
     * @param startY finger Y in parent-scroll-view coordinates
     */
    protected abstract void onItemLongPress(int index, float startX, float startY);


    /** Registers the listener called when an item is selected. */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }


    /** Callback fired when the user picks an item (with optional sub-index). */
    public interface OnItemSelectedListener {
        /**
         * @param item     the selected item
         * @param subIndex shade / sub-index within the item (0 for
         *                 items that don't expose sub-indexes)
         */
        void onItemSelected(PickerItem item, int subIndex);
    }

}
