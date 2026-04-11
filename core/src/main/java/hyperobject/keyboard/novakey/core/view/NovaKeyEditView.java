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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.loaders.Loader;
import hyperobject.keyboard.novakey.core.model.loaders.MainDimensionsLoader;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.Themeable;


/**
 * Live preview surface hosted by {@link EditView} for the "resize
 * keyboard" mode. Loads the currently-saved dimensions on construction,
 * lets the user drag the wheel around and pinch to resize it, then
 * writes the result back through the {@link MainDimensionsLoader} when
 * {@link #saveDimens()} is called.
 * <p>
 * Acts as its own {@link View.OnTouchListener} instead of overriding
 * {@link View#onTouchEvent} so callers can re-route touches without
 * subclassing. The draw pass just uses the model's background and board
 * themes directly — there are no keys or overlays in edit mode.
 * <p>
 * Note: the internal {@code smallRadius} field holds the ratio
 * {@code outerRadius / innerRadius} (a divisor), not the inner radius
 * in pixels — {@link #saveDimens()} converts before handing it to
 * {@link MainDimensions}.
 */
public class NovaKeyEditView extends View implements View.OnTouchListener, Themeable {

    private MasterTheme mTheme;


    //Dimensions
    private final int screenWidth, screenHeight;//in pixels
    private final Loader<MainDimensions> mMainDimensionsLoader;
    private final MainDimensions mDimens;
    private int viewWidth, viewHeight;
    private float centerX, centerY, padding;
    private int height;
    private float radius, smallRadius;
    //Drawing
    private Paint p;

    //editing
    private boolean moving = false, resizing = false;
    private float moveX, moveY;//moving
    private float resizeDist, oldRadius;//resizing


    /** Simple constructor; delegates to the (Context, AttributeSet) form. */
    public NovaKeyEditView(Context context) {
        this(context, null);
    }


    /** XML-inflation constructor; delegates to the three-arg form. */
    public NovaKeyEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    /**
     * Full constructor: sets up the antialiased paint, registers this
     * view as its own touch listener, captures the display metrics, and
     * loads the saved {@link MainDimensions} through a fresh
     * {@link MainDimensionsLoader} so the preview starts at the same
     * size the live keyboard currently has.
     */
    public NovaKeyEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        //set Listener
        setOnTouchListener(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        mMainDimensionsLoader = new MainDimensionsLoader(context);
        mDimens = mMainDimensionsLoader.load();
    }


    /**
     * Installs the theme used by {@link #onDraw} to render the preview's
     * background and circular board.
     */
    @Override
    public void setTheme(MasterTheme theme) {
        mTheme = theme;
    }


    /**
     * Measure pass: pulls radius, ratio, horizontal center and padding
     * from the saved {@link MainDimensions}, then forces the view to
     * occupy the entire screen ({@code screenWidth x screenHeight}).
     * After {@code setMeasuredDimension} it rereads {@code viewWidth} /
     * {@code viewHeight} from the incoming specs to recover the actual
     * laid-out size (which can be smaller than the screen when the IME
     * window has a title bar). Finally, {@code centerY} is reconstructed
     * so the preview aligns with where the live keyboard would sit
     * inside that window.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        radius = mDimens.getRadius();
        smallRadius = mDimens.getRadius() / mDimens.getSmallRadius();

        //sets location to saved size
        centerX = mDimens.getX();
        padding = mDimens.getPadding();

        //set view Dimens
        viewWidth = screenWidth;
        viewHeight = screenHeight;
        //centerY will be set after method
        setMeasuredDimension(viewWidth, viewHeight);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);//fixes title bar

        height = mDimens.getHeight();

        // centerY needs to be overriden to actual centerY for touch logic
        centerY = viewHeight - height + mDimens.getY();
    }


    /**
     * Restores the preview geometry to a neutral starting point:
     * default radius from {@code R.dimen.default_radius}, horizontally
     * centered, vertically anchored so the bottom of the wheel touches
     * the bottom of the view, ratio 3 (so the inner radius is one third
     * of the outer), and triggers a redraw.
     */
    public void resetDimens() {
        radius = getResources().getDimension(R.dimen.default_radius);
        centerX = viewWidth / 2;
        centerY = viewHeight - radius;
        smallRadius = 3;
        height = (int) (radius * 2 + padding);
        invalidate();
    }


