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

package hyperobject.keyboard.novakey.core.actions;

import android.content.ClipData;
import android.view.inputmethod.ExtractedText;
import android.widget.Toast;

import hyperobject.keyboard.novakey.core.Clipboard;
import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.actions.input.InputAction;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.InputState;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Multiplexed clipboard operation driven by a {@link Clipboard} opcode:
 * copy, cut, paste, select-all, or deselect-all. The target editor is
 * whatever field the IME is currently attached to — this action reads
 * its selection through {@link NovaKeyService#getExtractedText()} and
 * writes back through {@code inputText} / {@code setSelection}.
 * <p>
 * User-visible effects: system clipboard mutated on copy/cut (with a
 * short "Text Copied" toast); selection replaced with clipboard contents
 * on paste; entire field selected or collapsed on the select/deselect
 * variants.
 */
public class ClipboardAction implements Action<String> {

    private final int mAction;


    /**
     * Captures which clipboard sub-operation to run.
     *
     * @param action one of the {@link Clipboard} opcodes
     *               ({@code COPY}, {@code CUT}, {@code PASTE},
     *               {@code SELECT_ALL}, {@code DESELECT_ALL})
     */
    public ClipboardAction(int action) {
        mAction = action;
    }


    /**
     * Dispatches on the stored opcode.
     * <p>
     * How:
     * <ul>
     *   <li>COPY/CUT: grabs the current selection via
     *       {@link NovaKeyService#getSelectedText()}, pushes it onto the
     *       system clipboard via {@link #copy}, and on CUT also replaces
     *       the selection with an empty string. Fires a
     *       {@link ShowToastAction} on success.</li>
     *   <li>PASTE: reads the primary clip's last item and inserts it
     *       via an {@link InputAction}, so the paste flows through the
     *       same composing-text pathway as typing.</li>
     *   <li>SELECT_ALL: extends the selection from 0 to the length of
     *       the extracted text.</li>
     *   <li>DESELECT_ALL: collapses the selection to one end — the
     *       start if the cursor-mode is "selecting right", otherwise
     *       the end — so deselect keeps the caret where the user was
     *       most recently anchored.</li>
     * </ul>
     * Always returns {@code null}; the {@code Action<String>} parameter
     * is historical and unused by callers.
     */
    @Override
    public String trigger(NovaKeyService ime, Controller control, Model model) {
        ExtractedText eText = ime.getExtractedText();
        InputState state = model.getInputState();

        // copy/cut
        if (mAction == Clipboard.COPY || mAction == Clipboard.CUT) {
            String text = ime.getSelectedText();
            if (copy(text, ime)) {
                // cut
                if (mAction == Clipboard.CUT) {
                    ime.inputText("", 0);
                }
                control.fire(new ShowToastAction("Text Copied", Toast.LENGTH_SHORT));
            }
        }
        // paste
        else if (mAction == Clipboard.PASTE) {
            String text = ime.getClipboard().getPrimaryClip()
                    .getItemAt(ime.getClipboard().getPrimaryClip().getItemCount() - 1)
                    .getText().toString();
            if (text != null)
                control.fire(new InputAction(text));
        }
        // select all
        else if (mAction == Clipboard.SELECT_ALL) {
            int end = eText.text.length();
            ime.setSelection(0, end);
        }
        // deselect all
        else if (mAction == Clipboard.DESELECT_ALL) {
            int i = control.getModel().getCursorMode() <= 0
                    ? eText.selectionEnd : eText.selectionStart;
            ime.setSelection(i, i);
        }
        return null;
    }


    /**
     * Pushes a plain-text string onto the system clipboard.
     *
     * @return {@code true} if anything was copied, {@code false} if the
     *         string was empty (the clipboard is left alone in that case
     *         so we don't wipe whatever the user previously copied)
     */
    public boolean copy(String text, NovaKeyService ime) {
        if (text.length() > 0) {
            ClipData cd = ClipData.newPlainText("text", text);
            ime.getClipboard().setPrimaryClip(cd);
            return true;
        }
        return false;
    }
}
