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

import android.view.inputmethod.ExtractedText;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetKeyboardAction;
import hyperobject.keyboard.novakey.core.actions.SetShiftStateAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.ShiftState;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * The "user swiped up / pressed shift" action. Does one of two things
 * depending on which keyboard is currently active:
 * <ul>
 *   <li>On PUNCTUATION or SYMBOLS: flips to the opposite of the pair
 *       ({@code PUNCTUATION ↔ SYMBOLS}). Shift is the paging gesture
 *       between the two symbol pages.</li>
 *   <li>On any alphabet keyboard: cycles the {@link ShiftState} through
 *       {@code LOWERCASE → UPPERCASE → CAPS_LOCKED → LOWERCASE} and,
 *       if there is a text selection, re-cases the selection to match
 *       the new state (title-case for UPPERCASE, ALL CAPS for
 *       CAPS_LOCKED, lowercase for LOWERCASE) while preserving the
 *       selection bounds.</li>
 * </ul>
 */
public class ShiftAction implements Action<Void> {

    /**
     * Runs the two-mode dispatch described in the class doc.
     * <p>
     * How: first snapshots the selection (start/end and the selected
     * text), then switches on the current keyboard code. On symbol
     * layouts it fires a {@link SetKeyboardAction}. On alphabet layouts
     * it switches on the current shift state, fires a
     * {@link SetShiftStateAction} for the next state, and — if there
     * was a selection — fires an {@link InputAction} containing the
     * re-cased text and restores the selection bounds via
     * {@link NovaKeyService#setSelection}.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        InputState state = model.getInputState();

        String selectedText = ime.getSelectedText();
        boolean shiftText = selectedText.length() > 0;
        int s = 0, e = 0;
        if (shiftText) {
            ExtractedText et = ime.getExtractedText();
            s = et.selectionStart;
            e = et.selectionEnd;
        }

        switch (model.getKeyboardCode()) {
            case Keyboards.PUNCTUATION:
                control.fire(new SetKeyboardAction(Keyboards.SYMBOLS));
                break;
            case Keyboards.SYMBOLS:
                control.fire(new SetKeyboardAction(Keyboards.PUNCTUATION));
                break;
            default:
                switch (model.getShiftState()) {
                    case LOWERCASE:
                        control.fire(new SetShiftStateAction(ShiftState.UPPERCASE));
                        if (shiftText) {//uppercase each word
                            control.fire(new InputAction(Util.uppercaseFirst(selectedText)));
                            ime.setSelection(s, e);
                        }
                        break;
                    case UPPERCASE:
                        control.fire(new SetShiftStateAction(ShiftState.CAPS_LOCKED));
                        if (shiftText) {//caps everything
                            control.fire(new InputAction(selectedText.toUpperCase()));
                            ime.setSelection(s, e);
                        }
                        break;
                    case CAPS_LOCKED:
                        control.fire(new SetShiftStateAction(ShiftState.LOWERCASE));
                        if (shiftText) {//lowercase everything
                            control.fire(new InputAction(selectedText.toLowerCase()));
                            ime.setSelection(s, e);
                        }
                        break;
                }
        }
        return null;
    }
}
