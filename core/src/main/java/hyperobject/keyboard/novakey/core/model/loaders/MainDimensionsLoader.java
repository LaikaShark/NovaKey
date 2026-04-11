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
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.model.MainDimensions;

/**
 * Loads and saves the NovaKey wheel's geometry — center, radii,
 * padding, and view dimensions — from {@code SharedPreferences}.
 * Portrait and landscape store independent copies of every value
 * (landscape keys are suffixed {@code _land}) so rotating the device
 * doesn't drag one orientation's layout into the other.
 * <p>
 * The stored "smallRadius" pref is a <em>divisor</em> of the main
 * radius; the resolved pixel value lives on {@link MainDimensions}
 * everywhere else in the codebase. {@link #load()} and {@link #save}
 * do the conversion in both directions.
 */
public class MainDimensionsLoader implements Loader<MainDimensions> {

    private final SharedPreferences mSharedPref;
    private final Context mContext;


    /**
     * Captures the context and the default {@code SharedPreferences}
     * handle, so subsequent loads/saves can read orientation and the
     * default-radius/default-padding dimens.
     */
    public MainDimensionsLoader(Context context) {
        this.mContext = context;
        this.mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }


    /**
     * Returns whether the device is currently in landscape orientation,
     * so the per-orientation pref key suffix can be picked.
     */
    private boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }


    /**
     * Reads every geometry value out of prefs and assembles a
     * {@link MainDimensions}. Missing values fall back to the
     * {@code R.dimen.default_*} resources, the display's full width,
     * and reasonable defaults derived from the loaded radius/padding.
     * Converts the divisor-shaped {@code smallRadius} pref into its
     * pixel equivalent before stashing it on the result.
     */
    @Override
    public MainDimensions load() {
        float r = mSharedPref.getFloat("size" + (isLandscape() ? "_land" : ""),
                mContext.getResources().getDimension(R.dimen.default_radius));
        float sr = r / mSharedPref.getFloat("smallRadius", 3);
        float p = mSharedPref.getFloat("padd" + (isLandscape() ? "_land" : ""),
                mContext.getResources().getDimension(R.dimen.default_padding));

        int w = mContext.getResources().getDisplayMetrics().widthPixels;
        int h = (int) (mSharedPref.getFloat("height" + (isLandscape() ? "_land" : ""), r * 2 + p));

        float x = mSharedPref.getFloat("x" + (isLandscape() ? "_land" : ""), w / 2);
        //TODO: if legacy Y
        float y = mSharedPref.getFloat("y" + (isLandscape() ? "_land" : ""), r + p);

        return new MainDimensions(x, y, r, sr, p, w, h);
    }


    /**
     * Writes {@code md} back into prefs. Mirrors {@link #load()}: the
     * smallRadius divisor is reconstructed from the resolved pixel
     * value, and every key gets the {@code _land} suffix in landscape.
     * Uses {@code apply()} so the write is fire-and-forget.
     */
    @Override
    public void save(MainDimensions md) {
        // The "smallRadius" pref stores a divisor (load() resolves the pixel
        // value as r / divisor), but MainDimensions.smallRadius is the
        // resolved pixel value everywhere else in the codebase. Convert back
        // to a divisor here so the round-trip is symmetric. Fall back to the
        // default of 3 if smallRadius is zero/negative to avoid writing
        // Infinity into the pref.
        float divisor = md.getSmallRadius() > 0
                ? md.getRadius() / md.getSmallRadius()
                : 3f;
        // W - //TODO: for now it's based on the width of the screen
        mSharedPref.edit()
                .putFloat("x" + (isLandscape() ? "_land" : ""), md.getX())
                .putFloat("y" + (isLandscape() ? "_land" : ""), md.getY())
                .putFloat("size" + (isLandscape() ? "_land" : ""), md.getRadius())
                .putFloat("smallRadius", divisor)
                .putFloat("padd" + (isLandscape() ? "_land" : ""), md.getPadding())
                .putFloat("height" + (isLandscape() ? "_land" : ""), md.getHeight())
                .apply();
    }
}
