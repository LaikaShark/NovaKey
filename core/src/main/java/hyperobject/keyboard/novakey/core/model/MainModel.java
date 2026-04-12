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

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.controller.BasicCorrections;
import hyperobject.keyboard.novakey.core.controller.Corrections;
import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.elements.MainElement;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboard;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.loaders.ElementsLoader;
import hyperobject.keyboard.novakey.core.model.loaders.KeyboardsLoader;
import hyperobject.keyboard.novakey.core.model.loaders.Loader;
import hyperobject.keyboard.novakey.core.model.loaders.MainDimensionsLoader;
import hyperobject.keyboard.novakey.core.model.loaders.ThemeLoader;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Production {@link Model} used by the running IME. Owns the
 * {@link MainElement} plus the flat element list, and caches the
 * dimensions/theme/keyboards that the four {@link Loader}s produce from
 * user preferences. {@link Settings} itself is the singleton source of
 * truth for simple flag/int prefs; MainModel layers the richer structured
 * state (dimensions, themes, keyboard layouts) on top.
 * <p>
 * Construction order matters: the theme / dimensions / keyboards loaders
 * are all built first, then {@link #syncWithPrefs()} primes their caches,
 * and only then is the main element built around the freshly loaded
 * default keyboard.
 */
public class MainModel implements Model {
    //Loaders
    private final Loader<List<Element>> mElementLoader;
    private final Loader<MainDimensions> mMainDimensionsLoader;
    private final Loader<MasterTheme> mThemeLoader;
    private final Loader<Keyboards> mKeyboardsLoader;

    private MainDimensions mDimensions;

    private MasterTheme mTheme;

    private ShiftState mShiftState;
    private int mCursorMode = 0;
    private InputState mInputState;

    private int mKeyboardCode = Keyboards.DEFAULT;
    private Keyboards mKeyboards;

    private MainElement mMain;
    private final List<Element> mElements;

    private Corrections mCorrections;


    /**
     * Wires up the four loaders, primes the dimensions/theme/keyboards
     * caches via {@link #syncWithPrefs()}, then constructs the input
     * state, corrections engine, {@link MainElement}, and the flat list
     * of auxiliary buttons. The initial shift state is {@link ShiftState#UPPERCASE}
     * so the very first keystroke on a fresh install is capitalized.
     */
    public MainModel(Context context) {
        mThemeLoader = new ThemeLoader(context);
        mMainDimensionsLoader = new MainDimensionsLoader(context);
        mElementLoader = new ElementsLoader();
        mKeyboardsLoader = new KeyboardsLoader(context);

        syncWithPrefs();

        mInputState = new InputState();
        mCorrections = new BasicCorrections();
        mCorrections.initialize(context);

        mShiftState = ShiftState.UPPERCASE;


        mMain = new MainElement(getKeyboard());
        mElements = new ArrayList<>();
        List<Element> btns = mElementLoader.load();
        mElements.addAll(btns);
    }


    /**
     * Re-reads dimensions, theme, and keyboards from the loaders. Called
     * from the constructor and from {@link #onStart} so every new typing
     * session picks up settings changes.
     */
    @Override
    public void syncWithPrefs() {
        mDimensions = mMainDimensionsLoader.load();
        mTheme = mThemeLoader.load();
        mKeyboards = mKeyboardsLoader.load();
    }


    /**
     * Returns a fresh copy of the element list with {@link MainElement}
     * prepended — the main element always draws first / handles touches
     * last, so placing it at index 0 satisfies both orderings at once.
     */
    @Override
    public List<Element> getElements() {
        List<Element> list = new ArrayList<>(mElements);
        list.add(0, mMain);//first element
        return list;
    }


    /** Forwards the overlay swap to the owning {@link MainElement}. */
    @Override
    public void setOverlayElement(OverlayElement element) {
        mMain.setOverlay(element);
    }


    /** Returns the cached {@link MainDimensions} bag (mutated live during resize). */
    @Override
    public MainDimensions getMainDimensions() {
        return mDimensions;
    }


    /** Returns the cached master theme. */
    @Override
    public MasterTheme getTheme() {
        return mTheme;
    }


    /** Replaces the cached master theme. */
    @Override
    public void setTheme(MasterTheme theme) {
        mTheme = theme;
    }


    /** Returns the live {@link InputState} for the current session. */
    @Override
    public InputState getInputState() {
        return mInputState;
    }


    /**
     * Begins a typing session: refreshes the input state from
     * {@code editorInfo}, re-syncs prefs, picks a starting keyboard
     * for the field type (text → alphabet, everything else →
     * punctuation), and seeds the shift state from the editor's
     * autocap flags via {@link #initialShiftState}.
     */
    @Override
    public void onStart(EditorInfo editorInfo) {
        mInputState.updateEditorInfo(editorInfo);
        syncWithPrefs();

        switch (mInputState.getType()) {
            default:
            case TEXT:
                setKeyboard(Keyboards.DEFAULT);
                break;
            case NUMBER:
                setKeyboard(Keyboards.PUNCTUATION);
                break;
            case PHONE:
                setKeyboard(Keyboards.PUNCTUATION);
                break;
            case DATETIME:
                setKeyboard(Keyboards.PUNCTUATION);
                break;
        }

        setShiftState(initialShiftState(editorInfo));
    }


    /**
     * Maps an {@link EditorInfo}'s autocap flags to the starting
     * {@link ShiftState} for a fresh session: TYPE_TEXT_FLAG_CAP_CHARACTERS
     * → {@link ShiftState#CAPS_LOCKED}, TYPE_TEXT_FLAG_CAP_SENTENCES or
     * TYPE_TEXT_FLAG_CAP_WORDS → {@link ShiftState#UPPERCASE} (a
     * one-shot shift; the existing space-action path re-triggers it
     * after each space for the WORDS variant), and otherwise
     * {@link ShiftState#LOWERCASE}. Non-text fields force LOWERCASE
     * since shift has no meaning on the punctuation/symbols keyboards.
     */
    private ShiftState initialShiftState(EditorInfo editorInfo) {
        if (mInputState.getType() != InputState.Type.TEXT)
            return ShiftState.LOWERCASE;
        int it = editorInfo.inputType;
        if ((it & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0)
            return ShiftState.CAPS_LOCKED;
        if ((it & (InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS)) != 0)
            return ShiftState.UPPERCASE;
        return ShiftState.LOWERCASE;
    }


    /** Resolves the current keyboard code to the concrete {@link Keyboard}. */
    @Override
    public Keyboard getKeyboard() {
        return mKeyboards.get(getKeyboardCode());
    }


    /** Returns the integer code identifying the active keyboard. */
    @Override
    public int getKeyboardCode() {
        return mKeyboardCode;
    }


    /**
     * Switches to {@code code} and pushes the new keyboard into the main
     * element's overlay slot so drawing and touch dispatch both pick it
     * up immediately.
     */
    @Override
    public void setKeyboard(int code) {
        mKeyboardCode = code;
        setOverlayElement(getKeyboard());
    }


    /** Returns the current shift state. */
    @Override
    public ShiftState getShiftState() {
        return mShiftState;
    }


    /** Replaces the current shift state. */
    @Override
    public void setShiftState(ShiftState shiftState) {
        this.mShiftState = shiftState;
    }


    /**
     * Returns the cursor-move mode: {@code 0} moves both caret endpoints,
     * {@code -1} moves only the left endpoint, {@code 1} moves only the
     * right endpoint.
     */
    @Override
    public int getCursorMode() {
        return mCursorMode;
    }


    /**
     * Sets the cursor-move mode after validating it lies in [-1, 1] —
     * any out-of-range value throws {@link IllegalArgumentException}
     * rather than silently normalizing.
     */
    @Override
    public void setCursorMode(int cursorMode) {
        if (cursorMode < -1 || cursorMode > 1)
            throw new IllegalArgumentException(cursorMode + " is outside the range [-1, 1]");
        mCursorMode = cursorMode;
    }


    /** Returns the installed {@link Corrections} strategy. */
    @Override
    public Corrections getCorrections() {
        return mCorrections;
    }
}
