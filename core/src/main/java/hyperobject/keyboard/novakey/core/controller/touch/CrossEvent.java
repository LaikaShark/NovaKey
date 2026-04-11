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

package hyperobject.keyboard.novakey.core.controller.touch;

/**
 * Immutable value object passed to {@link AreaCrossedHandler#onCross}
 * whenever the finger crosses from one wheel area into another. Carries
 * both the old and the new area index so the callback can tell direction
 * of travel without consulting the handler state.
 * <p>
 * Area indices follow the {@link hyperobject.keyboard.novakey.core.elements.MainElement
 * MainElement} convention: 0 for the inner circle, 1–5 for the five
 * outer sectors, -1 for off-wheel.
 * <p>
 * TODO: add velocityX & velocityY to crossEvent
 */
public class CrossEvent {

    public final int newArea, prevArea;


    /**
     * Captures a sector-transition event.
     *
     * @param newArea  area the finger just entered
     * @param prevArea area the finger is leaving
     */
    public CrossEvent(int newArea, int prevArea) {
        this.newArea = newArea;
        this.prevArea = prevArea;
    }


    /**
     * Two CrossEvents are equal iff both their {@code newArea} and
     * {@code prevArea} fields match. Provided so handlers and tests can
     * use CrossEvents as map keys or in equality checks.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CrossEvent))
            return false;
        CrossEvent that = (CrossEvent) o;
        return this.newArea == that.newArea &&
                this.prevArea == that.prevArea;
    }


    /** Hash consistent with {@link #equals}: combines both area fields. */
    @Override
    public int hashCode() {
        return newArea * 31 + prevArea;
    }
}
