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

import java.util.HashMap;
import java.util.Map;

/**
 * Base bag for geometric values an {@link hyperobject.keyboard.novakey.core.elements.Element}
 * needs to draw itself. Concrete subclasses (e.g. {@link MainDimensions})
 * define the key constants and the typed getters/setters; this class just
 * backs them with a lazy {@link HashMap} so keys only allocate storage
 * when something is actually written.
 * <p>
 * Written by dimension loaders during a prefs sync and by live resize
 * interactions; read by elements and themes every draw frame.
 */
public abstract class Dimensions {

    private Map<String, Object> mValues;


    /**
     * Stores {@code o} under {@code key}, lazily allocating the backing
     * map on the first write. Null keys or values are silently dropped so
     * callers don't need to null-check before stashing optional values.
     */
    public void set(String key, Object o) {
        if (key == null || o == null)
            return;
        if (mValues == null)
            mValues = new HashMap<>();
        mValues.put(key, o);
    }


    /**
     * Returns the raw object stored under {@code key}, or {@code null} if
     * nothing has been written yet or the key is missing. Callers usually
     * reach for the typed variants {@link #getI} / {@link #getF} instead.
     */
    public Object get(String key) {
        if (mValues == null || key == null)
            return null;
        return mValues.get(key);
    }


    /**
     * Typed accessor for int-valued entries. Returns 0 when nothing has
     * been written yet; casts unconditionally once the map exists, so
     * callers are responsible for matching the type they set.
     */
    public int getI(String key) {
        if (mValues == null || key == null)
            return 0;
        return (int) mValues.get(key);
    }


    /**
     * Typed accessor for float-valued entries. Returns 0 when nothing has
     * been written yet; casts unconditionally once the map exists, so
     * callers are responsible for matching the type they set.
     */
    public float getF(String key) {
        if (mValues == null || key == null)
            return 0;
        return (float) mValues.get(key);
    }
}
