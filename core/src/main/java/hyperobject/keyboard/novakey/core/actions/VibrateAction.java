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

package hyperobject.keyboard.novakey.core.actions;

import hyperobject.keyboard.novakey.core.controller.Controller;
import hyperobject.keyboard.novakey.core.NovaKeyService;
import hyperobject.keyboard.novakey.core.model.Model;

/**
 * Fires a short haptic buzz for the given duration by delegating to
 * {@link NovaKeyService#vibrate(long)}. The service decides whether to
 * actually vibrate based on the user's haptic-feedback preference — so
 * firing this action when haptics are disabled is a no-op rather than
 * an error.
 * <p>
 * Touches no model state; the side effect is entirely on the vibrator.
 */
public class VibrateAction implements Action<Void> {

    private final long mTime;


    /**
     * @param vibrateLevel vibration duration in milliseconds, as stored
     *                     in the vibrate-level preference
     */
    public VibrateAction(int vibrateLevel) {
        mTime = vibrateLevel;
    }


    /**
     * Asks the IME to vibrate for the stored duration.
     */
    @Override
    public Void trigger(NovaKeyService ime, Controller control, Model model) {
        ime.vibrate(mTime);
        return null;
    }
}
