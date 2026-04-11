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

package hyperobject.keyboard.novakey.core.model.loaders;

import java.util.ArrayList;
import java.util.List;

import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.elements.buttons.Button;
import hyperobject.keyboard.novakey.core.elements.buttons.ButtonData;
import hyperobject.keyboard.novakey.core.elements.buttons.ButtonToggleModeChange;
import hyperobject.keyboard.novakey.core.elements.buttons.PunctuationButton;
import hyperobject.keyboard.novakey.core.utils.drawing.shapes.Circle;
import hyperobject.keyboard.novakey.core.view.posns.DeltaRadiusPosn;

/**
 * Loader that produces the flat list of auxiliary {@link Button}
 * elements NovaKey draws around its wheel — currently the mode-switch
 * button and the punctuation button. Pure code right now: nothing is
 * read from prefs, and {@link #save(List)} is a deliberate no-op.
 * <p>
 * Positions are picked on the lower-left and lower-right corners via
 * {@link DeltaRadiusPosn}, both with a circular hit shape sized to the
 * same pixel count as the button bitmap.
 */
public class ElementsLoader implements Loader<List<Element>> {


    /**
     * Builds and returns a fresh two-button list on every call. TODO is
     * pre-existing: the 150px hard-code should eventually be resolved
     * from display density plus a user preference.
     */
    @Override
    public List<Element> load() {
        //TODO: get button size from DPI and preferences
        int size = 150;
        List<Element> buttons = new ArrayList<>();
        Button b1 = new ButtonToggleModeChange(
                new ButtonData()
                        .setPosn(new DeltaRadiusPosn(size / 2, Math.PI * 5 / 4))
                        .setSize(size)
                        .setShape(new Circle()));
        buttons.add(b1);
        Button b2 = new PunctuationButton(
                new ButtonData()
                        .setPosn(new DeltaRadiusPosn(size / 2, Math.PI * 7 / 4))
                        .setSize(size)
                        .setShape(new Circle()));
        buttons.add(b2);
        return buttons;
    }


    /**
     * No-op: the button list is code-defined, so there's nothing to
     * persist. Kept to satisfy the {@link Loader} contract.
     */
    @Override
    public void save(List<Element> elements) {

    }
}
