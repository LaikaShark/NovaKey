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

package hyperobject.keyboard.novakey.core.utils.drawing;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.BMPDrawable;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.FontIcon;

/**
 * Process-wide icon registry and factory. Loaded once at IME startup
 * by {@link #load(Context)} and then consumed via {@link #get(String)}.
 * <p>
 * Holds two kinds of icons:
 * <ul>
 *   <li>Direct static fields — {@code cursors}, {@code cursorLeft},
 *       {@code cursorRight} (cursor overlays drawn as
 *       {@link BMPDrawable}s). These were never added to the
 *       searchable list and are accessed via the field name directly.</li>
 *   <li>A flat {@link ArrayList} of {@link FontIcon}s populated from
 *       two codepoints files: {@code res/raw/codepoints} (Material
 *       Icons) and {@code res/raw/codepoints_custom} (the project's
 *       custom icon font). {@link #get(String)} does a linear scan
 *       with String-compared equality — see
 *       {@link FontIcon#equals(Object)}.</li>
 * </ul>
 * The registry pattern means elements never need to plumb
 * {@link Resources} around; they just ask for an icon by name at
 * draw time.
 */
public class Icons {

    public static BMPDrawable cursors, cursorLeft, cursorRight;//TODO: add to list
    private static ArrayList<Drawable> icons;


    /**
     * One-shot loader: populates the cursor {@link BMPDrawable}s and
     * walks both codepoints files to register every Material and
     * custom font icon. Must be called before any {@link #get(String)}
     * lookup.
     */
    public static void load(Context context) {
        Resources res = context.getResources();
        icons = new ArrayList<>();

        cursors = new BMPDrawable(res, R.drawable.ic_cursors);
        cursorLeft = new BMPDrawable(res, R.drawable.ic_cursor_left);
        cursorRight = new BMPDrawable(res, R.drawable.ic_cursor_right);

        setMaterialIcons(res);
        setCustomIcons(res);
    }


    /**
     * Parses {@code res/raw/codepoints} and registers one
     * {@link FontIcon} per line against {@link Font#MATERIAL_ICONS}.
     * <p>
     * How: each line is {@code "name hex_codepoint"}. The hex is parsed
     * to an int and turned into a glyph string via
     * {@link StringBuilder#appendCodePoint}. I/O errors are rethrown as
     * {@link RuntimeException} — load failure here means the keyboard
     * can't render its icons, so crashing loudly is the right call.
     *
     * @param res resources used to open the raw file
     */
    private static void setMaterialIcons(Resources res) {
        InputStream is = res.openRawResource(R.raw.codepoints);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder sb = new StringBuilder("");
                String[] params = line.split(" ");
                icons.add(new FontIcon(params[0],
                        sb.appendCodePoint(Integer.parseInt(params[1], 16)).toString(),
                        Font.MATERIAL_ICONS));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading TXT file: " + ex);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
    }


    /**
     * Same as {@link #setMaterialIcons} but for the project-owned
     * {@code res/raw/codepoints_custom} file and
     * {@link Font#CUSTOM_ICONS}. Kept as a separate method so the two
     * fonts can diverge independently (different file, different
     * typeface) without a flag parameter.
     *
     * @param res resources used to open the raw file
     */
    private static void setCustomIcons(Resources res) {
        InputStream is = res.openRawResource(R.raw.codepoints_custom);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder sb = new StringBuilder("");
                String[] params = line.split(" ");
                icons.add(new FontIcon(params[0],
                        sb.appendCodePoint(Integer.parseInt(params[1], 16)).toString(),
                        Font.CUSTOM_ICONS));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading TXT file: " + ex);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
    }


    /**
     * Looks up a registered icon by name.
     * <p>
     * How: linear scan of the icon list, relying on
     * {@link FontIcon#equals(Object)}'s String-compared semantics so
     * either the name or the glyph string can be used as the key.
     * Returns {@code null} if no icon matches — callers that draw the
     * result are responsible for their own null check.
     *
     * @param name lookup key (icon name or glyph string)
     * @return the matching drawable, or {@code null} if none registered
     */
    public static Drawable get(String name) {
        for (Drawable d : icons) {
            if (d.equals(name))
                return d;
        }
        return null;
    }
}
