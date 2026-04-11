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

import android.graphics.Canvas;
import android.view.MotionEvent;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.controller.touch.SelectingHandler;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;
import hyperobject.keyboard.novakey.core.view.themes.board.BoardTheme;

/**
 * Created by Viviano on 9/2/2016.
 */
public class CursorOverlay implements OverlayElement {

    private final TouchHandler mHandler;


    public CursorOverlay() {
        mHandler = new SelectingHandler();
    }


    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        BoardTheme board = theme.getBoardTheme();

        int cursorCode = model.getCursorMode();
        board.drawItem(Icons.cursors, d.getX(), d.getY(),
                d.getSmallRadius(), canvas);
        if (cursorCode >= 0)
            board.drawItem(Icons.cursorLeft, d.getX(), d.getY(),
                    d.getSmallRadius(), canvas);
        if (cursorCode <= 0)
            board.drawItem(Icons.cursorRight, d.getX(), d.getY(),
                    d.getSmallRadius(), canvas);
    }


    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }
}
