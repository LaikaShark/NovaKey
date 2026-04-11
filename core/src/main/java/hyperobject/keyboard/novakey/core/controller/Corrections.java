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

package hyperobject.keyboard.novakey.core.controller;

import android.content.Context;

/**
 * Strategy interface for text corrections and predictions — given the
 * composing-region text (whatever the user has just typed but not yet
 * committed), produce the replacement the keyboard should offer or commit.
 * <p>
 * Today the only implementation is {@link BasicCorrections}, which is a
 * placeholder contraction expander. A real corrections/predictions engine
 * would plug in here by implementing this interface.
 */
public interface Corrections {

    /**
     * One-time setup hook called before any call to {@link #correction(String)}.
     * Implementations should load their dictionaries / model data here so the
     * per-keystroke path stays cheap.
     *
     * @param context used to pull resources and on-device data files
     */
    void initialize(Context context);


    /**
     * Returns the replacement for the given composing text, or the same
     * text unchanged if no correction applies.
     *
     * TODO: this corrections method will change drastically — current
     * implementation is only a placeholder.
     *
     * @param composing the text currently being composed by the user
     * @return the corrected text, or {@code composing} if no correction fires
     */
    String correction(String composing);

}
