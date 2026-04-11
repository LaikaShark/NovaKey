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

package hyperobject.keyboard.novakey.core.utils;

/**
 * Pre-Java-8 functional interface for a boolean-valued test of one argument.
 * <p>
 * Mirrors {@code java.util.function.Predicate} but exists because the
 * standard library's version is only available from API 24 while NovaKey
 * currently supports {@code minSdk 21}. Implementations should be
 * side-effect free and idempotent — callers treat {@link #test(Object)}
 * as a pure query.
 *
 * @param <T> the type of value being tested
 */
public interface Predicate<T> {

    /**
     * Evaluates this predicate on {@code t}.
     *
     * @param t the input argument
     * @return {@code true} if the argument matches the predicate
     */
    boolean test(T t);
}
