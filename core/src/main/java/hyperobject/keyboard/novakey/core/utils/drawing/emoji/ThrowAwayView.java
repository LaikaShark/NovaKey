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

package hyperobject.keyboard.novakey.core.utils.drawing.emoji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import hyperobject.keyboard.novakey.core.R;

/**
 * A standalone {@link View} that renders a paginated grid of emoji and
 * commits one back via an {@link onClickListener} when tapped. Used
 * outside the main wheel UI (e.g. in setup/demo flows) — hence the
 * name "throw away": it's a lightweight, self-contained picker rather
 * than an element hooked into the Controller/Model pipeline.
 * <p>
 * Layout: {@code maxLine = 7} columns wide; five rows of emoji plus a
 * reserved bottom row that acts as a back/forward pager (left half
 * rewinds, right half advances). {@code index} is the starting offset
 * into {@link Emoji#emojis} for the current page.
 */
public class ThrowAwayView extends View implements View.OnTouchListener {

    private float emojiSize;
    private int maxLine = 7;
    private int index = 0;

    public onClickListener listener;
    private Paint p;


    /**
     * Standard inflatable-view constructor. Installs this view as its
     * own touch listener and preallocates an antialiased paint used
     * for all emoji draws.
     */
    public ThrowAwayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);

        p = new Paint();
        p.setAntiAlias(true);
    }


    /**
     * Pass-through override — defers entirely to
     * {@link View#onMeasure}. Present so subclasses or static
     * analysis find a hook to override, but introduces no custom
     * sizing logic.
     */
    @Override
    public void onMeasure(int w, int h) {
        super.onMeasure(w, h);
    }


    /**
     * Paints one page of emoji.
     * <p>
     * How: derives the per-cell size as {@code width / maxLine}, then
     * iterates up to {@code 5 * maxLine} slots starting at
     * {@code index}, stopping early if the emoji list runs out. Each
     * cell draws at its center coordinates with the size looked up
     * from {@code R.dimen.emojiSize}. The grid fills row-major: x
     * advances each step, wrapping to x=0 and incrementing y once x
     * reaches {@code maxLine}.
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = getResources().getDimension(R.dimen.emojiSize);
        emojiSize = getWidth() / maxLine;

        int x = 0;
        int y = 0;


        for (int i = 0; i < 5 * maxLine; i++) {
            if (index + i >= Emoji.emojis.size())
                break;
            Emoji e = Emoji.emojis.get(index + i);
            e.draw(x * emojiSize + (emojiSize / 2), y * emojiSize + (emojiSize / 2), size, p, canvas);
            x++;
            if (x >= maxLine) {
                x = 0;
                y++;
            }
        }

    }


    private boolean isClick = false;


    /**
     * Touch router: DOWN arms a 200ms "is this still a tap?" timer
     * and invalidates so the next frame can reflect any pressed
     * state. UP, if the timer hasn't expired yet, converts the event
     * coordinates to a grid cell — rows below the 5-row threshold
     * are treated as the pager (x<3 is back, otherwise forward) and
     * rows inside the grid fire the listener with the corresponding
     * emoji if one exists at that slot.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startClickTimer();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (isClick) {
                    int x = (int) (event.getX() / getWidth() * maxLine);
                    int y = (int) (event.getY() / emojiSize);

                    if (y >= 5) {
                        if (x < 3)
                            back();
                        else
                            forward();
                    } else if (listener != null) {
                        int i = index + y * maxLine + x;
                        if (i < Emoji.emojis.size())
                            listener.onClik(Emoji.emojis.get(i));
                    }
                }
                isClick = false;
                break;
        }
        return true;
    }


    /**
     * Arms the 200 ms click-vs-drag timer. {@code isClick} is set
     * true on entry and flipped back to false by the timer's
     * {@code onFinish}, so any UP past the 200 ms window is treated
     * as a drag and ignored in {@link #onTouch}.
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
     * Installs the callback fired when the user selects an emoji.
     */
    public void setListener(onClickListener list) {
        listener = list;
    }


    /**
     * Callback interface for emoji selection. Note the original
     * method spelling {@code onClik} (missing 'c') is preserved
     * verbatim to avoid breaking callers.
     */
    public interface onClickListener {
        void onClik(Emoji e);
    }


    /**
     * Pages backwards by {@code 6 * maxLine} slots (one full screen
     * plus the pager row), clamping to 0. Invalidates so the next
     * frame redraws the new page.
     */
    private void back() {
        index -= 6 * maxLine;
        if (index < 0)
            index = 0;
        invalidate();
    }


    /**
     * Pages forward by {@code 6 * maxLine} slots, clamping to the
     * last-valid emoji index rather than past the end. Invalidates
     * to trigger a redraw.
     */
    private void forward() {
        index += 6 * maxLine;
        if (index >= Emoji.emojis.size()) {
            index = Emoji.emojis.size() - 1;
        }
        invalidate();

    }
}
