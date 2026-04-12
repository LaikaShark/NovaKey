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

package hyperobject.keyboard.novakey.tutorial;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.utils.Util;
import hyperobject.keyboard.novakey.core.utils.drawing.Draw;
import hyperobject.keyboard.novakey.core.utils.drawing.Font;

/**
 * Custom view that hosts the tutorial's task panel: a Back/Next button
 * row along the top, a dot "progress map" between them, and a
 * horizontally-scrolling strip of task descriptions below. The strip is
 * rendered by translating each task's text by its index times the view
 * width plus a shared {@link #currX} offset; {@link #beginAnimation}
 * animates {@code currX} to slide between tasks.
 * <p>
 * Click regions are hard-coded to the top strip
 * ({@code event.getY() <= dimen * 1.5f}), split into left half (Back)
 * and right half (Next). The Next button relabels to "Done" on the
 * final task and, when pressed there, invokes {@link OnFinishListener}.
 * <p>
 * Note: uses a no-arg {@code new Handler()}-style pattern indirectly
 * through {@link ValueAnimator}; no actual {@code new Handler()} leak
 * lives here.
 */
public class TaskView extends View {

    private float dimen;
    private Paint p = new Paint();

    private TextButton back, forward;

    private int index = 0;
    private float currX = 0;

    private OnIndexChangeListener listener;
    private OnFinishListener mOnFinishList;

    private ArrayList<Task> tasks;


    /**
     * Inflation constructor. Reads the text and button dimensions from
     * resources, builds placeholder Back/Next buttons (actual positions
     * get recomputed in {@link #onMeasure}), and installs a touch
     * listener that routes top-strip DOWN events into
     * {@link #back()}/{@link #forward()}.
     *
     * @param context application context
     * @param attrs   XML attributes
     */
    public TaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dimen = getResources().getDimension(R.dimen.tut_text_size);

