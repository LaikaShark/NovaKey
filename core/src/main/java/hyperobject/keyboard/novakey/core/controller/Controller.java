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
import hyperobject.keyboard.novakey.core.view.themes.AppTheme;

/**
 * Created by Viviano on 7/10/2015.
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
     * Controller initializes models and creates private references to
     * the given IME and View
     *
     * @param ime the input method service
     */
    public Controller(NovaKeyService ime) {
        // context
        mIME = ime;


        //create colors
        Colors.initialize();
        //load app themes
        AppTheme.load(ime, ime.getResources());
        //create fonts
        Font.create(ime);
        //load icons
        Icons.load(ime);
        //load emojis
//		Emoji.load(this);
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


    /**
     * @return returns the main view
     */
    public NovaKeyView getView() {
        return mView;
    }


    /**
     * @return returns the draw model
     */
    public Model getModel() {
        return mModel;
    }


    /**
     * Redraws the view
     */
    public void invalidate() {
        mView.invalidate();
    }


    /**
     * Triggers action and update the view
     *
     * @param action action to fire
     * @return returns the result of the action
     */
    @Override
    public <T> T fire(Action<T> action) {
        T t = null;
        if (action != null)
            t = action.trigger(mIME, this, mModel);
        invalidate();
        return t;
    }


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