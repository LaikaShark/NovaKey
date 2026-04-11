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
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.OverlayElement;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Swaps the active {@link OverlayElement} the {@code MainElement}
 * delegates to — i.e. the keys/menus layer drawn on top of the wheel.
 * Used to move between the typing overlay and the cursor/delete/popup
 * overlays without tearing down the whole element tree.
 * <p>
 * User-visible effect: whatever was on top of the wheel disappears and
 * the new overlay takes over both drawing and touch routing.
 */
public class SetOverlayAction implements Action<Void> {

    private final OverlayElement mElement;


    /**
     * @param element the new overlay to install as the wheel's top layer
     */
    public SetOverlayAction(OverlayElement element) {
        mElement = element;
    }


    /**
     * Writes the new overlay into the model and invalidates the view
     * so it redraws with the replacement overlay.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        //TODO: animation
        model.setOverlayElement(mElement);
        control.invalidate();
        return null;
    }
}
