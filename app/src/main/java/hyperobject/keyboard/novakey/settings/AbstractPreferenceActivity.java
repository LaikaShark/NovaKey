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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import hyperobject.keyboard.novakey.R;

/**
 * Shared base for the preference-style screens that aren't hosted by the
 * AndroidX preference fragment (currently just {@link StylePreferenceActivity}).
 * <p>
 * Installs a subclass-provided content layout and wires a standard
 * {@link FloatingActionButton} at {@code R.id.fab} as the "done" button:
 * clicking it fires {@link #onActivityClosed(boolean) onActivityClosed(true)}
 * and finishes the activity. Any other exit path (back button, process
 * death) runs {@code onActivityClosed(false)} from {@link #onDestroy()} so
 * subclasses can distinguish a confirmed save from a cancel.
 * <p>
 * Note: {@code getResources().getColor(int)} is deprecated on API 23+ but
 * left in place during the modernization pass (warning only, not fatal on
 * API 35).
 */
public abstract class AbstractPreferenceActivity extends AppCompatActivity {

    private boolean mDone = false;


    /**
     * Inflates {@link #getLayoutId()}, tints the FAB with the NovaKey
     * blue accent plus a white icon, and installs an OnClick listener
     * that flips {@link #mDone} to {@code true}, calls
     * {@link #onActivityClosed(boolean) onActivityClosed(true)}, and
     * finishes the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.novakey_blue)));
        fab.setColorFilter(0xFFffffff);//icon color
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityClosed(true);
                mDone = true;
                finish();
            }
        });
    }


    /**
     * Subclass hook: returns the layout resource to inflate in
     * {@link #onCreate(Bundle)}. The inflated layout must include a
     * {@link FloatingActionButton} with id {@code R.id.fab}.
     */
    abstract int getLayoutId();


    /**
     * Subclass hook fired when the activity is closed.
     *
     * @param positiveResult {@code true} if the FAB was clicked to exit
     *                       (save), {@code false} for any other exit
     *                       path (cancel)
     */
    abstract void onActivityClosed(boolean positiveResult);


    /**
     * Catches "cancel" exits — if the FAB was never clicked,
     * {@link #mDone} is still {@code false} and we synthesize a
     * {@code onActivityClosed(false)} call on the way out so subclasses
     * can roll back any in-progress state.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mDone)
            onActivityClosed(false);
    }

}
