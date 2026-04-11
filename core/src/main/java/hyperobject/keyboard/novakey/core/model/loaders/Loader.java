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

/**
 * Two-method save/load contract used by {@link hyperobject.keyboard.novakey.core.model.MainModel}
 * to materialize each piece of structured state (dimensions, theme,
 * keyboards, element list) from {@code SharedPreferences} and stash
 * user edits back out.
 * <p>
 * Production implementations are {@code ThemeLoader},
 * {@code MainDimensionsLoader}, {@code KeyboardsLoader}, and
 * {@code ElementsLoader} — MainModel constructs one of each in the
 * order listed, then calls {@link #load()} via {@code syncWithPrefs}.
 */
public interface Loader<T> {

    /**
     * Reconstructs a fresh {@code T} from whatever backing store this
     * loader reads from (usually {@code SharedPreferences}; element/
     * keyboard loaders read code-defined layouts).
     */
    T load();


    /**
     * Persists {@code t} so a later {@link #load()} will see it.
     * Implementations that have nothing to persist (code-defined
     * layouts) may no-op.
     */
    void save(T t);
}
