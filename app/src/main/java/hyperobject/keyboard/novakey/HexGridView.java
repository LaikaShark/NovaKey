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

package hyperobject.keyboard.novakey;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * Custom view that renders a 2D {@link Drawable} grid in a staggered
 * "hex" layout (odd rows offset horizontally by half a cell) and lets
 * the user pan across it with a one-finger scroll gesture.
 * <p>
 * Only a 9x9 window of the grid is drawn at a time, centered on the
 * logical {@code (currX, currY)} cell. Scrolling accumulates sub-cell
 * offsets in {@code offX}/{@code offY} and promotes them to whole-cell
 * moves of {@code currX}/{@code currY} once they exceed one {@code dimen}.
 * <p>
 * Used by {@link EmojiSettingActivity} as a preview of the hex-packed
 * emoji layout; not a user-facing widget.
 */
public class HexGridView extends View implements GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;

    private Drawable[][] mGrid;
    private float dimen;
    private int currX = 0, currY = 0;
    private float offX = 0, offY = 0;
    private Paint p;


    /** Single-arg code path; delegates to the (context, attrs) constructor. */
    public HexGridView(Context context) {
        this(context, null);
    }


    /** XML-inflation entry point; delegates to the three-arg constructor. */
    public HexGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    /**
     * Full constructor. Reads the per-cell dimension from resources,
     * primes the anti-aliased paint and draws-to-bitmap cache, and
     * installs a {@link GestureDetectorCompat} routed to this view's
     * {@link GestureDetector.OnGestureListener} callbacks.
     */
    public HexGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dimen = getResources().getDimension(R.dimen.hex_grid_dimen);

        p = new Paint();
        p.setAntiAlias(true);

        setDrawingCacheEnabled(true);

        mDetector = new GestureDetectorCompat(context, this);
    }


    /**
     * Installs the drawable grid to render. Must be called before the
     * first {@link #onDraw(Canvas)} or drawing will throw.
     */
    public void setGrid(Drawable[][] grid) {
        this.mGrid = grid;
    }


    /**
     * Draws a 9x9 window into {@link #mGrid}, centered on
     * {@code (currX, currY)}. Each cell is positioned at
     * {@code (x * dimen + dimen/2 * (odd ? 2 : 1) - offX,
     *        y * dimen + dimen/2 - offY)} where {@code odd} is the
     * parity of the grid row — odd rows shift right by half a cell so
     * neighbouring rows interlock.
     *
     * @throws IllegalStateException if {@link #setGrid} was never called
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGrid == null)
            throw new IllegalStateException("Grid has not been set");

        for (int x = -1; currX + x < 9; x++) {

            int actualX = currX + x;
            if (actualX >= 0 && actualX < mGrid.length) {
                for (int y = 0; y < 9; y++) {

                    int actualY = currY + y;
                    if (actualY >= 0 && actualY < mGrid[actualX].length) {
                        Drawable d = mGrid[actualX][actualY];
                        if (d != null) {
                            boolean odd = actualY % 2 != 0;
                            d.draw(x * dimen + (dimen / 2) * (odd ? 2 : 1) - offX,
                                    y * dimen + (dimen / 2) - offY, dimen * .6f, p, canvas);
                        }
                    }
                }
            }
        }
    }


    /**
     * Accumulates a scroll delta and promotes any whole-cell movement
     * into {@code currX}/{@code currY}. The while-loops handle the case
     * where a single gesture crosses multiple cells, keeping the sub-cell
     * remainder in {@code offX}/{@code offY}.
     */
    private void addScrollDist(float deltaX, float deltaY) {
        offX += deltaX;
        while (offX >= dimen) {
            currX++;
            offX -= dimen;
        }
        while (offX < 0) {
            currX--;
            offX += dimen;
        }

        offY += deltaY;
        while (offY >= dimen) {
            currY++;
            offY -= dimen;
        }
        while (offY < 0) {
            currY--;
            offY += dimen;
        }
    }


    /**
     * Forwards all raw touches to the gesture detector, which then calls
     * back into {@link #onScroll(MotionEvent, MotionEvent, float, float)}
     * for pan handling.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }


    /** Claims the initial DOWN event so the gesture detector can track it. */
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }


    /** Unused — no visual "pressed" feedback for this debug view. */
    @Override
    public void onShowPress(MotionEvent e) {

    }


    /** Taps do not consume anything; only scrolls matter. */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


    /**
     * Pan handler: forwards the scroll distance into
     * {@link #addScrollDist(float, float)} and requests a redraw.
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        addScrollDist(distanceX, distanceY);
        invalidate();
        return true;
    }


    /** Unused — no long-press behaviour on this debug view. */
    @Override
    public void onLongPress(MotionEvent e) {

    }


    /** Flings are ignored; only incremental scrolls move the grid. */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
