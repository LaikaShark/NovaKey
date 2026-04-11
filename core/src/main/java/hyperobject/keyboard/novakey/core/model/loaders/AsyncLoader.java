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

import android.os.AsyncTask;

/**
 * Adapter that runs any synchronous {@link Loader}'s load pass on a
 * background {@link AsyncTask} and fires the result back on the main
 * thread via a {@link LoadListener}. Lets callers keep the cheap
 * synchronous {@link Loader} contract while still off-loading the one
 * place where it might be expensive (theme parsing, keyboard resource
 * decode).
 * <p>
 * Save is left abstract because whether a write needs to be async at
 * all depends on the loader — subclasses decide.
 */
public abstract class AsyncLoader<T> {

    private final LoadListener<T> mListener;
    private final Loader<T> mLoader;


    /**
     * Wires up the synchronous loader to delegate to and the listener
     * that should be notified on completion. No work runs until
     * {@link #load()} is called.
     */
    public AsyncLoader(LoadListener<T> listener, Loader<T> loader) {
        mListener = listener;
        mLoader = loader;
    }


    /**
     * Kicks off an {@link AsyncTask} that runs {@code mLoader.load()} on
     * the background pool and forwards the resulting {@code T} to the
     * registered {@link LoadListener} on the main thread.
     */
    public void load() {
        new AsyncTask<Void, Void, T>() {
            @Override
            protected T doInBackground(Void... params) {
                return mLoader.load();
            }


            @Override
            protected void onPostExecute(T t) {
                super.onPostExecute(t);
                mListener.onLoad(t);
            }
        }.execute();
    }


    /**
     * Subclass hook: persist {@code t} to the backing store. May run
     * async in implementations that need it.
     */
    public abstract void save(T t);


    /**
     * Callback handed the loaded object on the main thread once
     * {@link #load()} finishes.
     */
    public interface LoadListener<T> {
        /** Called on the main thread once the load task completes. */
        void onLoad(T t);
    }
}
