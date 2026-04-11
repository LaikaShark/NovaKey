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

import android.view.KeyEvent;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputConnection;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.Predicate;

/**
 * Deletes text from the current editor, in either direction and at
 * either "slow" (one character at a time) or "fast" (chew up to the
 * next space) granularity. Returns the deleted string so callers can
 * feed it into undo or clipboard flows.
 * <p>
 * Fast mode is what powers the delete-swipe gesture's run-on behavior:
 * the handler fires a {@code DeleteAction(false, true)} on each
 * additional segment and the character-until-space predicate eats
 * entire words at a time. Slow mode (the default) is what the tap-style
 * delete button and the selection-backspace path use.
 */
public class DeleteAction implements Action<String> {

    private final boolean mForward, mFast;


    /**
     * Slow backspace (delete one character to the left of the caret).
     */
    public DeleteAction() {
        this(false);
    }


    /**
     * Slow delete in a chosen direction.
     *
     * @param forwards {@code true} for forward-delete (delete-to-right),
     *                 {@code false} for backspace
     */
    public DeleteAction(boolean forwards) {
        this(forwards, false);
    }


    /**
     * @param forwards {@code true} for forward-delete, {@code false} for backspace
     * @param fast     {@code true} to delete until the next space
     *                 character, {@code false} to delete exactly one
     *                 character
     */
    public DeleteAction(boolean forwards, boolean fast) {
        mForward = forwards;
        mFast = fast;
    }


    /**
     * Dispatches between the two delete pathways and returns whatever
     * text was removed.
     * <p>
     * How:
     * <ul>
     *   <li>If the current selection is non-empty, treat this as a
     *       "delete the selection" and just send the appropriate
     *       {@code KEYCODE_DEL} / {@code KEYCODE_FORWARD_DEL} through
     *       the IME so the field handles it natively. Returns the
     *       original selected text.</li>
     *   <li>Otherwise, delegate to {@link #handleDelete} with a
     *       predicate that decides when to stop eating characters:
     *       slow mode stops immediately (always-true predicate),
     *       fast mode stops when it hits a space.</li>
     * </ul>
     */
    @Override
    public String trigger(NovaKeyService ime, Controller control, Model model) {
        InputState is = model.getInputState();
        // If not a single cursor then perform a delete key action
        if (is.getSelectionStart() != is.getSelectionEnd()) {
            String selected = ime.getSelectedText();
            ime.commitComposingText();

            if (mForward)
                ime.sendDownUpKeyEvents(KeyEvent.KEYCODE_FORWARD_DEL);
            else
                ime.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            return selected;
        }
        // Otherwise handle a delete fast or slow
        else {
            Predicate<Character> slow = character -> true,
                    fast = character -> character.charValue() == ' ';
            return handleDelete(ime, control, model, !mForward, mFast ? fast : slow, true);
        }
    }


    /**
     * Eats characters from around the cursor until a predicate says stop.
     * <p>
     * How: pulls the extracted text out of the IME, splits it into the
     * portion left and right of the selection, and walks character by
     * character away from the caret (backwards into {@code left}, forwards
     * into {@code right}). Each character is tested against {@code until};
     * if the predicate returns {@code false} the character is appended
     * to the builder and the walk continues. If it returns {@code true}
     * the walk stops, and the stopping character is included only when
     * {@code included} is true.
     * <p>
     * After the walk, the composing text is committed (so Android's
     * spell-checker sees the field as settled), the {@code InputState}'s
     * composing buffer is cleared to match, and a single
     * {@code deleteSurroundingText} call removes the run of characters
     * from the editor. Finally an {@link UpdateShiftAction} is fired so
     * auto-capitalize can kick back in if the caret is now at a sentence
     * boundary.
     *
     * @param backspace {@code true} to eat left of the caret, {@code false}
     *                  to eat right
     * @param until     stopping predicate: returns {@code true} when the
     *                  current character should end the walk
     * @param included  whether the stopping character itself is deleted
     * @return the concatenation of every character that was deleted,
     *         in left-to-right order
     */
    public String handleDelete(NovaKeyService ime, Controller control, Model model,
                               boolean backspace, Predicate<Character> until, boolean included) {
        // add deleted character to temporary memory so it can be added

        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null)
            return "";

        StringBuilder sb = new StringBuilder();
        ExtractedText et = ime.getExtractedText();
        String text = (String) et.text;
        String left = text.substring(0, et.selectionStart);
        String right = text.substring(et.selectionEnd);

        char curr = 0;
        if (backspace) {
            if (left.length() > 0)
                curr = left.charAt(left.length() - 1);
        } else {
            if (right.length() > 0)
                curr = right.charAt(0);
        }

        int soFar = 1;

        while (!until.test(curr) && curr != 0) {
            if (backspace)
                sb.insert(0, curr);
            else
                sb.append(curr);

            curr = 0;
            if (backspace) {
                if (left.length() - soFar > 0)
                    curr = left.charAt(left.length() - 1 - soFar);
            } else {
                if (right.length() - soFar > 0)
                    curr = right.charAt(soFar);
            }
            soFar++;
        }
        if (included && curr != 0) {
            if (backspace)
                sb.insert(0, curr);
            else
                sb.append(curr);
        }

        ic.finishComposingText();
        model.getInputState().clearComposingText();
        if (sb.length() >= 1) {
            if (backspace)
                ic.deleteSurroundingText(sb.length(), 0);
            else
                ic.deleteSurroundingText(0, sb.length());
        }
        control.fire(new UpdateShiftAction());
        return sb.toString();
    }
}
