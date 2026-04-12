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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

/**
 * Miscellaneous static helpers used all over the keyboard: trigonometry
 * for the polar wheel layout, string munging for text entry, colour
 * math for contrast/shade adjustments, and a couple of animation
 * composition helpers.
 * <p>
 * Everything is stateless and side-effect free except where noted.
 * Related drawing utilities live next door in
 * {@link hyperobject.keyboard.novakey.core.utils.drawing} — this class
 * is the junk drawer for logic that didn't fit anywhere else.
 */
public class Util {

    /**
     * Euclidean distance between two points, returned as a {@code float}
     * because almost every caller is already working in screen pixels.
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }


    /**
     * Convenience for building a square {@link RectF} of half-edge
     * {@code r} centred on {@code (x, y)} — the shape most wheel
     * drawables want when asked to render into a bounding box.
     */
    public static RectF square(float x, float y, float r) {
        return new RectF(x - r, y - r, x + r, y + r);
    }


    /**
     * Angle of the vector from centre {@code (cx, cy)} to point
     * {@code (x, y)} in standard screen coordinates (y grows downward).
     * Returns a value in {@code (-π, π]}; use {@link #getAngle} if you
     * want the {@code [0, 2π)} normalised version instead.
     */
    public static double angle(float cx, float cy, float x, float y) {
        return Math.atan2(y - cy, x - cx);
    }


    /**
     * Angle of {@code (x, y)} relative to the origin, normalised into
     * {@code [0, 2π)} by wrapping the negative half of {@code atan2}
     * around.
     */
    public static float getAngle(float x, float y) {
        double angle = Math.atan2(y, x);
        return (float) (angle < 0 ? Math.PI * 2 + angle : angle);
    }


    /**
     * Polar-to-cartesian x: walks {@code r} units along angle {@code a}
     * from centre x {@code cx}.
     */
    public static float xFromAngle(float cx, float r, double a) {
        return cx + (float) (Math.cos(a) * r);
    }


    /**
     * Polar-to-cartesian y: walks {@code r} units along angle {@code a}
     * from centre y {@code cy}, flipping the sign so "up" (positive
     * angle) moves visually upward in Android's y-grows-down coords.
     */
    public static float yFromAngle(float cy, float r, double a) {
        return cy - (float) (Math.sin(a) * r);
    }


    /**
     * Capitalises the first character of {@code text}, leaving the rest
     * untouched. Returns the original string unchanged on empty input
     * and falls back to just the capitalised first character if the
     * substring call blows up.
     */
    public static String capsFirst(String text) {
        if (text.isEmpty())
            return text;
        Character first = text.charAt(0);
        first = Character.toUpperCase(first);
        try {
            return first + text.substring(1);
        } catch (Exception e) {
            return first.toString();
        }
    }


    /**
     * Title-cases every word in {@code text} — the first character and
     * every character that immediately follows a space is upper-cased.
     * Uses naive string concatenation; fine for the short strings the
     * keyboard handles.
     */
    public static String uppercaseFirst(String text) {
        String res = "";
        for (int i = 0; i < text.length(); i++) {
            if (i == 0 || text.charAt(i - 1) == ' ')
                res += Character.toUpperCase(text.charAt(i));
            else
                res += text.charAt(i);
        }
        return res;
    }


    /**
     * Returns the index of the n-th occurrence of {@code c} in
     * {@code s}, 1-indexed. Implemented recursively: for {@code n <= 1}
     * falls through to {@link String#indexOf}; otherwise strips off
     * everything up to the first match and recurses on the remainder,
     * adding the stripped length back in to restore an absolute index.
     */
    public static int nthIndexOf(String s, int n, char c) {
        if (n <= 1)
            return s.indexOf(c);
        else {
            String sub = s.substring(s.indexOf(c) + 1);
            return (s.length() - sub.length()) + nthIndexOf(sub, n - 1, c);
        }
    }