        float dp = getResources().getDimension(R.dimen.tut_btn_dimen);
        back = new TextButton("Back", dp, dimen / 2);
        forward = new TextButton("Next", getWidth() - dp, dimen / 2);
        updateButtonStates();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getY() <= dimen * 1.5f) {
                        if (event.getX() <= getWidth() / 2) {
                            if (back.state != DISABLED && back.state != INVISIBLE)//back pressed
                                back();
                        } else {
                            if (forward.state != DISABLED && forward.state != INVISIBLE) {//forward pressed
                                if (index < tasks.size() - 1)
                                    forward();
                                else {
                                    if (mOnFinishList != null)
                                        mOnFinishList.onFinish();
                                }
                            }
                        }
                    }
                    invalidate();
                }
                return true;
            }
        });
    }


    /**
     * Disables the Next button. Called when the user moves forward to
     * a task that isn't yet complete — Next re-enables once the task's
     * {@link Task#isComplete(String)} returns true.
     */
    public void disableNext() {
        if (forward != null) {
            forward.state = DISABLED;
            invalidate();
        }
    }


    /** Re-enables the Next button once the current task is satisfied. */
    public void enableNext() {
        if (forward != null) {
            forward.state = ENABLED;
            invalidate();
        }
    }


    /**
     * Polls the current task's {@link Task#isComplete(String)}. Safe
     * to call before tasks are loaded; returns {@code false} in that
     * case.
     *
     * @param test current editor contents
     * @return whatever the current task reports
     */
    public boolean isComplete(String test) {
        if (tasks != null) {
            return tasks.get(index).isComplete(test);
        }
        return false;
    }


    /**
     * Installs the tutorial task list. Expected to be called once
     * after inflation, before the view is drawn.
     */
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }


    /** Returns the index of the currently active task. */
    public int getIndex() {
        return index;
    }


    /**
     * Measure pass: fixes the height at seven text lines
     * ({@code dimen * 7}) and repositions the Back/Next buttons
     * (constructed as placeholders before {@link #getWidth()} was
     * valid) by mutating their fields in place rather than allocating
     * a fresh pair on every measure cycle.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, (int) dimen * 7);
        float dp = getResources().getDimension(R.dimen.tut_btn_dimen);
        back.x = dp;
        back.y = dimen;
        forward.x = getWidth() - dp;
        forward.y = dimen;
        updateButtonStates();
    }


    /**
     * Paint pass: draws every task's description at its horizontal
     * offset (so sliding {@link #currX} pans the strip), then overlays
     * the progress dots, Back button, and Next button.
     */
    @Override
    public void onDraw(Canvas canvas) {
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(Font.SANS_SERIF_LIGHT);
        p.setTextSize(dimen * .8f);
        for (int i = 0; i < tasks.size(); i++) {
            drawTask(i, currX + getWidth() * i, canvas);
        }
        drawMap(canvas);
        back.draw(canvas);
        forward.draw(canvas);
    }


    /**
     * Renders one task's instruction text, word-wrapped via
     * {@link Util#toMultiline} to fit either the view width minus
     * padding or a configured minimum, whichever is smaller.
     *
     * @param index  which task to draw
     * @param x      left position of the task card
     * @param canvas target canvas
     */
    private void drawTask(int index, float x, Canvas canvas) {
        float maxLength = Math.min(getWidth() - getPaddingRight() - getPaddingRight(),
                getResources().getDimension(R.dimen.tut_min_text_length));
        String str = Util.toMultiline(tasks.get(index).mainText(), p, maxLength);
        p.setColor(0xFFF0F0F0);
        Draw.text(str, x + getWidth() / 2, getHeight() - dimen * 2.5f, p, canvas);
    }


    /**
     * Draws the small progress indicator row at the top: one faint
     * circle per task, plus one bright circle positioned according to
     * {@link #currX} so it slides between task dots during the
     * transition animation.
     */
    private void drawMap(Canvas canvas) {
        int count = tasks.size();
        float mapHeight = dimen / 2,
                length = mapHeight * count,
                mapX = length / (getWidth() * tasks.size()) * currX;
        mapX *= -1;
        p.setColor(0x50ffffff);

        for (int i = 0; i < count; i++) {
            canvas.drawCircle(getWidth() / 2 - length / 2 + (i * mapHeight + mapHeight / 2),
                    dimen / 2, mapHeight / 4, p);
        }
        p.setColor(0xFFffffff);
        canvas.drawCircle((getWidth() / 2 - length / 2 + (mapHeight / 2)) + mapX,
                dimen / 2, mapHeight / 4, p);
    }


    private final static int ENABLED = 0, DISABLED = 1, INVISIBLE = 2;

    /**
     * Tiny inner value class for the Back/Next buttons. Holds a text
     * label, position, and one of the {@link #ENABLED}/{@link #DISABLED}/
     * {@link #INVISIBLE} state constants. The enclosing
     * {@link TaskView} paints them directly in {@link #onDraw(Canvas)}.
     */
    private class TextButton {
        public String text;
        private float x, y;
        public int state = ENABLED;


        /**
         * Builds a button with the given label and (x, y) position.
         * State defaults to ENABLED.
         */
        TextButton(String text, float x, float y) {
            this.x = x;
            this.y = y;
            this.text = text;
        }


        /**
         * Paints this button unless INVISIBLE. DISABLED buttons use
         * the dimmed grey color; ENABLED ones use the full white.
         */
        void draw(Canvas canvas) {
            p.setColor(0xFFF0F0F0);
            if (state != INVISIBLE) {
                if (state == DISABLED)
                    p.setColor(0xFF909090);
                Draw.text(text, x, y, p, canvas);
            }
        }
    }


    /**
     * Starts the horizontal slide animation to task {@code i}.
     * <p>
     * How: animates {@link #currX} from its current value to
     * {@code -i * getWidth()} over 400 ms. The interpolator is chosen
     * based on direction: moving forward uses
     * {@link AnticipateOvershootInterpolator} (a pull-back before the
     * slide), moving back uses plain {@link OvershootInterpolator}.
     * On animation end the actual task index is swapped in and the
     * {@link OnIndexChangeListener} is notified.
     *
     * @param i        destination index
     * @param complete if {@code true} uses the gentler "complete"
     *                 interpolator (used by {@link #back()})
     */
    private void beginAnimation(final int i, boolean complete) {
        final ValueAnimator anim = ValueAnimator.ofFloat(currX, -i * getWidth())
                .setDuration(400);
        anim.setInterpolator(!complete ? new AnticipateOvershootInterpolator(.5f) :
                new OvershootInterpolator(.5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currX = (Float) anim.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }


            @Override
            public void onAnimationEnd(Animator animation) {
                int prev = index;
                index = i;
                updateButtonStates();
                if (listener != null)
                    listener.onNewIndex(index, prev);
                invalidate();
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
     * Back button action: slides one task backward, or resets to task
     * 0 if the user is already on it (which should be unreachable
     * since the Back button is hidden at index 0).
     */
    private void back() {
        if (index > 0)
            beginAnimation(index - 1, true);
        else
            reset();
    }


    /**
     * Next button action: slides one task forward when there is one.
     * The final task's Next click is routed through
     * {@link OnFinishListener#onFinish()} instead.
     */
    private void forward() {
        if (index < tasks.size() - 1)
            beginAnimation(index + 1, false);
    }


    /** Jumps back to the first task (used only by dead {@link #back()} path). */
    private void reset() {
        beginAnimation(0, false);
    }


    /**
     * Recomputes which buttons should show for the current task:
     * Back becomes INVISIBLE on the first task, and Next relabels to
     * "Done" (and forces itself ENABLED) on the last task.
     */
    private void updateButtonStates() {
        if (index == 0)
            back.state = INVISIBLE;
        else
            back.state = ENABLED;

        if (tasks != null && index == tasks.size() - 1) {
            forward.text = "Done";
            forward.state = ENABLED;
        } else
            forward.text = "Next";
    }


    /**
     * Registers a listener called each time the active task index
     * changes. Fires after the slide animation ends, not when it
     * starts.
     */
    public void setOnIndexChangeListener(OnIndexChangeListener listener) {
        this.listener = listener;
    }


    /** Index-change callback interface consumed by {@link TutorialActivity}. */
    public interface OnIndexChangeListener {
        /**
         * Called after a slide animation finishes.
         *
         * @param index new active task index
         * @param prev  previous active task index
         */
        void onNewIndex(int index, int prev);
    }


    /**
     * Registers the listener fired when the user taps "Done" on the
     * final task.
     */
    public void setOnFinishListener(OnFinishListener listener) {
        this.mOnFinishList = listener;
    }


    /** Finish callback interface consumed by {@link TutorialActivity}. */
    public interface OnFinishListener {
        /** Called when the user presses Done on the final task. */
        void onFinish();
    }
}
