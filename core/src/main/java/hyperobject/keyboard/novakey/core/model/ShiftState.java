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

package hyperobject.keyboard.novakey.core.model;

/**
 * The three stable positions in NovaKey's shift cycle, realizing the
 * README's Default → Shift → Caps Lock state machine. Transitions are
 * produced by {@code ShiftAction} / {@code UpdateShiftAction} and stored
 * on the {@link Model}; key rendering and character output both branch
 * on this enum.
 * <ul>
 *   <li>{@link #LOWERCASE} — the resting state; keys emit lowercase.</li>
 *   <li>{@link #UPPERCASE} — a one-shot shift; the next key emitted
 *       reverts the state back to {@link #LOWERCASE}.</li>
 *   <li>{@link #CAPS_LOCKED} — sticky shift; every key stays uppercase
 *       until the user explicitly leaves caps lock.</li>
 * </ul>
 */
public enum ShiftState {
    LOWERCASE,
    UPPERCASE,
    CAPS_LOCKED
}
