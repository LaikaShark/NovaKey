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

package hyperobject.keyboard.novakey;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import hyperobject.keyboard.novakey.HexGridView;
import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.emoji.Emoji;
import hyperobject.keyboard.novakey.core.utils.drawing.emoji.ThrowAwayView;

/**
 * Internal / developer screen used to lay out emoji into a hex grid one
 * at a time. Reachable from {@link PreferencesFragment} via the hidden
 * "pref_test" key. Not shipped as a user-facing feature.
 * <p>
 * The activity owns a 10x10 {@code Drawable[][]} grid. As the user taps
 * through the {@link ThrowAwayView} it picks up the emitted emoji and
 * drops it into the next slot, advancing the insertion cursor row-major
 * and wrapping back to (0,0) when both dimensions are exhausted.
 */
public class EmojiSettingActivity extends AppCompatActivity {

    int x = 0, y = 0;


    /**
     * Loads the emoji assets, installs the hex grid layout, and wires the
     * {@link ThrowAwayView} listener so that each emitted {@link Drawable}
     * is written into {@code grid[x][y]} and the grid is re-drawn. After
     * each insert, {@code (x, y)} advances: x first, then y once x hits
     * the row width; both coordinates wrap at 10 so the grid fills back
     * to front if the user keeps tapping.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Emoji.load(this);
        setContentView(R.layout.activity_emoji_setting);


        final Drawable[][] grid = new Drawable[10][10];

        final HexGridView hex = findViewById(R.id.hex);
        hex.setGrid(grid);

        ThrowAwayView tav = findViewById(R.id.throaway);
        tav.setListener(e -> {
            grid[x][y] = e;
            hex.setGrid(grid);
            hex.invalidate();

            x++;
            if (x >= 10) {
                x = 0;
                y++;
            }
            if (y >= 10)
                y = 0;
        });
    }
}
