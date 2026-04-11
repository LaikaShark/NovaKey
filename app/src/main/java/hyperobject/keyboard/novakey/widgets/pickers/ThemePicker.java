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
import android.util.AttributeSet;

import hyperobject.keyboard.novakey.core.utils.PickerItem;
import hyperobject.keyboard.novakey.core.model.factories.ThemeFactory;
import hyperobject.keyboard.novakey.core.view.themes.BaseMasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;

/**
 * {@link HorizontalPicker} concrete subclass that picks a
 * {@link BoardTheme}. The visible item set comes from
 * {@link ThemeFactory#BOARDS}; each entry is reparented onto a shared
 * {@link BaseMasterTheme} so it can render itself in the picker row
 * without disturbing the editor's live theme.
 * <p>
 * Unlike {@link ColorPicker}, there's no sub-selection story here,
 * so the long-press hook is a deliberate no-op. Selection is emitted
 * through the inherited
 * {@link HorizontalPicker.OnItemSelectedListener}.
 */
public class ThemePicker extends HorizontalPicker {

    /** XML-inflation constructor. */
    public ThemePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Builds the picker item set by walking {@link ThemeFactory#BOARDS}
     * and reparenting each {@link BoardTheme} onto a shared
     * {@link BaseMasterTheme} so it can draw itself in the picker
     * strip independent of the actively-edited theme.
     */
    @Override
    protected PickerItem[] initializeItems() {
        MasterTheme base = new BaseMasterTheme();
        PickerItem[] arr = new PickerItem[ThemeFactory.BOARDS.size()];
        int i = 0;
        for (BoardTheme t : ThemeFactory.BOARDS) {
            arr[i] = t;
            t.setParent(base);
            i++;
        }
        return arr;
    }


    /**
     * Long-press is deliberately ignored — there is no secondary
     * selection for a board theme, so the entire interaction is
     * handled by single taps.
     */
    @Override
    protected void onItemLongPress(int index, float startX, float startY) {
        //Do nothing
    }
}
