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

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetKeyboardAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.elements.keyboards.Keyboards;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;

/**
 * Inserts a single space character and runs the side effects that go
 * with it: commits any in-progress correction/composing text, auto-
 * switches back to the alphabet keyboard when the return-after-space
 * flag is set (triggered by {@link KeyAction} on characters like
 * {@code . , ; & ! ?}), and fires an {@link UpdateShiftAction} so
 * auto-capitalize can kick in at a new sentence.
 */
public class SpaceAction implements Action<Void> {

    /**
     * Runs the commit → insert-space → side-effects sequence.
     * <p>
     * How: if auto-correct is on, the field allows it, and the caret
     * sits at the end of the composing region (i.e. the user just
     * finished typing a word rather than moving the cursor into the
     * middle of an existing one), {@link NovaKeyService#commitCorrection()}
     * locks in the corrected form. Otherwise the composing region is
     * finalized as-is via {@link NovaKeyService#commitComposingText()}
     * — this avoids {@code setComposingText}'s side effect of yanking
     * the cursor to the end of the composing region, which would
     * otherwise cause mid-word space presses to insert the space at
     * the end of the word instead of at the caret. A literal " " is
     * then inserted. If the input state had {@code returnAfterSpace}
     * flagged, fires a {@link SetKeyboardAction} back to the default
     * alphabet and clears the flag so it doesn't fire on the next
     * space too. Finally queues an {@link UpdateShiftAction}.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        InputState state = model.getInputState();

        //AutoCorrect — only when caret is at the end of the composing
        //region, otherwise the setComposingText call inside
        //commitCorrection would relocate the caret to the word's end.
        boolean caretAtComposingEnd =
                state.getSelectionStart() == state.getSelectionEnd()
                        && state.getCandidatesEnd() >= 0
                        && state.getSelectionEnd() == state.getCandidatesEnd();
        if (Settings.autoCorrect && state.shouldAutoCorrect() && caretAtComposingEnd) {
            ime.commitCorrection();
        } else {
            ime.commitComposingText();
        }
        ime.inputText(" ", 1);
        if (state.returnAfterSpace())
            control.fire(new SetKeyboardAction(Keyboards.DEFAULT));
        state.setReturnAfterSpace(false);
        control.fire(new UpdateShiftAction());
        return null;
    }
}
