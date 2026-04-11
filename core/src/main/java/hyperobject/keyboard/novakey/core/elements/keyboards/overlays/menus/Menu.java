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

package hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus;

import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.NoAction;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;

/**
 * Shared contract for the popup menu overlays ({@link InfiniteMenu},
 * {@link OnUpMenu}). Each menu is a list of {@link Entry entries};
 * concrete menus decide how to lay them out, how touches select them,
 * and when to commit.
 */
public interface Menu {

    /**
     * One selectable item in a menu. {@code data} is the payload to
     * render — typically a {@code Character}, {@code String}, or
     * {@code Drawable}. {@code action} is fired when the user commits
     * on this entry.
     */
    class Entry {
        public final Object data;
        public final Action action;


        /**
         * @param data   what the menu draws for this entry (char/string/drawable)
         * @param action the action to fire on commit
         */
        public Entry(Object data, Action action) {
            this.data = data;
            this.action = action;
        }
    }

    /**
     * Shared "cancel" entry — shows the clear icon and fires a
     * {@link NoAction} so commits on it are a silent no-op.
     */
    Entry CANCEL = new Entry(Icons.get("clear"), new NoAction());
}
