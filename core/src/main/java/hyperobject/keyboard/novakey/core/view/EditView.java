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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.controller.Gun;
import hyperobject.keyboard.novakey.core.actions.SetEditingAction;
import hyperobject.keyboard.novakey.core.FloatingButton;
import hyperobject.keyboard.novakey.core.IconView;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.Themeable;

/**
 * The "resize keyboard" input view the IME swaps in when the user enters
 * edit mode. Hosts a {@link NovaKeyEditView} (which handles the drag /
 * pinch gestures and draws a live preview), three {@link FloatingButton}s
 * (cancel / reset / accept), and a {@link SeekBar} with a reset icon that
 * together control the ratio between the outer and inner radii.
 * <p>
 * The cancel/accept buttons fire a {@link SetEditingAction} through the
 * {@link Gun} to ask the controller to leave edit mode, after optionally
 * persisting the new dimensions via
 * {@link NovaKeyEditView#saveDimens()}.
 */
public class EditView extends RelativeLayout implements Themeable {

    private final NovaKeyEditView mResizeView;
    private final FloatingButton mCancel, mRefresh, mAccept;
    private final IconView mResetSr;
    private final SeekBar mSeekBar;
    private final int MIN = 20, MAX = 35, DEFAULT = 3;
    private final Gun mGun;
    private final MainDimensions mDimens;


    /**
     * Inflates {@code R.layout.edit_view_layout} into this
     * {@link RelativeLayout}, wires every child (resize view, floating
     * buttons, seekbar, reset icon) to its listener, and seeds the
     * seekbar from the currently-saved inner/outer radius ratio.
     * <p>
     * The seekbar models the ratio "outer / inner" in tenths, inverted
     * so the slider's right end corresponds to a larger inner circle —
     * {@link #srToProgress(float)} handles the conversion both ways.
     *
     * @param context host context
     * @param gun     action firing channel used to exit edit mode
     * @param dimens  the live main dimensions used to lock this view's
     *                measured size
     */
    public EditView(Context context, Gun gun, MainDimensions dimens) {
        super(context);
        mGun = gun;
        mDimens = dimens;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.edit_view_layout, this, true);

        mResizeView = (NovaKeyEditView) findViewById(R.id.resize_view);

        mCancel = (FloatingButton) findViewById(R.id.cancel);
        mCancel.setIcon(Icons.get("clear"));

        mRefresh = (FloatingButton) findViewById(R.id.refresh);
        mRefresh.setIcon(Icons.get("refresh"));

        mAccept = (FloatingButton) findViewById(R.id.accept);
        mAccept.setIcon(Icons.get("check"));

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setMax(MAX - MIN);

        float sr = mResizeView.getRadius() / mResizeView.getSmallRadius();
        mSeekBar.setProgress(srToProgress(sr));
        mResizeView.setSmallRadius(sr);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float sr = ((MIN + MAX) - (progress + MIN)) / 10f;
                mResizeView.setSmallRadius(sr);
                mResizeView.invalidate();
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mResetSr = (IconView) findViewById(R.id.reset_sr);
        mResetSr.setIcon(Icons.get("refresh"));
        mResetSr.setSize(.8f);
        mResetSr.setClickListener(() -> mSeekBar.setProgress(srToProgress(DEFAULT)));

        mCancel.setOnClickListener(v -> onCancel());
        mRefresh.setOnClickListener(v -> onRefresh());
        mAccept.setOnClickListener(v -> onSave());
    }


    /**
     * Propagates the theme to every child that knows how to be themed:
     * the resize preview, the three floating buttons, the reset icon,
     * and the seekbar's background, progress and thumb drawables (tinted
     * via {@link PorterDuff} color filters).
     *
     * @param theme theme to apply; must not be {@code null}
     * @throws IllegalArgumentException if {@code theme} is null
     */
    public void setTheme(MasterTheme theme) {
        mResizeView.setTheme(theme);

        mCancel.setTheme(theme);
        mRefresh.setTheme(theme);
        mAccept.setTheme(theme);

        mResetSr.setColor(theme.getContrastColor());

        Drawable d = mSeekBar.getBackground();
        if (d != null)
            d.setColorFilter(theme.getPrimaryColor(), PorterDuff.Mode.MULTIPLY);
        d = mSeekBar.getProgressDrawable();
        if (d != null)
            d.setColorFilter(theme.getContrastColor(), PorterDuff.Mode.MULTIPLY);
        d = mSeekBar.getThumb();
        d.setColorFilter(theme.getContrastColor(), PorterDuff.Mode.SRC_ATOP);
    }


    /**
     * Cancel button handler: fires a {@code SetEditingAction(false)}
     * without touching the dimensions, so any in-progress resize is
     * discarded.
     */
    private void onCancel() {
        mGun.fire(new SetEditingAction(false));
    }


    /**
     * Refresh button handler: resets the preview to the default radius /
     * position via {@link NovaKeyEditView#resetDimens()} and snaps the
     * seekbar back to the {@link #DEFAULT} ratio.
     */
    private void onRefresh() {
        mResizeView.resetDimens();
        mSeekBar.setProgress(srToProgress(DEFAULT));
    }


    /**
     * Accept button handler: persists the preview's current dimensions
     * via {@link NovaKeyEditView#saveDimens()}, then fires a
     * {@code SetEditingAction(false)} to leave edit mode.
     */
    private void onSave() {
        mResizeView.saveDimens();
        mGun.fire(new SetEditingAction(false));
    }


    /**
     * Maps a radius-ratio {@code sr} (outer / inner) to the inverted
     * seekbar integer space: multiplies by 10 to hit the tenths
     * resolution used by the seekbar, then subtracts from {@link #MAX}
     * so sliding right yields a larger inner circle.
     */
    private int srToProgress(float sr) {
        sr *= 10;
        return (int) (MAX - sr);
    }


    /**
     * The IME framework adds the input view to its {@code mInputFrame}, which
     * is a {@code wrap_content} FrameLayout. A {@code match_parent} child
     * inside a {@code wrap_content} parent gets {@code MeasureSpec.UNSPECIFIED}
     * with a size of 0, so the default RelativeLayout measure pass would
     * collapse the edit UI to nothing and the keyboard would vanish as soon
     * as edit mode engaged.
     *
     * Force concrete dimensions matching the current keyboard (plus the
     * Android 15+ bottom navbar inset) so the input view reports a real
     * size regardless of the parent spec.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = mDimens.getWidth();
        int h = mDimens.getHeight() + MainView.bottomNavInset(this);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }


    // Insets can arrive after the first measure pass; re-measure when they
    // change so the reserved navbar area tracks the live inset.
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        requestLayout();
        return super.onApplyWindowInsets(insets);
    }
}
