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

package hyperobject.keyboard.novakey.core.actions.input;


import android.view.inputmethod.InputConnection;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetKeyboardAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.model.ShiftState;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * The "a user pressed a letter-or-symbol key" action. Takes the
 * character the key represents and figures out exactly how to insert
 * it given the current shift state, auto-correct/auto-capitalize
 * settings, and a few character-specific auto-behaviors:
 * <ul>
 *   <li><b>Auto-correct:</b> regular letters/digits go through the
 *       IME's correction pipeline (composing text) instead of being
 *       hard-committed.</li>
 *   <li><b>Quick-insert pairs:</b> typing an opener from {@code ¿¡⌊⌈}
 *       automatically types the matching closer and leaves the caret
 *       between them.</li>
 *   <li><b>Quick-close pairs:</b> typing {@code ( [ { < > | "} cycles
 *       through three states on repeated taps — just the opener, then
 *       also the closer with the caret moved back, then replace with
 *       a doubled opener (for unusual inputs that need the raw char).</li>
 *   <li><b>Return-after-space:</b> certain punctuation characters
 *       ({@code . , ; & ! ?}) flag the input state so that the next
 *       space press automatically returns to the default alphabet
 *       keyboard.</li>
 *   <li><b>Quote/inverted-punctuation shortcut:</b> on a first press
 *       of {@code ' " ¿ ¡}, the keyboard snaps back to the default
 *       alphabet layout on the spot.</li>
 * </ul>
 * Always fires an {@link UpdateShiftAction} at the end so the shift
 * state can advance (e.g. drop out of UPPERCASE after one character).
 */
public class KeyAction implements Action<Void> {

    //return after space
    private final Character[] returnAfterSpace = new Character[]
            {'.', ',', ';', '&', '!', '?'};


    /**
     * Membership test: is {@code c} in the {@code returnAfterSpace}
     * whitelist of punctuation that should switch the keyboard back to
     * the alphabet after the next space?
     */
    public boolean shouldReturnAfterSpace(Character c) {
        for (Character C : returnAfterSpace) {
            if (C == c)
                return true;
        }
        return false;
    }


    private static char[] openers = new char[]{'¿', '¡', '⌊', '⌈'},
            closers = new char[]{'?', '!', '⌋', '⌉'};


    /**
     * Maps a quick-insert opener to its matching closer, or returns 0
     * if {@code c} is not a known opener.
     */
    private static char getCloser(int c) {
        for (int i = 0; i < openers.length; i++) {
            if (openers[i] == c)
                return closers[i];
        }
        return 0;
    }


    /**
     * True if {@code c} is one of the quick-insert openers
     * ({@code ¿ ¡ ⌊ ⌈}).
     */
    private static boolean isOpener(int c) {
        for (int i : openers) {
            if (i == c)
                return true;
        }
        return false;
    }


    private static char[] quickOpeners = new char[]{'(', '[', '{', '<', '>', '|', '\"'},// the | is used for absolute
            quickClosers = new char[]{')', ']', '}', '>', '<', '|', '\"'};//  value so its in


    /**
     * Maps a quick-close opener to its matching closer, or returns 0
     * if {@code c} is not a quick-close opener.
     */
    private static char getQuickCloser(int c) {
        for (int i = 0; i < quickOpeners.length; i++) {
            if (quickOpeners[i] == c)
                return quickClosers[i];
        }
        return (char) 0;
    }


    /**
     * True if {@code c} is one of the quick-close openers
     * ({@code ( [ { < > | "}).
     */
    private static boolean isQuickOpener(int c) {
        for (int i : quickOpeners) {
            if (i == c)
                return true;
        }
        return false;
    }


    private final Character mChar;


    /**
     * @param character the character this key represents (in its
     *                  canonical / uppercase form; shift state is
     *                  applied at fire time)
     */
    public KeyAction(Character character) {
        mChar = character;
    }


    /**
     * Inserts the key's character using the appropriate pathway.
     * <p>
     * How: branches on auto-correct eligibility, then quick-insert,
     * then quick-close, then a plain insert. In every branch the
     * character is first cased according to the current
     * {@link ShiftState} (LOWERCASE → lowercase, UPPERCASE/CAPS_LOCKED
     * → uppercase). After insertion, updates
     * {@link InputState#setReturnAfterSpace} for the return-after-space
     * whitelist, fires a keyboard-reset on the quote/inverted-punctuation
     * shortcut, and fires an {@link UpdateShiftAction}.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        InputConnection ic = ime.getCurrentInputConnection();
        InputState state = model.getInputState();
        boolean regText = state.shouldAutoCorrect();

        //TODO: insert keys normally
        // Regular text with autocorrecting
        if (!regText && Settings.autoCorrect && (Character.isLetter(mChar) || Util.isNumber(mChar))) {
            char c;
            if (model.getShiftState() == ShiftState.LOWERCASE) {
                c = Character.toLowerCase(mChar);
            } else {
                c = Character.toUpperCase(mChar);
            }
            ime.inputText(Character.toString(c), 1);
        }
        // Quick Insert
        else if (!regText && Settings.quickInsert && isOpener(mChar)) {
            ime.inputText(mChar.toString(), 1);
            ime.inputText(String.valueOf(getCloser(mChar)), -1);
        }
        // Quick Close
        else if (!regText && Settings.quickClose && isQuickOpener(mChar)) {
            switch (state.getRepeatCount()) {
                default:
                case 0:
                    ime.inputText(mChar.toString(), 1);
                    break;
                case 1:
                    ime.inputText(getQuickCloser(mChar) + "", 0);
                    ime.moveSelection(-1, -1);
                    break;
                case 2:
                    ic.deleteSurroundingText(0, 1);
                    ime.inputText(mChar.toString() + mChar.toString(), 1);
                    break;
            }
        } else {
            //TODO if regular keys but not auto correcting(MAKE PRETTIER)
            char c;
            if (model.getShiftState() == ShiftState.LOWERCASE) {
                c = Character.toLowerCase(mChar);
            } else {
                c = Character.toUpperCase(mChar);
            }
            ime.inputText(Character.toString(c), 1);
        }

        //side effects
        state.setReturnAfterSpace(shouldReturnAfterSpace(mChar));
        if (state.getRepeatCount() <= 0 &&
                (mChar == '\'' || mChar == '"' || mChar == '¿' || mChar == '¡'))
            control.fire(new SetKeyboardAction(Keyboards.DEFAULT));

        control.fire(new UpdateShiftAction());
        return null;
    }
}
