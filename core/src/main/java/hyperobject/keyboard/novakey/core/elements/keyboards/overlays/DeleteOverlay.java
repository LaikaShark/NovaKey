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
import hyperobject.keyboard.novakey.core.controller.touch.DeleteHandler;
import hyperobject.keyboard.novakey.core.controller.touch.TouchHandler;
import hyperobject.keyboard.novakey.core.model.MainDimensions;
import hyperobject.keyboard.novakey.core.model.Model;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;
import hyperobject.keyboard.novakey.core.utils.drawing.drawables.Drawable;
import hyperobject.keyboard.novakey.core.view.themes.MasterTheme;

/**
 * Overlay shown while the user is holding a delete gesture. Draws a
 * single backspace glyph scaled to 80% of the inner radius and routes
 * touch events to a {@link DeleteHandler} which handles the repeat
 * cadence and per-character vs per-word deletion logic.
 */
public class DeleteOverlay implements OverlayElement {

    private final TouchHandler mHandler;
    private final Drawable mIcon;


    /** Grabs the shared backspace icon and installs a fresh delete handler. */
    public DeleteOverlay() {
        mHandler = new DeleteHandler();
        mIcon = Icons.get("backspace");
    }


    /**
     * Draws the backspace icon centered on the wheel at 80% of the
     * inner radius. Must only be called from inside a view's
     * {@code onDraw}.
     */
    @Override
    public void draw(Model model, MasterTheme theme, Canvas canvas) {
        MainDimensions d = model.getMainDimensions();
        theme.getBoardTheme().drawItem(mIcon, d.getX(), d.getY(),
                d.getSmallRadius() * .8f, canvas);
    }


    /** Forwards the touch to the {@link DeleteHandler}. */
    @Override
    public boolean handle(MotionEvent event, Controller control) {
        return mHandler.handle(event, control);
    }
}