    /**
     * Inserts newlines into {@code str} so that no rendered line
     * exceeds {@code max} pixels wide when measured with {@code p}.
     * <p>
     * How: scans character by character, remembering the most recent
     * space. When the measured width hits {@code max} it breaks at the
     * last space; if there was no space since the current line started
     * it hard-breaks mid-word and rewinds the scan so the next measured
     * line starts fresh.
     */
    public static String toMultiline(String str, Paint p, float max) {
        if (max > 0) {
            int s = 0;
            int lastSpace = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == ' ')
                    lastSpace = i;
                float len = p.measureText(str, s, i);
                if (len >= max) {
                    if (lastSpace <= s) {//cut long word
                        str = newLineAt(str, i);
                        s = i + 1;
                        i--;
                    } else {
                        str = newLineAt(str, lastSpace);
                        s = lastSpace + 1;
                    }
                }
            }
        }
        return str;
    }


    /**
     * Returns {@code str} with a newline inserted after position
     * {@code index}, consuming one space if the next character is one
     * (so the break point doesn't leave an orphan space at the start of
     * the new line). Returns {@code str} unchanged if {@code index} is
     * at or past the final character.
     */
    public static String newLineAt(String str, int index) {
        if (index < str.length() - 1) {
            String prev = str.substring(0, index + 1),
                    next = str.substring(index + 1);
            if (next.length() > 0 && next.charAt(0) == ' ')
                return prev + '\n' + next.substring(1);
            return prev + '\n' + next;
        }
        return str;
    }


    /**
     * Character-count based multiline wrap: breaks {@code s} so each
     * line has at most {@code lineMax} characters. Splits on spaces
     * first and then chops any single word longer than {@code lineMax}
     * into hard-wrapped chunks. Note that this overload does not share
     * any code with the Paint-based one above.
     */
    public static String toMultiline(String s, int lineMax) {
        String[] S = s.split(" ");
        String res = "";
        for (int i = 0; i < S.length; i++) {
            String[] divStr = new String[S[i].length() / lineMax + 1];
            for (int j = 0; j < divStr.length; j++) {
                int start = lineMax * j, end = lineMax * (j + 1);
                String add = S[i].substring(start, end > S[i].length() ? S[i].length() : end);

                String[] lines = res.split("\n");
                String currLine = lines[lines.length - 1];
                if (currLine.length() + add.length() > lineMax)
                    res += "\n" + add + " ";
                else
                    res += add + " ";
            }

        }
        return res;
    }


    /**
     * Number of non-overlapping occurrences of {@code match} inside
     * {@code str}, counted by sliding a fixed-width window across the
     * haystack. Special-cases an exact equality so identical strings
     * report at least one match.
     */
    public static int countMatches(String str, String match) {
        int count = 0;
        if (match.length() < str.length()) {
            for (int i = 0; i < str.length() - (match.length() - 1); i++) {
                if (str.substring(i, i + match.length()).equals(match))
                    count++;
            }
        }
        if (match.equals(str))
            count++;
        return count;
    }


    /** True if {@code keyCode} is an ASCII digit 0–9. */
    public static boolean isNumber(int keyCode) {
        return keyCode >= '0' && keyCode <= '9';
    }


    /**
     * Brightens or darkens a colour by {@code f} shade steps (one step
     * is 7.5%). Pure black is bumped to {@code 0xFF202020} first so
     * that multiplication actually has an effect, and the result is
     * clamped through {@link #redestributeRGB} to preserve hue when
     * any channel would otherwise overflow.
     */
    public static int colorShade(int c, int f) {
        if (c == Color.BLACK)
            c = 0xFF202020;
        float mult = 1 + f * .075f;
        return redestributeRGB((int) (Color.red(c) * mult), (int) (Color.green(c) * mult),
                (int) (Color.blue(c) * mult));
    }


    /** Clamps each of {@code r, g, b} to 255 before packing into ARGB. */
    private static int clampRGB(int r, int g, int b) {
        return Color.argb(255, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }


    /**
     * Colour-safe brightness scaling: if {@code (r, g, b)} all fit in
     * one byte returns them as-is, otherwise blends the over-range
     * colour toward white by the overflow amount so the hue is
     * preserved instead of one channel clipping to 255 alone. Result is
     * always opaque ARGB.
     */
    private static int redestributeRGB(int r, int g, int b) {
        int m = Math.max(r, Math.max(g, b));
        if (m <= 255)
            return Color.argb(255, r, g, b);
        int total = r + g + b;
        if (total >= 3 * 255)
            return Color.argb(255, 255, 255, 255);//white
        int x = (3 * 255 - total) / (3 * m - total);
        int gray = 255 - x * m;
        return Color.argb(255, gray + x * r, gray + x * g, gray + x * b);
    }


    /**
     * True if white text contrasts better than black on {@code color}.
     * Uses the YIQ-weighted luminance heuristic (R*299 + G*587 + B*114)
     * and picks white whenever that weighted value is below 128.
     */
    private static boolean whiteDoesContrast(int color) {
        float yiq = relativeLuminance(color);
        return yiq < 128;
    }


    /**
     * Returns {@link Color#WHITE} or {@link Color#BLACK}, whichever is
     * easier to read against {@code color}, using the YIQ heuristic in
     * {@link #whiteDoesContrast}.
     */
    public static int contrastColor(int color) {
        return whiteDoesContrast(color) ? Color.WHITE : Color.BLACK;
    }


    /**
     * Returns the YIQ-weighted relative luminance of an ARGB colour in
     * the range {@code [0, 255]}. Used by the contrast helpers; not a
     * true WCAG luminance (it skips the sRGB gamma step).
     */
    public static float relativeLuminance(int color) {
        return (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
    }


    /**
     * Symmetric luminance-ratio between two colours — always {@code >= 1}
     * regardless of argument order, so callers don't have to think
     * about which colour is lighter.
     */
    public static float contrastRatio(int color1, int color2) {
        float l1 = relativeLuminance(color1),
                l2 = relativeLuminance(color2);
        return Math.max(l1 / l2, l2 / l1);
    }


    /**
     * Picks the best foreground colour for a given background. Prefers
     * {@code preferred} if its contrast ratio clears the 1.1 threshold;
     * otherwise falls back to whichever of {@code preferred}/{@code secondary}
     * has the higher ratio.
     */
    public static int bestColor(int preferred, int secondary, int background) {
        float r1 = contrastRatio(preferred, background);
        if (r1 >= 1.1f)
            return preferred;
        else {
            float r2 = contrastRatio(secondary, background);
            return r2 > r1 ? secondary : preferred;
        }
    }


    /**
     * Overload that sequences animators with a uniform delay between
     * each. Equivalent to {@code sequence(anims, delay, null)}; see
     * the three-arg version — note that a {@code null} skip list will
     * NPE the 3-arg implementation.
     */
    public static AnimatorSet sequence(Animator[] anims, long delay) {
        return sequence(anims, delay, null);
    }


    /**
     * Builds an {@link AnimatorSet} that plays {@code anims} back to
     * back with {@code delay} ms between most steps. Indices listed in
     * {@code skipDelayAt} have their delay suppressed so the next
     * animator starts immediately after the previous one instead of
     * waiting. If there's only one animator the set just plays it.
     */
    public static AnimatorSet sequence(Animator[] anims, long delay, int[] skipDelayAt) {
        AnimatorSet set = new AnimatorSet();
        if (anims.length == 1) {
            set.play(anims[0]);
            return set;
        }
        int skipMax = skipDelayAt.length;
        int skip = 0;
        for (int i = 0; i < anims.length - 1; i++) {
            if (skip < skipMax && skipDelayAt[skip] == i) {
            } else {
                anims[i + 1].setStartDelay(delay);
            }
            set.play(anims[i]).before(anims[i + 1]);
        }
        return set;
    }


    /**
     * Linear interpolation from {@code beg} to {@code end} by fraction
     * {@code frac} in {@code [0, 1]}. Used pervasively by the animation
     * helpers for time-parameterised tweens.
     */
    public static float fromFrac(float beg, float end, float frac) {
        return beg + (end - beg) * frac;
    }

}
