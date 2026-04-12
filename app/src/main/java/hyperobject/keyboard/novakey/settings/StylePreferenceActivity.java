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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Toast;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.utils.Colors;
import hyperobject.keyboard.novakey.core.model.MainModel;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.model.factories.ThemeFactory;
import hyperobject.keyboard.novakey.core.model.loaders.ThemeLoader;
import hyperobject.keyboard.novakey.widgets.NovaKeyPreview;
import hyperobject.keyboard.novakey.widgets.pickers.ColorPicker;
import hyperobject.keyboard.novakey.widgets.pickers.ReleasePicker;
import hyperobject.keyboard.novakey.widgets.pickers.ThemePicker;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;

/**
 * Theme / style editor screen. Shows a {@link NovaKeyPreview} at the top
 * and wires a {@link ThemePicker}, three {@link ColorPicker}s (primary /
 * accent / contrast), plus two checkboxes (auto-color and 3D) to the
 * preview's live {@link Model}. Each picker change updates the model's
 * {@link hyperobject.keyboard.novakey.core.view.themes.MasterTheme} in
 * place and triggers an {@code invalidate()} so the preview redraws.
 * <p>
 * Extends {@link AbstractPreferenceActivity}, which supplies the FAB
 * "done" button — clicking it routes through
 * {@link #onActivityClosed(boolean)} to persist the new theme via
 * {@link ThemeLoader}.
 */
public class StylePreferenceActivity extends AbstractPreferenceActivity {

    private boolean mIsAuto = false;
    private NovaKeyPreview mPreview;
    private Model mModel;


    /**
     * Builds the live preview model, looks up each picker widget in
     * the inflated layout, and attaches selection listeners so that
     * choosing a board theme or color writes straight back into
     * {@code mModel.getTheme()} and refreshes the preview.
     * <p>
     * The auto-color checkbox doesn't affect the preview — it's a raw
     * boolean that gets written to {@link Settings#pref_auto_color} on
     * save. The 3D checkbox toggles
     * {@link hyperobject.keyboard.novakey.core.view.themes.MasterTheme#set3D(boolean)}
     * and invalidates the preview.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ReleasePicker releasePicker = (ReleasePicker) findViewById(R.id.releasePick);

        mPreview = (NovaKeyPreview) findViewById(R.id.preview);
        mModel = new MainModel(this);
        mPreview.setModel(mModel);

        final CheckBox autoCheck = (CheckBox) findViewById(R.id.autoColor);
        autoCheck.setChecked(mIsAuto);
        autoCheck.setOnCheckedChangeListener((buttonView, isChecked) -> mIsAuto = isChecked);

        final CheckBox _3dCheck = (CheckBox) findViewById(R.id.threeDee);
        _3dCheck.setChecked(mModel.getTheme().is3D());
        _3dCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mModel.getTheme().set3D(isChecked);
            mPreview.invalidate();
        });

        final ThemePicker themePicker = (ThemePicker) findViewById(R.id.themePicker);
        themePicker.setItem(ThemeFactory.getBoardNum(mModel.getTheme().getBoardTheme()));
        themePicker.setOnItemSelectedListener((item, subIndex) -> {
            mModel.getTheme().setBoardTheme((BoardTheme) item);
            mPreview.invalidate();
        });

        final ColorPicker primaryColor = (ColorPicker) findViewById(R.id.primaryColor);
        primaryColor.setReleasePicker(releasePicker);
        primaryColor.setItem(Colors.path(mModel.getTheme().getPrimaryColor())[0]);
        primaryColor.setOnItemSelectedListener((item, subIndex) -> {
            mModel.getTheme().setPrimaryColor(((Colors) item).shade(subIndex));
            mPreview.invalidate();
        });

        final ColorPicker secondaryColor = (ColorPicker) findViewById(R.id.secondaryColor);
        secondaryColor.setReleasePicker(releasePicker);
        secondaryColor.setItem(Colors.path(mModel.getTheme().getAccentColor())[0]);
        secondaryColor.setOnItemSelectedListener((item, subIndex) -> {
            mModel.getTheme().setAccentColor(((Colors) item).shade(subIndex));
            mPreview.invalidate();
        });

        final ColorPicker ternaryPicker = (ColorPicker) findViewById(R.id.ternaryColor);
        ternaryPicker.setReleasePicker(releasePicker);
        ternaryPicker.setItem(Colors.path(mModel.getTheme().getContrastColor())[0]);
        ternaryPicker.setOnItemSelectedListener((item, subIndex) -> {
            mModel.getTheme().setContrastColor(((Colors) item).shade(subIndex));
            mPreview.invalidate();
        });
    }


    /** Returns the layout resource inflated by the base class. */
    @Override
    int getLayoutId() {
        return R.layout.style_preference_layout;
    }


    /**
     * Save hook fired by {@link AbstractPreferenceActivity} — commits
     * the in-memory theme to disk via {@link ThemeLoader#save} and
     * writes the auto-color flag to the default SharedPreferences.
     * Only runs on positive exit (FAB click); cancel paths leave the
     * persisted theme untouched.
     *
     * @param positiveResult {@code true} to save the edited theme
     */
    @Override
    void onActivityClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean(Settings.pref_auto_color, mIsAuto);
            editor.apply();

            new ThemeLoader(this).save(mModel.getTheme());

            Toast t = Toast.makeText(this, "Style Saved.", Toast.LENGTH_SHORT);
            t.show();
        }
    }
}
