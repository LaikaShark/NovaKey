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

package hyperobject.keyboard.novakey.core.controller;

import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import hyperobject.keyboard.novakey.core.Clipboard;
import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.SetEditingAction;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.elements.Element;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.InfiniteMenu;
import hyperobject.keyboard.novakey.core.model.MainModel;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.model.Settings;
import hyperobject.keyboard.novakey.core.utils.Colors;
import hyperobject.keyboard.novakey.core.utils.CustomTimer;
import hyperobject.keyboard.novakey.core.view.MainView;
import hyperobject.keyboard.novakey.core.view.NovaKeyView;
import hyperobject.keyboard.novakey.core.utils.drawing.Font;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;

/**
 * Central orchestrator of the keyboard runtime. Owns the {@link Model},
 * the {@link NovaKeyView}, and the current {@link TouchHandler}, and is
 * the single mutation choke point — elements and handlers never touch
 * model state directly, they build an {@link Action} and hand it to
 * {@link #fire(Action)}.
 * <p>
 * Lifetime: one Controller is constructed per {@link NovaKeyService}
 * instance in {@code onCreate}. Construction is the "big bang" that wires
 * up themes, fonts, icons, the clipboard menu, settings, the model, and
 * the view, then registers itself as the view's {@code OnTouchListener}.
 * <p>
 * Runtime loop:
 * <ol>
 *   <li>Android delivers a {@link MotionEvent} to the view.</li>
 *   <li>{@link #onTouch} routes it: if there's an "active" handler from a
 *       previous event, the event goes straight there; otherwise Controller
 *       walks the model's element list top-down and hands the event to each
 *       until one claims it ({@code handle} returns true), at which point
 *       that element's handler becomes the active handler.</li>
 *   <li>Handlers respond by firing actions via {@link #fire(Action)}.</li>
 *   <li>Every {@code fire} call ends with {@link #invalidate()} so the
 *       view redraws on the next frame.</li>
 * </ol>
 * The active handler is released when it returns {@code false} from
 * {@code handle} — typically on {@code ACTION_UP}.
 */
public class Controller implements Gun, View.OnTouchListener {

    //main stuff
    private final NovaKeyService mIME;
    private final Model mModel;
    private final NovaKeyView mView;

    //touch
    private TouchHandler mHandler;//current handler
    private CustomTimer mDoublePress;


    /**
     * Wires up the entire keyboard runtime. In order: initializes color
     * constants, loads app themes, creates fonts, loads icons, seeds
     * {@link InfiniteMenu}'s hidden-keys table, builds the clipboard menu,
     * hooks the {@link Settings} singleton to shared preferences and pulls
     * an initial snapshot, constructs the {@link MainModel} and
     * {@link MainView}, binds the view to the model/theme, and registers
     * {@code this} as the view's touch listener. Finally primes the
     * double-press timer that toggles editing mode when the user holds a
     * second finger for a second.
     *
     * @param ime the owning {@link NovaKeyService}; used both as Context
     *            for resource loading and as the target all fired actions
     *            eventually call back into
     */
    public Controller(NovaKeyService ime) {
        // context
        mIME = ime;


        //create colors
        Colors.initialize();
        //create fonts
        Font.create(ime);
        //load icons
        Icons.load(ime);
        //Create Hidden Keys
        InfiniteMenu.setHiddenKeys(ime.getResources().getStringArray(R.array.hidden_keys));
        //Create Clipboard Menu
        Clipboard.createMenu();
        //Initialize setting
        Settings.setPrefs(PreferenceManager.getDefaultSharedPreferences(ime));
        Settings.update();


        // model
        mModel = new MainModel(mIME);
        // view
        mView = new MainView(ime);
        mView.setModel(mModel);
        mView.setOnTouchListener(this);
        mView.setTheme(mModel.getTheme());

        // touch
        mDoublePress = new CustomTimer(1000, () -> fire(new SetEditingAction(true)));
    }


    /** Returns the keyboard's root view so the IME can attach it as its input view. */
    public NovaKeyView getView() {
        return mView;
    }


    /** Returns the live model backing this controller. */
    public Model getModel() {
        return mModel;
    }


    /** Requests a redraw of the view on the next frame. */
    public void invalidate() {
        mView.invalidate();
    }


    /**
     * The single mutation entry point. Calls {@code action.trigger(ime,
     * this, model)} if the action is non-null, then invalidates the view
     * so the effect is visible on the next frame. Returns whatever the
     * action returned (or {@code null} if the action itself was null).
     *
     * @param action the action to fire (nullable — no-op if null)
     * @param <T>    the action's return type
     */
    @Override
    public <T> T fire(Action<T> action) {
        T t = null;
        if (action != null)
            t = action.trigger(mIME, this, mModel);
        invalidate();
        return t;
    }


    /**
     * Touch event router. On every incoming event:
     * <ul>
     *   <li>Multi-touch bookkeeping first: a second pointer going down
     *       arms the 1-second double-press timer which, if it fires,
     *       enters editing mode; the second pointer lifting or the primary
     *       pointer going up cancels it.</li>
     *   <li>If there is already an active {@link TouchHandler} (from a
     *       previous event in the same gesture), the event goes straight
     *       to it; if {@code handle} returns false, the handler is
     *       released.</li>
     *   <li>Otherwise Controller walks the model's element list
     *       back-to-front (topmost element first) and offers the event to
     *       each. The first handler that returns true becomes the new
     *       active handler and receives all subsequent events until it
     *       releases.</li>
     * </ul>
     * Always returns true — the keyboard claims every touch that lands on
     * its view.
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //for multitouch
            case MotionEvent.ACTION_POINTER_DOWN:
                mDoublePress.begin();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mDoublePress.cancel();
                break;
            case MotionEvent.ACTION_UP:
                mDoublePress.cancel();
                break;
        }

        //if has a handler handle event
        if (mHandler != null) {
            boolean result = mHandler.handle(event, this);
            if (!result)
                mHandler = null;
        } else {
            //instantiate new handlers until one returns true
            List<Element> elems = mModel.getElements();
            for (int i = elems.size() - 1; i >= 0; i--) {
                TouchHandler handler = elems.get(i);
                boolean res = handler.handle(event, this);
                if (res) {
                    mHandler = handler;
                    break;
                }
            }
        }
        return true;//take in all events
    }
}
