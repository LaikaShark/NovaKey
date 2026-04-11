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
import android.util.AttributeSet;
import android.view.View;

import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.Themeable;

/**
 * Base Android {@link View} for the NovaKey keyboard surface.
 * <p>
 * Owns a {@link Model} and a {@link MasterTheme} and implements the core
 * draw loop: {@link #onDraw} walks {@code model.getElements()} top to
 * bottom, asking each {@link Element} to render itself with the model's
 * current theme. Touch handling, sizing, and inset accounting live in
 * concrete subclasses ({@link MainView} for the live IME,
 * {@link NovaKeyEditView} for the resize UI).
 */
public abstract class NovaKeyView extends View implements Themeable {

    protected Model mModel;
    protected MasterTheme mTheme;


    /** Simple constructor; delegates to the (Context, AttributeSet) form. */
    public NovaKeyView(Context context) {
        this(context, null);
    }


    /** XML-inflation constructor; delegates to the three-arg form. */
    public NovaKeyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    /**
     * Full constructor — just forwards to the Android {@link View}
     * superclass. Model and theme are injected later via
     * {@link #setModel} and {@link #setTheme}.
     */
    public NovaKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Installs the model whose elements this view will draw. Must be
     * called before the first {@link #onDraw} or the draw pass will NPE
     * on {@code mModel.getElements()}.
     */
    public void setModel(Model model) {
        mModel = model;
    }


    /**
     * Caches the current theme. The draw pass reads the theme straight
     * off the model rather than this field, so this setter is mostly for
     * {@link Themeable} contract uniformity.
     */
    @Override
    public void setTheme(MasterTheme theme) {
        mTheme = theme;
    }


    /**
     * Core draw pass: defers to the Android superclass, then iterates
     * the model's element list in order and asks each to draw itself
     * with the model's current theme on the supplied canvas. Returns
     * immediately if the model has no elements yet (first frame between
     * service creation and loader completion).
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mModel.getElements() == null)
            return;
        for (Element e : mModel.getElements()) {
            e.draw(mModel, mModel.getTheme(), canvas);
        }
    }

}