    /**
     * Writes the preview's current geometry back into {@link MainDimensions}
     * and persists via the loader so the live keyboard picks up the new
     * size on its next relayout. Converts the internal ratio into the
     * pixel-valued inner radius the renderer expects, computes a height
     * that accounts for the wheel's current vertical offset, and guards
     * against a zero ratio by falling back to the default "third"
     * divisor.
     */
    public void saveDimens() {
        mDimens.setRadius(radius);
        // The local `smallRadius` field holds the divisor used by the resize
        // UI (3 = "third the radius"); MainDimensions stores the resolved
        // pixel value to match the renderer's convention. Convert before
        // handing it off so MainDimensionsLoader.save() sees the same unit
        // as the rest of the codebase.
        mDimens.setSmallRadius(smallRadius > 0 ? radius / smallRadius : radius / 3f);
        mDimens.setX(centerX);
        mDimens.setY(radius + padding);
        mDimens.setHeight((int) (viewHeight - (centerY - radius - padding)));
        mMainDimensionsLoader.save(mDimens);
    }


    /** Returns the current outer radius in pixels. */
    public float getRadius() {
        return radius;
    }


    /**
     * Returns the current {@code outer / inner} radius ratio. Note this
     * is a divisor, not the inner radius in pixels.
     */
    public float getSmallRadius() {
        return smallRadius;
    }


    /**
     * Sets the {@code outer / inner} radius ratio live — called by the
     * seekbar listener on every tick so the preview tracks the slider
     * without waiting for the user to commit.
     */
    public void setSmallRadius(float sr) {
        smallRadius = sr;
    }


    /**
     * Draws the preview: first the themed background across the whole
     * measurable area (anchored at {@code centerY - radius - padding} to
     * land just above the wheel's top edge), then the circular board on
     * top. Keys and overlays are deliberately absent in edit mode.
     */
    @Override
    public void onDraw(Canvas canvas) {
        mTheme.getBackgroundTheme()
                .drawBackground(0, (centerY - radius - padding), viewWidth, viewHeight,
                        centerX, centerY,
                        radius, radius / smallRadius, canvas);

        mTheme.getBoardTheme().drawBoard(centerX, centerY, radius, radius / smallRadius, canvas);
    }


    /**
     * Touch dispatch for drag and pinch-resize gestures.
     * <ul>
     *     <li>{@code ACTION_DOWN} inside the current wheel starts a
     *     drag, capturing the touch-to-center offset.</li>
     *     <li>{@code ACTION_MOVE} updates either the center (drag) or
     *     the radius (pinch using the two-pointer distance delta), then
     *     runs a batch of clamps: keep the wheel inside the view, snap
     *     the center to the horizontal midline when within
     *     {@code center_threshold}, cap the radius so the wheel fits on
     *     both axes, and enforce {@code min_radius}.</li>
     *     <li>{@code ACTION_UP} clears both gesture flags.</li>
     *     <li>{@code ACTION_POINTER_DOWN} with a second pointer below
     *     the wheel's top promotes the gesture from drag to pinch,
     *     stashing the starting radius and finger distance.</li>
     *     <li>{@code ACTION_POINTER_UP} cancels both modes — safer than
     *     trying to revert to a single-finger drag mid-gesture.</li>
     * </ul>
     * Always returns {@code true} to claim the gesture for the lifetime
     * of the view.
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float currX = event.getX(0), currY = event.getY(0);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (Util.distance(currX, currY, centerX, centerY) <= radius) {
                    moveX = currX - centerX;
                    moveY = currY - centerY;
                    moving = true;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if (moving || resizing) {
                    if (moving) {
                        centerX = currX - moveX;
                        centerY = currY - moveY;
                    } else if (resizing && event.getPointerCount() > 1) {
                        radius = oldRadius + (Util.distance(currX, currY, event.getX(1), event.getY(1)) - resizeDist) / 2;
                    }
                    //Checks Edit Bounds
                    if (centerX + radius > viewWidth)
                        centerX = viewWidth - radius;
                    if (centerX - radius < 0)
                        centerX = radius;
                    if (centerY + radius > viewHeight)
                        centerY = viewHeight - radius;
                    if (centerY - radius < 0)
                        centerY = radius;

                    //center
                    if (Math.abs(centerX - viewWidth / 2) < getResources().getDimension(R.dimen.center_threshold))
                        centerX = viewWidth / 2;

                    // max radius
                    if (radius * 2 > viewWidth) {
                        radius = viewWidth / 2;
                        centerX = radius;
                    }
                    if (radius * 2 > viewHeight) {
                        radius = viewHeight / 2;
                        centerY = radius;
                    }
                    //min radius
                    if (radius < getResources().getDimension(R.dimen.min_radius))
                        radius = getResources().getDimension(R.dimen.min_radius);


                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                moving = false;
                resizing = false;
                break;

            //for multitouch
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() > 1 && event.getY(1) >= centerY - radius) {
                    oldRadius = radius;
                    resizeDist = Util.distance(currX, currY, event.getX(1), event.getY(1));
                    moving = false;
                    resizing = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                moving = false;
                resizing = false;
                break;
        }
        return true;
    }
}
