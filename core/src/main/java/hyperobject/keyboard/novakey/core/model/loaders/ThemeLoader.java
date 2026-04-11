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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.model.factories.ThemeFactory;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Loader that round-trips the active {@link MasterTheme} through the
 * JSON blob stored under {@link Settings#pref_theme}. Serialization and
 * deserialization are delegated to {@link ThemeFactory}; this class is
 * the thin {@link Loader} adapter that owns the {@link SharedPreferences}
 * handle and funnels reads/writes through the factory.
 */
public class ThemeLoader implements Loader<MasterTheme> {

    private final SharedPreferences mSharedPref;


    /**
     * Captures the default {@link SharedPreferences} handle the loader
     * will read from and write to.
     */
    public ThemeLoader(Context context) {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        ;
    }


    /**
     * Reads the stored theme blob and asks {@link ThemeFactory} to
     * parse it. Prints the raw string to stdout for debugging —
     * XXX pre-existing {@code System.out.println} calls, left intact.
     */
    @Override
    public MasterTheme load() {
        System.out.println("loading...");
        System.out.println(mSharedPref.getString(
                Settings.pref_theme, Settings.DEFAULT));
        return ThemeFactory.themeFromString(mSharedPref.getString(
                Settings.pref_theme, Settings.DEFAULT));
    }


    /**
     * Serializes {@code theme} via {@link ThemeFactory#stringFromTheme}
     * and commits the result to prefs synchronously.
     */
    @Override
    public void save(MasterTheme theme) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(Settings.pref_theme, ThemeFactory.stringFromTheme(theme));
        editor.commit();
    }
}
