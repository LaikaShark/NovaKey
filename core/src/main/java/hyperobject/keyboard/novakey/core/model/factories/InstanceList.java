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

package hyperobject.keyboard.novakey.core.model.factories;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tiny registry that maps integer ids to concrete {@code Class} tokens
 * and instantiates them on demand via the default constructor.
 * Subclasses fill the map in {@link #build(Map)}; call sites use the
 * integer id as a stable over-the-wire identifier (so prefs can store
 * "theme #3" without caring about class names).
 * <p>
 * Used by {@link ThemeFactory.Boards} to look up {@code BoardTheme}
 * implementations by number; designed to be reusable for other
 * "numbered catalog of classes" cases that may be added later.
 * <p>
 * Doubles as its own {@link Iterator}/{@link Iterable} — note that it
 * is <em>not</em> re-entrant: {@link #iterator()} resets the one shared
 * iterator cursor, so nesting loops will break.
 */
public abstract class InstanceList<T> implements Iterator<T>, Iterable<T> {

    private final Map<Integer, Class> mMap;
    private Iterator<Map.Entry<Integer, Class>> mIter;


    /**
     * Builds the backing map and immediately asks the subclass to fill
     * it. A {@link LinkedHashMap} is used so iteration order matches
     * insertion order, which lets the subclass pick the display order.
     */
    public InstanceList() {
        mMap = new LinkedHashMap<>();
        build(mMap);
    }


    /**
     * Subclass hook: populate {@code map} with {@code id → Class}
     * entries. Called once from the constructor.
     */
    protected abstract void build(Map<Integer, Class> map);


    /** Returns the number of registered entries. */
    public int size() {
        return mMap.size();
    }


    /**
     * Reflectively invokes {@code clazz}'s no-arg constructor and
     * returns the result. Every reflection exception is caught and
     * logged to stderr, in which case the method returns {@code null}.
     */
    private T construct(Class clazz) {
        try {
            return (T) clazz.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Looks up the class registered under {@code key} and returns a
     * fresh instance, or {@code null} if the id is unknown or
     * construction fails.
     */
    public T getValue(int key) {
        try {
            return construct(mMap.get(key));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Reverse lookup: walks the map in insertion order searching for an
     * entry whose class token matches {@code clazz} by identity, and
     * returns its id. Returns {@code null} if no entry matches.
     */
    public Integer getKey(Class clazz) {
        for (Map.Entry<Integer, Class> e : mMap.entrySet()) {
            if (e.getValue() == clazz) {
                return e.getKey();
            }
        }
        return null;
    }


    /**
     * Resets and returns the shared iterator cursor. Since this class
     * is its own Iterator, the returned object is always {@code this};
     * calling {@code iterator()} again invalidates any in-progress walk.
     */
    @Override
    public Iterator<T> iterator() {
        mIter = mMap.entrySet().iterator();
        return this;
    }


    /** Delegates to the shared cursor advanced by {@link #iterator()}. */
    @Override
    public boolean hasNext() {
        return mIter.hasNext();
    }


    /**
     * Advances the shared cursor and returns a freshly constructed
     * instance of the next registered class. Throws
     * {@link NoSuchElementException} if the cursor is exhausted.
     */
    @Override
    public T next() {
        return construct(mIter.next().getValue());
    }


    /**
     * Always throws — this registry is read-only once built, so
     * {@link Iterator#remove()} is not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("cannot remove items");
    }
}
