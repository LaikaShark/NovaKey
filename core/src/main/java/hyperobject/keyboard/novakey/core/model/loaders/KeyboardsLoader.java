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

package hyperobject.keyboard.novakey.core.model.loaders;

import android.content.Context;

import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;

/**
 * Loader for the {@link Keyboards} registry. All of the interesting
 * work happens inside the {@link Keyboards} constructor — it reads
 * the string-array resources for every shipped language/fixed board —
 * so this class is really just a thin {@link Loader} wrapper that
 * holds onto a {@link Context} for resource access.
 */
public class KeyboardsLoader implements Loader<Keyboards> {

    private final Context mContext;


    /**
     * Captures the context the {@link Keyboards} constructor will use
     * when decoding its string-array resources.
     */
    public KeyboardsLoader(Context context) {
        mContext = context;
    }


    /** Builds a fresh {@link Keyboards} registry from code-defined resources. */
    @Override
    public Keyboards load() {
        return new Keyboards(mContext);
    }


    /**
     * No-op pending a future user-defined layouts feature — keyboard
     * layouts are currently built from string-array resources, with
     * nothing to persist.
     */
    @Override
    public void save(Keyboards keyboards) {
        //TODO
    }
}
