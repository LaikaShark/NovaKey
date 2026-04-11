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
import android.view.ViewGroup;

/**
 * Thin {@link ViewGroup} wrapper that hosts a single {@link MainView}
 * child. Acts as the root container the IME service can hand back to
 * the framework, giving the keyboard a layoutable parent without forcing
 * {@link MainView} to inherit from {@link ViewGroup} itself.
 */
public class NovaKeyViewGroup extends ViewGroup {

    private MainView mView;


    /**
     * Creates the view group and its single {@link MainView} child,
     * attaching the child immediately so the group is ready to measure.
     */
    public NovaKeyViewGroup(Context context) {
        super(context);
        this.mView = new MainView(context);
        this.addView(mView);
    }


    /**
     * Lays the child {@link MainView} out at the same bounds as this
     * group — no internal offsets, the child fills the container.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mView.layout(l, t, r, b);
    }
}
