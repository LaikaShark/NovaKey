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

package hyperobject.keyboard.novakey.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.tutorial.TutorialActivity;

/**
 * Main settings screen content. Hosts the preference tree defined by
 * {@code res/xml/settings.xml} and wires the non-persistent entries
 * (style picker, tutorial, market link, reddit link, beta-test link)
 * to their respective activity launches.
 * <p>
 * Post-modernization this is an AndroidX {@link PreferenceFragmentCompat}
 * hosted by {@link SettingsActivity} (which is itself an
 * {@link androidx.appcompat.app.AppCompatActivity}). The legacy
 * framework {@code android.preference.PreferenceFragment} was deprecated
 * in API 28 and dropped in the 2026 pass. The notable behavioral
 * differences:
 * <ul>
 *   <li>XML inflation happens in {@link #onCreatePreferences} via
 *       {@link #setPreferencesFromResource}, not in {@code onCreate}.</li>
 *   <li>{@link Preference} is now the AndroidX class;
 *       {@code findPreference} is generic and returns {@code <T>}.</li>
 *   <li>SeekBar entries in {@code settings.xml} use AndroidX's built-in
 *       widget, not the abandoned {@code com.pavelsikun} library.</li>
 * </ul>
 */
public class PreferencesFragment extends PreferenceFragmentCompat {

    /**
     * AndroidX preference-tree setup hook. Inflates the XML into the
     * fragment's preference hierarchy and attaches click listeners to
     * the entries that launch other activities (style picker, tutorial,
     * Play Store rating, beta program, subreddit).
     * <p>
     * Each click listener is guarded by a null check so that a
     * preference missing from the XML (e.g. a build variant that drops
     * it) silently no-ops instead of crashing.
     *
     * @param savedInstanceState preference-state bundle (unused)
     * @param rootKey            fragment root key — null for the top-level screen
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        Preference stylePref = findPreference(Settings.pref_style);
        if (stylePref != null) {
            stylePref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), StylePreferenceActivity.class));
                return true;
            });
        }

        Preference ratePref = findPreference(Settings.pref_rate);
        if (ratePref != null) {
            ratePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=hyperobject.keyboard.novakey"));
                startActivity(intent);
                return true;
            });
        }

        Preference tutPref = findPreference(Settings.pref_tut);
        if (tutPref != null) {
            tutPref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), TutorialActivity.class));
                return true;
            });
        }

        Preference betaTestPref = findPreference(Settings.pref_beta_test);
        if (betaTestPref != null) {
            betaTestPref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/apps/testing/hyperobject.keyboard.novakey")));
                return true;
            });
        }

        Preference subredditPref = findPreference(Settings.pref_subreddit);
        if (subredditPref != null) {
            subredditPref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.reddit.com/r/NovaKey/")));
                return true;
            });
        }
    }
}
