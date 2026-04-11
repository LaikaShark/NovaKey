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

package hyperobject.keyboard.novakey.core.utils;

import android.content.SharedPreferences;
import android.view.inputmethod.ExtractedText;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tiny collection of {@code System.out.println} shortcuts used as an
 * ad-hoc debug log during development.
 * <p>
 * On Android these land in stdout rather than logcat, so they are mostly
 * useful when running unit tests on the JVM or attaching through a host
 * shell — production code should prefer {@code android.util.Log}.
 * Every helper swallows exceptions by printing a fallback line instead
 * of propagating, so {@code Print.*} calls can be sprinkled anywhere
 * without try/catch noise.
 */
public class Print {

    /**
     * Prints any object's {@code toString()} on its own line, falling
     * back to a fixed error string if printing itself throws.
     */
    public static void ln(Object o) {
        try {
            System.out.println(o);
        } catch (Exception e) {
            System.out.println("print failed!");
        }
    }


    /** Prints a radian angle converted to degrees for human reading. */
    public static void angle(double a) {
        ln(Math.toDegrees(a));
    }


    /** Prints an int as a zero-padded hex literal like {@code 0xff00aa}. */
    public static void hex(int i) {
        ln("0x" + Integer.toHexString(i));
    }


    /**
     * Prints an integer list as space-separated numbers on one line by
     * concatenating each element followed by a space.
     */
    public static void intList(ArrayList<Integer> l) {
        String s = "";
        for (int i : l) {
            s += i + " ";
        }
        ln(s);
    }


    /**
     * Dumps every key/value pair in a {@link SharedPreferences} bundle
     * under a "-----Shared Pref-----" header, one entry per line.
     */
    public static void sharedPref(SharedPreferences pref) {
        Map<String, ?> keys = pref.getAll();
        ln("-----Shared Pref-----");
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            ln(entry.getKey() + ": " +
                    entry.getValue().toString());
        }
    }


    /** Prints each element of a string array on its own line. */
    public static void stringArr(String[] arr) {
        for (String s : arr) {
            ln(s);
        }
    }


    /**
     * Pretty-prints an {@link ExtractedText} showing the selection as
     * pipe characters around the highlighted region, e.g.
     * {@code "hello |world|!"}. Prints {@code "null"} if {@code et} is
     * null. When the selection is empty only the caret pipe is drawn.
     */
    public static void et(ExtractedText et) {
        if (et == null)
            ln("null");
        else
            ln('"' +
                    et.text.toString().substring(0, et.selectionStart) +
                    "|" + (et.selectionEnd == et.selectionStart ? "" :
                    et.text.toString().substring(et.selectionStart, et.selectionEnd) + "|") +
                    et.text.toString().substring(et.selectionEnd));
    }
}
