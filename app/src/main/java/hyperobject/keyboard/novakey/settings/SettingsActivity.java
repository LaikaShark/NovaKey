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

/**
 * Launcher activity of the app and host for {@link PreferencesFragment}.
 * <p>
 * Post-modernization this is an AndroidX {@link AppCompatActivity} using
 * the support fragment manager. Prior to the 2026 pass it extended the
 * framework {@code android.preference.PreferenceActivity}, which was
 * deprecated in API 28 and is incompatible with
 * {@link androidx.preference.PreferenceFragmentCompat}.
 * <p>
 * First-run flow: if {@link MainNovaKeyService#MY_PREFERENCES} has no
 * {@code has_setup} flag, the user is redirected to {@link SetupActivity}
 * instead of seeing the preferences screen.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Activity create hook: initializes the drawing/theme singletons
     * (which are normally set up by the IME service, but may not have
     * run yet when settings is launched cold), binds the global
     * {@link Settings} object to the default preferences, and either
     * redirects to setup on first launch or installs the preferences
     * fragment.
     * <p>
     * The {@code savedInstanceState == null} guard avoids stacking a
     * second fragment copy on configuration changes — Android restores
     * the original fragment for us.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext()
                .getSharedPreferences(MainNovaKeyService.MY_PREFERENCES, MODE_PRIVATE);

        // Re-initialize the global drawing/theme singletons. The IME service
        // does this on its own create-path, but launching settings cold
        // (without the IME being active) means none of these have run yet.
        Colors.initialize();
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
