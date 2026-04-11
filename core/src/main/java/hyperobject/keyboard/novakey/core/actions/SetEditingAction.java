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

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.view.EditView;

/**
 * Swaps the IME's displayed input view between the normal keyboard view
 * and the in-keyboard layout editor ({@link EditView}). Used by the
 * settings/tutorial flow to let the user tweak the wheel geometry from
 * inside the IME itself.
 * <p>
 * On enter: builds a fresh {@link EditView} bound to the current theme
 * and installs it via {@link NovaKeyService#setInputView}.
 * On exit: re-syncs the model with SharedPreferences (so any dimension
 * changes the edit view wrote are picked up) and restores the normal
 * controller-owned view.
 */
public class SetEditingAction implements Action<Void> {

    private final boolean mEditing;


    /**
     * @param editing {@code true} to enter the editor, {@code false} to
     *                leave it and return to the normal keyboard view
     */
    public SetEditingAction(boolean editing) {
        mEditing = editing;
    }


    /**
     * Installs the correct view on the IME and, on exit, pulls any
     * persisted dimension tweaks back into the model via
     * {@link Model#syncWithPrefs()}.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        if (mEditing) {
            EditView editView = new EditView(ime, control, model.getMainDimensions());
            editView.setTheme(model.getTheme());
            ime.setInputView(editView);
        } else {
            //updates the main model
            model.syncWithPrefs();
            ime.setInputView(control.getView());
        }
        return null;
    }
}
