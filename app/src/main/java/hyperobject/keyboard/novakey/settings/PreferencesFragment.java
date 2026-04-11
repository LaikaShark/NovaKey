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

import hyperobject.keyboard.novakey.EmojiSettingActivity;
import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.tutorial.TutorialActivity;

/**
 * Settings screen content.
 *
 * Migrated from the framework {@code android.preference.PreferenceFragment}
 * (deprecated since API 28) to {@link PreferenceFragmentCompat} during the
 * AndroidX modernization pass. The notable behavioral differences:
 *
 *   - The XML inflation now happens in {@link #onCreatePreferences} instead of
 *     {@code onCreate}, and uses {@link #setPreferencesFromResource}.
 *   - {@link androidx.preference.Preference} is the AndroidX class — methods
 *     like {@code findPreference} are now generic and return {@code <T>}.
 *   - SeekBarPreference entries in settings.xml use AndroidX's built-in
 *     widget, not the abandoned {@code com.pavelsikun} library.
 */
public class PreferencesFragment extends PreferenceFragmentCompat {
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

        // The legacy code wrapped this lookup in a try/catch because the
        // "pref_test" key sometimes wasn't present. The AndroidX
        // findPreference() simply returns null in that case, so we just
        // null-check like every other branch above.
        Preference testPref = findPreference("pref_test");
        if (testPref != null) {
            testPref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), EmojiSettingActivity.class));
                return true;
            });
        }
    }
}
