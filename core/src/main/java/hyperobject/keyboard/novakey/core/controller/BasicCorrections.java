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

import java.util.Locale;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Placeholder {@link Corrections} implementation: expands English-style
 * apostrophe-less contractions back into the apostrophe form (e.g. "dont"
 * → "don't") by table lookup against {@code R.array.contractions}.
 * <p>
 * Preserves the user's casing — ALL CAPS stays ALL CAPS, Leading-cap stays
 * Leading-cap, otherwise lowercase. This is not a real corrections engine;
 * it's a stand-in until a proper predictive/autocorrect model is wired in.
 */
public class BasicCorrections implements Corrections {

    String[] mContractions;


    /**
     * Loads the contractions lookup table out of resources. Called once at
     * controller setup; the array is cached on the instance so per-keystroke
     * lookups stay allocation-free.
     */
    @Override
    public void initialize(Context context) {
        mContractions = context.getResources().getStringArray(R.array.contractions);
    }


    /**
     * Looks up {@code composing} in the contractions table (via
     * {@link #contractionIndex}) and, if found, returns the apostrophe form
     * with casing matched to the input: all-caps input → all-caps output,
     * leading-cap input → leading-cap output, otherwise the table entry
     * verbatim. Returns the input unchanged if no entry matches.
     *
     * TODO: this corrections method will change drastically — placeholder.
     */
    @Override
    public String correction(String composing) {


        int idx = contractionIndex(composing);
        if (idx != -1) {
            String s = mContractions[idx];
            if (composing.matches("[A-Z]+"))
                return s.toUpperCase(Locale.US);//TODO: other languages
            else if (Character.isUpperCase(composing.charAt(0)))
                return Util.capsFirst(s);
            return s;
        }
        return composing;
    }


    /**
     * Linear scan of the contractions table. For each entry, strips the
     * apostrophe and compares case-insensitively to {@code text}. Returns
     * the matching index, or -1 if nothing matched.
     */
    private int contractionIndex(String text) {
        for (int i = 0; i < mContractions.length; i++) {
            String check = mContractions[i].replace("\'", "");
            if (check.equalsIgnoreCase(text.toString()))
                return i;
        }
        return -1;
    }
}
