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

package hyperobject.keyboard.novakey.core.model;

import android.content.SharedPreferences;

import hyperobject.keyboard.novakey.core.BuildConfig;
import hyperobject.keyboard.novakey.core.model.factories.ThemeFactory;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Global singleton for simple user preferences. Holds one static field
 * per preference so hot paths can read them without a {@code SharedPreferences}
 * lookup, plus the string keys used both to register
 * listeners and to read the values back out.
 * <p>
 * The IME service calls {@link #setPrefs} once at startup, pointing this
 * class at the host's {@code MY_PREFERENCES} file (MainNovaKeyService
 * deliberately keeps that legacy key so existing installs don't lose
 * their settings on upgrade — do not rename it). After that, the
 * registered listener automatically re-runs {@link #update()} whenever
 * any pref changes, so the static fields stay in sync.
 * <p>
 * Structured state (dimensions, theme, keyboard layouts) lives in
 * {@link MainModel} loaders instead; this class is strictly for the
 * boolean/int/String flags shown on the settings screen.
 */
public class Settings {

    // Preference keys. Entries flagged "(REMOVED)" / "(DEPRECATED)" are kept
    // here for the legacy-pref migration in fixLegacyThemeing(); "INTENT"
    // entries are action hooks wired up elsewhere, not raw values.
    public static String
            //NovaKey 0.1
            pref_hide_letters = "pref_hide_letters",
            pref_hide_password = "pref_hide_password",
            pref_vibrate = "pref_vibrate",
            pref_quick_insert = "pref_quick_insert",
    //pref_color = "pref_color",(REMOVED)
    //NovaKey 0.2
    pref_auto_correct = "pref_auto_correct",
            pref_quick_close = "pref_quick_close",
    //NovaKey 0.3
    pref_theme_legacy = "pref_theme",//(DEPRECATED)
    //pref_btns = "pref_btns",//(REMOVED)
    pref_tut = "pref_tut",//INTENT
            pref_style = "pref_style",//INTENT
            pref_space_bar = "pref_space_bar",//(DEPRECATED)
            pref_start_version = "pref_start_version",
    //NovaKey 0.3.5
    pref_long_press_time = "pref_long_press_time",
    //Novakey 0.3.7
    pref_vibrate_level = "pref_vibrate_level",
    //NovaKey 1.0
            pref_theme = "pref_master_theme",
            pref_auto_capitalize = "pref_auto_capitalize";

    /** Sentinel string used by theme prefs to mean "no user override, use the built-in default." */
    public static String DEFAULT = "DEFAULT";

    // Global flag mirror of the boolean prefs, updated by update():
    //   hideLetters   - hide letter labels on the wheel for a minimal look
    //   hidePassword  - force-hide labels on password fields even if hideLetters is off
    //   vibrate       - master toggle for haptic feedback on key press
    //   quickInsert   - insert the first key in a sector on touch-down, not swipe decide
    //   autoCorrect   - enable the on-the-fly word correction engine
    //   quickClose    - auto-close brackets/quotes when the user types the opener
    public static boolean hideLetters, hidePassword, vibrate, quickInsert, autoCorrect, quickClose;
    /** Whether the legacy space-bar button is shown instead of the gesture. */
    public static boolean hasSpaceBar;
    /** Whether to honor EditorInfo's caps-mode hint at the start of each sentence. */
    public static boolean autoCapitalize;

    // Integer settings:
    //   startVersion  - BuildConfig.VERSION_CODE at first install, for upgrade detection
    //   longPressTime - ms a touch must be held before it counts as a long press
    //   vibrateLevel  - vibration intensity (ms duration) when `vibrate` is on
    public static int startVersion, longPressTime, vibrateLevel;

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor edit;

    // Strong reference so the WeakHashMap inside SharedPreferencesImpl
    // cannot garbage-collect the listener and silently stop delivering
    // preference-change callbacks.
    private static SharedPreferences.OnSharedPreferenceChangeListener sListener;


    /**
     * Installs the {@code SharedPreferences} this class reads from and
     * registers a change listener so {@link #update()} fires automatically
     * whenever any pref value changes. Called once from the IME service
     * at startup — passing in the service's deliberately-preserved
     * {@code MY_PREFERENCES} handle.  The listener is held in a static
     * field so it survives garbage collection.
     */
    public static void setPrefs(SharedPreferences pref) {
        if (prefs != null && sListener != null)
            prefs.unregisterOnSharedPreferenceChangeListener(sListener);
        prefs = pref;
        edit = prefs.edit();
        sListener = (sharedPreferences, s) -> update();
        prefs.registerOnSharedPreferenceChangeListener(sListener);
    }


    /**
     * Re-reads every tracked preference out of the store and updates the
     * static fields. Also stamps {@code pref_start_version} on the very
     * first run (so upgrade detection has something to compare against),
     * then runs the legacy-pref migration pass.
     */
    public static void update() {
        //Boolean Flag settings
        hideLetters = prefs.getBoolean(pref_hide_letters, false);
        hidePassword = prefs.getBoolean(pref_hide_password, false);
        vibrate = prefs.getBoolean(pref_vibrate, false);
        quickInsert = prefs.getBoolean(pref_quick_insert, false);

        autoCorrect = prefs.getBoolean(pref_auto_correct, false);
        quickClose = prefs.getBoolean(pref_quick_close, false);

        hasSpaceBar = prefs.getBoolean(pref_space_bar, false);

        // Default true so existing installs keep the previous auto-cap behavior
        // (UpdateShiftAction unconditionally honored EditorInfo's caps mode before
        // this preference existed).
        autoCapitalize = prefs.getBoolean(pref_auto_capitalize, true);

        //Integer settings
        //this will only default to the given number if the person has never had this preference
        startVersion = prefs.getInt(pref_start_version, BuildConfig.VERSION_CODE);
        if (startVersion == BuildConfig.VERSION_CODE)
            edit.putInt(pref_start_version, startVersion);

        longPressTime = prefs.getInt(pref_long_press_time, 500);
        vibrateLevel = prefs.getInt(pref_vibrate_level, 50);

        fixLegacyPrefs();
        edit.commit();
    }


    /**
     * One-shot migration entry point for prefs that changed shape
     * between versions. Delegates to per-pref fixers; currently only
     * the theme pref has a legacy format.
     */
    private static void fixLegacyPrefs() {
        fixLegacyThemeing();
    }


    /**
     * Migrates pre-1.0 theme strings (see {@code ThemeFactory.themeFromLegacyString})
     * into the new JSON blob stored under {@link #pref_theme}, then
     * deletes the old pref so the migration only runs once.
     */
    private static void fixLegacyThemeing() {
        String str = prefs.getString(pref_theme_legacy, DEFAULT);

        if (!str.equals(DEFAULT)) {
            //set new from old
            String newStr = prefs.getString(pref_theme, DEFAULT);
            if (newStr == DEFAULT) {//if new theme doesn't exist otherwise don't change
                MasterTheme theme = ThemeFactory.themeFromLegacyString(str);
                edit.putString(pref_theme, ThemeFactory.stringFromTheme(theme));
            }
            //delete old
            edit.remove(pref_theme_legacy);
        }
    }
}
