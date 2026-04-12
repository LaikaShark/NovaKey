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

package hyperobject.keyboard.novakey.core.view.themes;

import hyperobject.keyboard.novakey.core.view.themes.background.BackgroundTheme;
import hyperobject.keyboard.novakey.core.view.themes.background.FlatBackgroundTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BaseTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;
import hyperobject.keyboard.novakey.core.view.themes.button.BaseButtonTheme;
import hyperobject.keyboard.novakey.core.view.themes.button.ButtonTheme;

/**
 * Default implementation of {@link MasterTheme}. Wires up a sensible set
 * of sub-themes on construction — {@link BaseTheme} for the board,
 * {@link FlatBackgroundTheme} for the area behind the wheel, and
 * {@link BaseButtonTheme} for the fixed buttons — and seeds the three
 * color slots with neutral gray/white values. Callers then swap in
 * concrete board/button/background variants (e.g. {@link DonutTheme},
 * {@link MulticolorTheme}, …) and override the colors via
 * {@link #setColors}.
 */
public class BaseMasterTheme implements MasterTheme {

    private boolean mIs3d;
    private int mPrimiary, mAccent, mContrast;

    private BoardTheme mBoard;
    private BackgroundTheme mBackground;
    private ButtonTheme mButton;


    /**
     * Builds a default master theme: base board/background/button
     * children, 3D off, and a neutral gray primary with near-white
     * accent and contrast.
     */
    public BaseMasterTheme() {
        setBoardTheme(new BaseTheme());
        setBackgroundTheme(new FlatBackgroundTheme());
        setButtonTheme(new BaseButtonTheme());

        mIs3d = false;
        mPrimiary = 0xFF616161;
        mAccent = 0xFFF5F5F5;
        mContrast = 0xFFF5F5F5;
    }


    /**
     * Stores the 3D flag. Children read this back on each draw, so the
     * change takes effect on the next frame.
     */
    @Override
    public MasterTheme set3D(boolean is3D) {
        mIs3d = is3D;
        return this;
    }


    /** @return the currently stored 3D flag */
    @Override
    public boolean is3D() {
        return mIs3d;
    }


    /**
     * Writes all three color slots at once. Returns {@code this} so the
     * caller can chain further setup calls.
     */
    @Override
    public MasterTheme setColors(int primary, int accent, int contrast) {
        mPrimiary = primary;
        mAccent = accent;
        mContrast = contrast;
        return this;
    }


    /** @return the primary color slot */
    @Override
    public int getPrimaryColor() {
        return mPrimiary;
    }


    /** Overwrites just the primary color slot. */
    @Override
    public void setPrimaryColor(int color) {
        mPrimiary = color;
    }


    /** @return the accent color slot */
    @Override
    public int getAccentColor() {
        return mAccent;
    }


    /** Overwrites just the accent color slot. */
    @Override
    public void setAccentColor(int color) {
        mAccent = color;
    }


    /** @return the contrast color slot */
    @Override
    public int getContrastColor() {
        return mContrast;
    }


    /** Overwrites just the contrast color slot. */
    @Override
    public void setContrastColor(int color) {
        mContrast = color;
    }


    /**
     * Stores the new board sub-theme and wires {@code this} as its
     * parent so the child can read colors back during draw.
     */
    @Override
    public MasterTheme setBoardTheme(BoardTheme boardTheme) {
        mBoard = boardTheme;
        mBoard.setParent(this);
        return this;
    }


    /** @return the currently installed board sub-theme */
    @Override
    public BoardTheme getBoardTheme() {
        return mBoard;
    }


    /**
     * Stores the new button sub-theme and wires {@code this} as its
     * parent so the child can read colors back during draw.
     */
    @Override
    public MasterTheme setButtonTheme(ButtonTheme buttonTheme) {
        mButton = buttonTheme;
        mButton.setParent(this);
        return this;
    }


    /** @return the currently installed button sub-theme */
    @Override
    public ButtonTheme getButtonTheme() {
        return mButton;
    }


    /**
     * Stores the new background sub-theme and wires {@code this} as its
     * parent so the child can read colors back during draw.
     */
    @Override
    public MasterTheme setBackgroundTheme(BackgroundTheme backgroundTheme) {
        mBackground = backgroundTheme;
        mBackground.setParent(this);
        return this;
    }


    /** @return the currently installed background sub-theme */
    @Override
    public BackgroundTheme getBackgroundTheme() {
        return mBackground;
    }
}
