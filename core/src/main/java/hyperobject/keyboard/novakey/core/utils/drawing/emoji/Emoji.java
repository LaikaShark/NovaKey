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

package hyperobject.keyboard.novakey.core.utils.drawing.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.utils.drawing.Draw;
import hyperobject.keyboard.novakey.core.utils.drawing.Font;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;

/**
 * A single emoji: the unicode string, a human-readable name, and a
 * bitmap rendering. Emoji are loaded once at IME startup from
 * {@code res/raw/emoji} (a JSON manifest sourced from the emoji-data
 * project) and stored in the {@link #emojis} list for the picker view
 * to iterate over.
 * <p>
 * Implements {@link Drawable} so emoji can be painted through the same
 * draw call as any other icon.
 */
public class Emoji implements Drawable {

    public static List<Emoji> emojis;


    /**
     * One-shot emoji loader. Parses the JSON manifest from
     * {@code res/raw/emoji} and, for every entry flagged
     * {@code has_img_google}, decodes its bitmap from the drawable
     * resource named {@code e_<image-stem>} and appends a new
     * {@link Emoji} to {@link #emojis}.
     * <p>
     * How: the {@code unified} field is a hyphen-delimited list of
     * hex codepoints (e.g. "1F468-200D-1F4BB") — each segment is
     * parsed as base-16 and appended via
     * {@link StringBuilder#appendCodePoint} to build the unicode
     * string that gets committed to the input connection. The image
     * filename's extension is stripped and hyphens are converted to
     * underscores to match the drawable resource naming convention.
     * <p>
     * I/O and JSON errors are logged and swallowed — the emoji picker
     * will just show nothing if loading fails, which beats crashing
     * the IME on startup.
     */
    public static void load(Context context) {
        Resources res = context.getResources();
        emojis = new ArrayList<>();
        try {
            InputStream is = res.openRawResource(R.raw.emoji);
            byte[] b = new byte[is.available()];
            is.read(b);
            JSONObject jObject = new JSONObject(new String(b));
            JSONArray arr = jObject.getJSONArray("emojis");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject curr = arr.getJSONObject(i);

                if (curr.getBoolean("has_img_google")) {
                    StringBuilder sb = new StringBuilder("");
                    String code = curr.getString("unified");
                    String[] S = code.split("-");
                    for (String s : S) {
                        sb.appendCodePoint(Integer.parseInt(s, 16));
                    }
                    String name = "";
                    name = curr.getString("name");
                    String image = curr.getString("image");
                    Bitmap bmp = BitmapFactory.decodeResource(res, res.getIdentifier(
                            "e_" + image.replace('-', '_').substring(0, image.length() - 4),
                            "drawable", context.getPackageName()));

                    emojis.add(new Emoji(name, sb.toString(), bmp));
                }
            }
        } catch (JSONException e) {
            Log.e("Exception", e.toString());
        } catch (IOException e) {
            Log.e("Exception", e.toString());
        }
    }


    private final String name, value;
    private final Bitmap bmp;


    /**
     * Binds a name, unicode string, and pre-decoded bitmap. Only
     * called from {@link #load} — there's no other way to construct
     * emoji since each one has to be paired with a bitmap asset.
     */
    public Emoji(String name, String value, Bitmap bmp) {
        this.name = name;
        this.value = value;
        this.bmp = bmp;
    }


    /**
     * Returns the unicode string to commit to the input connection.
     * Used as the emoji's textual value when it is selected.
     */
    @Override
    public String toString() {
        return value;
    }


    /**
     * Returns the human-readable name (e.g. "grinning face").
     */
    public String getName() {
        return name;
    }


    /**
     * Draws the emoji's bitmap at (x, y) using {@code size} as the
     * centering offset — the bitmap itself is painted at its native
     * pixel size via the simple {@link Canvas#drawBitmap} overload,
     * so this method does not rescale. Note that if {@code size}
     * doesn't match the bitmap's actual width/height the glyph will
     * be off-center; callers pass the expected emoji size and rely
     * on the source bitmaps having been decoded at that size.
     */
    @Override
    public void draw(float x, float y, float size, Paint p, Canvas canvas) {
        canvas.drawBitmap(bmp,
                x - size / 2, y - size / 2, p);
    }


    /**
     * Alternate renderer that uses the {@link Font#EMOJI} typeface
     * instead of the bitmap asset.
     * <p>
     * How: stashes the paint's typeface and text size, switches to the
     * emoji typeface at the requested pixel size, delegates to
     * {@link Draw#text}, and restores. Currently unreachable — kept
     * as a fallback for environments where the bitmap path doesn't
     * work (this is what early NovaKey builds used before the
     * emoji-data bitmap manifest was added).
     */
    private void drawFromTTF(float x, float y, float size, Paint p, Canvas canvas) {
        Typeface tempTTF = p.getTypeface();
        float tempSize = p.getTextSize();

        p.setTypeface(Font.EMOJI);
        p.setTextSize(size);
        Draw.text(value, x, y, p, canvas);

        p.setTypeface(tempTTF);
        p.setTextSize(tempSize);
    }


}
