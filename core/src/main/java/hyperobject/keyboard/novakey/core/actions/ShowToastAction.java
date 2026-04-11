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

package hyperobject.keyboard.novakey.core.actions;


import android.os.Handler;
import android.widget.Toast;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Shows a short Android {@link Toast} from inside an Action. Exists as
 * an Action (rather than a direct call) so feedback messages like
 * "Text Copied" flow through the same {@link Controller#fire} pipeline
 * as every other state change, and so other actions can composite a
 * toast without holding a Context reference themselves.
 */
public class ShowToastAction implements Action<Void> {

    private final String mMessage;
    private final int mLength;


    /**
     * @param message the text to display in the toast
     * @param length  one of {@link Toast#LENGTH_SHORT} or
     *                {@link Toast#LENGTH_LONG}
     */
    public ShowToastAction(String message, int length) {
        mMessage = message;
        mLength = length;
    }


    /**
     * Posts the {@code Toast.show()} call onto the IME's main looper,
     * so this action can be fired safely from any thread — the toast
     * API itself must run on the UI thread.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        Handler h = new Handler(ime.getMainLooper());
        h.post(() -> Toast.makeText(ime, mMessage, mLength).show());
        return null;
    }
}
