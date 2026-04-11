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
 * Overlay shown when the user enters cursor/selection mode. Draws the
 * neutral cursor icon and optionally one or both directional arrows
 * based on {@code model.getCursorMode()}, and routes touches to a
 * {@link SelectingHandler} which translates drags into cursor moves
 * or selection changes.
 */
public class CursorOverlay implements OverlayElement {

    private final TouchHandler mHandler;


    /** Installs a fresh selection handler; the overlay holds no other state. */
    public CursorOverlay() {
        mHandler = new SelectingHandler();
    }


    /**
     * Renders the cursor layer. Always draws the base cursor glyph;
     * additionally draws the left arrow when {@code cursorMode >= 0}
     * and the right arrow when {@code cursorMode <= 0}. Mode 0 means
     * "idle" and draws both arrows; positive/negative mean the cursor
     * is currently moving in that direction, in which case only the
     * matching arrow is shown.
     */
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


    /** Forwards the touch to the {@link SelectingHandler}. */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }
}
