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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hyperobject.keyboard.novakey.MainNovaKeyService;
import hyperobject.keyboard.novakey.core.utils.Colors;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.setup.SetupActivity;
import hyperobject.keyboard.novakey.core.utils.drawing.Font;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;
import hyperobject.keyboard.novakey.core.view.themes.AppTheme;

/**
 * Hosts the {@link PreferencesFragment} settings UI and acts as the launcher
 * activity for the app.
 *
 * Migrated during the AndroidX modernization pass from
 * {@code android.preference.PreferenceActivity} (deprecated since API 28) to
 * {@link AppCompatActivity}. The fragment is now installed via the support
 * fragment manager — the framework FragmentManager that the old code used is
 * incompatible with {@link androidx.preference.PreferenceFragmentCompat}.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext()
                .getSharedPreferences(MainNovaKeyService.MY_PREFERENCES, MODE_PRIVATE);

        // Re-initialize the global drawing/theme singletons. The IME service
        // does this on its own create-path, but launching settings cold
        // (without the IME being active) means none of these have run yet.
        Colors.initialize();
        AppTheme.load(this, getResources());
        Font.create(getApplicationContext());
        Icons.load(getApplicationContext());

        Settings.setPrefs(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Settings.update();

        // First-launch path: kick the user into the setup wizard.
        if (!pref.getBoolean("has_setup", false)) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        // Only install the fragment on a fresh create. On a configuration
        // change Android restores the existing fragment automatically and
        // committing a second copy would stack two PreferencesFragments.
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new PreferencesFragment())
                    .commit();
        }
    }
}
