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

package hyperobject.keyboard.novakey.core.elements.keyboards.overlays;

import hyperobject.keyboard.novakey.core.elements.Element;

/**
 * Marker interface for elements that can be installed as the overlay
 * layer on top of the main wheel — the typing {@link hyperobject.keyboard.novakey.core.elements.keyboards.Keyboard},
 * {@link CursorOverlay}, {@link DeleteOverlay}, and popup menus all
 * implement this.
 * <p>
 * There's no extra API beyond {@link Element}; the interface exists
 * purely so {@code MainElement#setOverlay} can take a narrower type
 * than raw {@code Element} and make the role explicit at the call site.
 */
public interface OverlayElement extends Element {

}
