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

/**
 * A tiny host activity that simply inflates {@code R.layout.activity_demo}.
 * Used as a sandbox screen for trying out the IME against a real
 * {@link android.widget.EditText}/text field target outside of the
 * settings or tutorial flows.
 * <p>
 * Extends {@link AppCompatActivity} post-AndroidX migration; prior to the
 * 2026 modernization pass this was {@code android.support.v7.app.AppCompatActivity}.
 */
public class DemoActivity extends AppCompatActivity {

    /**
     * Standard activity create hook: defers to the superclass and then
     * installs the demo layout. No further wiring is needed — the layout
     * itself carries whatever input fields the demo exercises.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }
}
