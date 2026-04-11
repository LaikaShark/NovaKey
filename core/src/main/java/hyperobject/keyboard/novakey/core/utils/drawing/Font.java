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
import android.graphics.Typeface;

/**
 * Process-wide {@link Typeface} registry for NovaKey. All text drawn by
 * the keyboard pulls its font from one of these static fields; the
 * static lifetime matches the IME process, so {@link #create(Context)}
 * only needs to run once during service creation.
 * <p>
 * Holds two kinds of fonts:
 * <ul>
 *   <li>Text fonts — {@code SANS_SERIF_LIGHT} (default key labels) and
 *       {@code SANS_SERIF_CONDENSED} (caps-locked key labels).</li>
 *   <li>Icon/emoji fonts — {@code MATERIAL_ICONS}, {@code CUSTOM_ICONS},
 *       {@code EMOJI} (Noto Color Emoji), and {@code EMOJI_REGULAR}
 *       (Noto Emoji, monochrome fallback).</li>
 * </ul>
 */
public class Font {

    public static Typeface EMOJI, EMOJI_REGULAR, MATERIAL_ICONS, CUSTOM_ICONS;
    public static Typeface
            SANS_SERIF_LIGHT,
            SANS_SERIF_CONDENSED;


    /**
     * Populates every typeface field on this class. Called once at IME
     * startup.
     * <p>
     * How: the two sans-serif fonts are looked up via
     * {@link Typeface#create(String, int)} against the platform family
     * names (always available, no asset load). The icon and emoji fonts
     * are loaded from {@code assets/} via
     * {@link Typeface#createFromAsset}. The color-emoji load is wrapped
     * in a swallowing try/catch — on devices that can't decode
     * {@code NotoColorEmoji.ttf}, {@code EMOJI} is left null and the
     * code should fall back to {@code EMOJI_REGULAR}.
     */
    public static void create(Context context) {
        SANS_SERIF_LIGHT = Typeface.create("sans-serif-light", Typeface.NORMAL);
        SANS_SERIF_CONDENSED = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

        try {
            EMOJI = Typeface.createFromAsset(context.getAssets(), "NotoColorEmoji.ttf");
        } catch (Exception e) {
        }
        EMOJI_REGULAR = Typeface.createFromAsset(context.getAssets(), "NotoEmoji-Regular.ttf");
        MATERIAL_ICONS = Typeface.createFromAsset(context.getAssets(), "MaterialIcons-Regular.ttf");
        CUSTOM_ICONS = Typeface.createFromAsset(context.getAssets(), "CustomIcons.ttf");
    }
}
