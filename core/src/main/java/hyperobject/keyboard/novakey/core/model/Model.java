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

package hyperobject.keyboard.novakey.core.model;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.List;

import hyperobject.keyboard.novakey.core.controller.Corrections;
import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboard;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Read/write contract the rest of the keyboard uses to talk to the
 * model layer. Holds (or exposes access to) the element list, the
 * wheel dimensions, the theme, the current keyboard layout, the shift
 * state, the input state, the cursor mode, and the corrections logic.
 * {@link MainModel} is the production implementation; actions mutate
 * state through this interface, elements and the view read from it.
 */
public interface Model {

    /**
     * Returns the ordered list of drawable/touchable elements. The first
     * entry is drawn first (so the last entry sits on top), and touch
     * dispatch walks the list top-down until one element claims the
     * gesture.
     */
    List<Element> getElements();


    /**
     * Installs {@code element} as the topmost overlay above the main
     * element — used to swap between the typing overlay, cursor overlay,
     * delete overlay, and popup menus.
     */
    void setOverlayElement(OverlayElement element);


    /**
     * Returns the live {@link MainDimensions} bag. Callers mutate this
     * object directly during the resize gesture; the next draw pass
     * picks up their edits.
     */
    MainDimensions getMainDimensions();


    /** Returns the currently active master theme. */
    MasterTheme getTheme();


    /** Replaces the currently active master theme (used when the user picks a new one). */
    void setTheme(MasterTheme theme);


    /**
     * Re-reads every loader-backed field from {@code SharedPreferences}.
     * Called from {@link #onStart} and any time the settings UI writes
     * new values.
     */
    void syncWithPrefs();


    /** Returns the live {@link InputState} for the current typing session. */
    InputState getInputState();


    /**
     * Begins a new typing session: updates the input state from
     * {@code editorInfo}, syncs prefs, and picks a sensible default
     * keyboard layout for the field type.
     */
    void onStart(EditorInfo editorInfo);


    /** Returns the concrete {@link Keyboard} that should be drawn right now. */
    Keyboard getKeyboard();


    /**
     * Returns the integer code identifying the active keyboard. Negative
     * codes are the fixed Symbols/Punctuation boards; non-negative codes
     * index into the languages list.
     */
    int getKeyboardCode();


    /**
     * Switches the active keyboard to {@code code} and refreshes the
     * overlay element so the new layout starts drawing on the next
     * frame.
     */
    void setKeyboard(int code);


    /** Returns the current shift state of the keyboard. */
    ShiftState getShiftState();


    /** Replaces the current shift state (driven by shift actions). */
    void setShiftState(ShiftState shiftState);


    /**
     * Returns the cursor-move mode: {@code 0} moves both caret endpoints
     * together, {@code -1} moves only the left endpoint, {@code 1} moves
     * only the right endpoint.
     */
    int getCursorMode();


    /**
     * Sets the cursor-move mode.
     *
     * @param cursorMode one of {@code -1}, {@code 0}, or {@code 1} —
     *                   anything outside that range throws
     *                   {@link IllegalArgumentException}
     */
    void setCursorMode(int cursorMode);


    /**
     * Returns the currently installed {@link Corrections} implementation,
     * which is what consumes and mutates the composing-text buffer.
     */
    Corrections getCorrections();
}
