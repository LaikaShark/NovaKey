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

package hyperobject.keyboard.novakey.core.elements.keyboards;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.input.KeyAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.InfiniteMenu;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.ShiftState;
import hyperobject.keyboard.novakey.core.utils.drawing.Font;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.TextDrawable;
import hyperobject.keyboard.novakey.core.view.posns.DeltaPosn;
import hyperobject.keyboard.novakey.core.view.posns.RadiiPosn;
import hyperobject.keyboard.novakey.core.view.posns.RelativePosn;
import hyperobject.keyboard.novakey.core.view.posns.SmallRadiusPosn;

/**
 * One character on a {@link Keyboard}, with the geometry that places it
 * on the wheel. Each key belongs to a {@code group} (0 for the center
 * cluster, 1–5 for the five outer sectors) and a {@code loc} (its slot
 * within that group).
 * <p>
 * A {@code Key} is also an {@link Action} — when the typing handler
 * commits a gesture to a specific key, it fires the key through the
 * controller, and {@link #trigger} forwards to the pre-built
 * {@link KeyAction} that actually inserts the character. Caching a
 * single {@code KeyAction} per key avoids allocating one per tap.
 */
public class Key implements Action<Void> {

    private final Character mChar;
    private final KeyAction mInput;//set just 1 to save memory during runtime
    public final int group, loc;
    private final boolean mAltLayout;

    private RelativePosn mPosn;
    private float mSize = 1;
    private final TextDrawable mDrawable;


    /** Convenience constructor for non-alt-layout keys. */
    public Key(Character c, int group, int loc) {
        this(c, group, loc, false);
    }


    /**
     * Builds a key with character, group/loc address, and alt-layout flag.
     * Pre-computes its resting position via {@link #getDesiredPosn} and
     * its drawable so the render path doesn't allocate per frame.
     *
     * @param c         the character this key inputs
     * @param group     0 for center cluster, 1–5 for outer sectors
     * @param loc       slot within the group (semantics depend on group)
     * @param altLayout whether this key lives on an alt layout — shifts
     *                  the "outermost" slot from loc 4 to loc 0 so wider
     *                  groups pack differently
     */
    public Key(Character c, int group, int loc, boolean altLayout) {
        mChar = c;
        mInput = new KeyAction(mChar);
        this.group = group;
        this.loc = loc;
        mAltLayout = altLayout;
        mPosn = getDesiredPosn();
        mDrawable = new TextDrawable(mChar.toString(), Font.SANS_SERIF_LIGHT);
    }


    /** Returns this key's character uppercased. */
    public Character getUppercase() {
        return Character.toUpperCase(mChar);
    }


    /** Returns this key's character lowercased. */
    public Character getLowercase() {
        return Character.toLowerCase(mChar);
    }


    /** Returns the current relative position used for drawing. */
    public RelativePosn getPosn() {
        return mPosn;
    }


    /**
     * Overrides the resting position, e.g. when {@link KeySizeAnimator}
     * repositions keys mid-animation.
     */
    public void setPosn(RelativePosn posn) {
        mPosn = posn;
    }


    /**
     * Returns the current size multiplier (1 = default, 2 = double, etc.).
     * Drawing multiplies this by the board radius to get a pixel size.
     */
    public float getSize() {
        return mSize;
    }


    /** Sets the size multiplier; see {@link #getSize} for units. */
    public void setSize(float size) {
        mSize = size;
    }


    /**
     * Returns the cached drawable with its text and font updated for the
     * given shift state. CAPS_LOCKED uses a bolder condensed font to
     * distinguish it from transient uppercase; upper vs lower only
     * changes the displayed text. The underlying {@code TextDrawable}
     * instance is reused across frames, so this is allocation-free.
     */
    public TextDrawable getDrawable(ShiftState shiftState) {
        mDrawable.setFont(shiftState == ShiftState.CAPS_LOCKED ?
                Font.SANS_SERIF_CONDENSED : Font.SANS_SERIF_LIGHT);

        mDrawable.setText(shiftState == ShiftState.LOWERCASE ?
                getLowercase().toString() : getUppercase().toString());

        return mDrawable;
    }


    /** Returns the raw character this key represents (always the source case). */
    public char getChar() {
        return mChar;
    }


    /**
     * Computes where this key should sit on the wheel based on its
     * {@code group}/{@code loc} address.
     * <p>
     * How: group 0 is the center cluster — loc 0 sits at the dead center
     * (delta (0,0)), other locs sit at 2/3 of the small radius at the
     * angle for that loc. Groups 1–5 are the outer sectors — loc 2 is
     * tucked just inside the inner radius, loc 4 (or loc 0 for alt
     * layouts) is just past the outer radius, loc 0 is just inside the
     * outer radius, and everything else rides the midline between the
     * two radii.
     */
    private RelativePosn getDesiredPosn() {
        if (group == 0) {
            if (loc == 0)
                return new DeltaPosn(0, 0);
            else {
                return new SmallRadiusPosn(2f / 3f, getAngle());
            }
        } else {
            if (loc == 2)
                return new RadiiPosn(1f / 6f, getAngle());
            if (loc == (mAltLayout ? 0 : 4))
                return new RadiiPosn(1f + 1f / 6f, getAngle());
            if (loc == 0)
                return new RadiiPosn(1f - 1f / 6f, getAngle());
            return new RadiiPosn(.5f, getAngle());
        }
    }


    /**
     * Returns the radial angle (in radians) for this key. Keys at loc 0
     * sit on the sector's midline; locs 1 and 3 are offset ±2/3 of the
     * sector's angular width to splay them apart without overlapping
     * the neighboring sectors.
     */
    private double getAngle() {
        if (group == 0)
            return angleAt(loc);
        double areaWidth = Math.PI / 5;
        if (loc == 3)
            return angleAt(group) - 2 * areaWidth / 3;
        if (loc == 1)
            return angleAt(group) + 2 * areaWidth / 3;
        return angleAt(group);
    }


    /**
     * Returns the center angle for sector {@code i} (1-indexed), offset
     * so sector 1 sits at the top (π/2 + π/5) and sectors progress
     * counterclockwise by 2π/5 each.
     */
    private double angleAt(int i) {
        return ((i - 1) * 2 * Math.PI) / 5 + Math.PI / 2 + Math.PI / 5;
    }


    /**
     * Returns the hidden-keys popup menu for this key in the given shift
     * state, or {@code null} if there's no hidden menu for this character.
     * Upper/caps-locked use the uppercase variant so e.g. long-press on
     * 'A' shows capital accented variants, not lowercase ones.
     */
    public InfiniteMenu getHiddenKeys(ShiftState shiftState) {
        switch (shiftState) {
            default:
            case LOWERCASE:
                return InfiniteMenu.getHiddenKeys(getLowercase());
            case UPPERCASE:
            case CAPS_LOCKED:
                return InfiniteMenu.getHiddenKeys(getUppercase());
        }
    }


    /**
     * {@link Action} implementation: fires the cached {@link KeyAction}
     * through the controller, which actually inserts the character into
     * the IME's input connection.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        return control.fire(mInput);
    }
}
