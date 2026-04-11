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

package hyperobject.keyboard.novakey.core.model.factories;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.view.themes.BaseMasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BaseTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.DonutTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.IconTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.MaterialTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.MulticolorDonutTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.MulticolorTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.SeparateSectionsTheme;

/**
 * Serializes and deserializes {@link MasterTheme}s for the theme
 * preference. Themes round-trip through a JSON blob of the shape
 * <pre>
 * {
 *   "colors": [primary, accent, contrast],
 *   "is3D":   bool,
 *   "board":  { "class": int },
 *   "background": { ... },
 *   "button":     { ... }
 * }
 * </pre>
 * The integer {@code class} lives in the {@link Boards} registry below,
 * so prefs can reference a board theme stably without storing a Java
 * class name. Also ships a legacy-string parser used by
 * {@code Settings.fixLegacyThemeing}.
 */
public class ThemeFactory {

    /** Registry of numbered board-theme implementations used for JSON serialization. */
    public static Boards BOARDS = new Boards();


    /**
     * Parses a JSON theme blob (see class docs for the schema) into a
     * {@link MasterTheme}. Returns a fresh {@link BaseMasterTheme} if
     * {@code str} is the "DEFAULT" sentinel or if parsing fails.
     * Background/button sections are parsed but currently unused —
     * they're parsed defensively so future writes won't break the
     * current reader.
     */
    public static MasterTheme themeFromString(String str) {
        if (str.equals(Settings.DEFAULT)) {
            return new BaseMasterTheme();
        }
        MasterTheme theme = new BaseMasterTheme();
        try {
            JSONObject obj = new JSONObject(str);
            JSONArray colors = obj.getJSONArray("colors");
            theme.setColors(
                    colors.getInt(0),
                    colors.getInt(1),
                    colors.getInt(2))
                    .set3D(obj.getBoolean("is3D"));
            //board
            JSONObject board = obj.getJSONObject("board");
            theme.setBoardTheme(BOARDS.getValue(board.getInt("class")));
            //background
            JSONObject background = obj.getJSONObject("background");

            //button
            JSONObject button = obj.getJSONObject("button");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return theme;
    }


    /**
     * Opposite of {@link #themeFromString}: walks a live
     * {@link MasterTheme} and produces the JSON blob that will be
     * written to {@code Settings.pref_theme}. Returns the {@code DEFAULT}
     * sentinel on any JSON error so the next read round-trips safely.
     */
    public static String stringFromTheme(MasterTheme theme) {
        try {
            //master
            JSONObject master = new JSONObject();
            JSONArray colors = new JSONArray()
                    .put(theme.getPrimaryColor())
                    .put(theme.getAccentColor())
                    .put(theme.getContrastColor());
            master.put("colors", colors);
            master.put("is3D", theme.is3D());
            //board
            JSONObject board = new JSONObject();
            board.put("class", BOARDS.getKey(theme.getBoardTheme().getClass()));
            //background
            JSONObject background = new JSONObject();

            //button
            JSONObject button = new JSONObject();

            //add all
            master.put("board", board);
            master.put("background", background);
            master.put("button", button);

            return master.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Settings.DEFAULT;
    }


    /**
     * Parses the pre-1.0 comma-separated theme string into a
     * {@link MasterTheme}. The legacy format is
     * {@code t,c1,c2,c3,A,3d} where {@code t} is the board id,
     * {@code c1/c2/c3} are primary/accent/contrast colors, and
     * {@code A}/{@code 3d} are text flags for auto-color and 3D mode.
     * Auto-color is intentionally dropped here because it's lifted into
     * its own pref by the migration caller; 3D and board id are
     * applied directly.
     */
    public static MasterTheme themeFromLegacyString(String str) {
        if (str.equals(Settings.DEFAULT)) {
            return new BaseMasterTheme();
        }
        String[] params = str.split(",");

        MasterTheme theme = new BaseMasterTheme();
        theme.setColors(Integer.valueOf(params[1]),
                Integer.valueOf(params[2]),
                Integer.valueOf(params[3]));
        //don't care about auto color
        theme.set3D(params[5].equalsIgnoreCase("3d"));
        theme.setBoardTheme(BOARDS.getValue(Integer.parseInt(params[0])));
        return theme;
    }


    /** Shortcut: returns the numeric id for the given {@link BoardTheme}'s class. */
    public static int getBoardNum(BoardTheme theme) {
        return BOARDS.getKey(theme.getClass());
    }


    /** Shortcut: returns a fresh {@link BoardTheme} instance for the given numeric id. */
    public static BoardTheme getBoard(int num) {
        return BOARDS.getValue(num);
    }


    /**
     * Numbered catalog of every shipped {@link BoardTheme}. The integer
     * id is the over-the-wire identifier stashed in the theme JSON blob,
     * so <strong>reorder with care</strong> — changing an existing id
     * silently remaps every user's current theme.
     */
    public static class Boards extends InstanceList<BoardTheme> {

        /**
         * Registers the seven built-in board themes by fixed id. Adding
         * a new theme: append it with the next unused integer; do not
         * renumber the existing entries.
         */
        @Override
        protected void build(Map<Integer, Class> map) {
            map.put(0, BaseTheme.class);
            map.put(1, MaterialTheme.class);
            map.put(2, SeparateSectionsTheme.class);
            map.put(3, DonutTheme.class);
            map.put(4, MulticolorDonutTheme.class);
            map.put(5, MulticolorTheme.class);
            map.put(6, IconTheme.class);
        }
    }


}
