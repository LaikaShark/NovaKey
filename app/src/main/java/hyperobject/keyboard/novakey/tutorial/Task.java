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

package hyperobject.keyboard.novakey.tutorial;

import android.widget.EditText;

/**
 * One step of the interactive tutorial flow. Each task owns:
 * <ul>
 *   <li>The primary instruction text shown in the task panel.</li>
 *   <li>A hint string displayed above the target {@link EditText}.</li>
 *   <li>A "teach" animation hook ({@link #onTeach}) triggered by the
 *       help button.</li>
 *   <li>Start/end hooks that can seed the edit field and register
 *       one-shot animations.</li>
 *   <li>A {@link #isComplete(String)} predicate the container polls on
 *       every text change to decide when to enable the Next button.</li>
 * </ul>
 * Concrete subclasses are created inline inside
 * {@link TutorialActivity#setInstructions()}.
 */
public abstract class Task {

    private String mainText, hintText;


    /**
     * Creates a task with the given instruction and hint strings.
     * Subclasses call this from their own anonymous constructor.
     */
    Task(String main, String hint) {
        this.mainText = main;
        this.hintText = hint;
    }


    /** Returns the primary instruction text for this task. */
    public String mainText() {
        return mainText;
    }


    /** Returns the hint displayed above the target EditText. */
    public String hintText() {
        return hintText;
    }


    /**
     * Controls whether the help/hint icon is visible for this task.
     * Default is {@code true}; introductory or terminal tasks override
     * this to hide the button.
     */
    boolean hasHint() {
        return true;
    }


    /**
     * Hook fired when the user taps the hint/help icon. Intended to
     * kick off a "teach" animation demonstrating the gesture the task
     * expects.
     */
    abstract void onTeach();


    /**
     * Hook fired when this task becomes active. Subclasses typically
     * reset the target edit field and optionally schedule a focus
     * animation over the relevant keyboard areas.
     */
    abstract void onStart(EditText text);


    /**
     * Predicate polled by {@link TaskView} on every text change to
     * decide whether the task should be marked complete (which in turn
     * enables the Next button).
     *
     * @param currText the current contents of the edit field
     * @return {@code true} if the goal for this task has been met
     */
    abstract boolean isComplete(String currText);


    /**
     * Hook fired when the user leaves this task (back or forward).
     * Default is a no-op; overridden only where a task needs to unwind
     * something it started in {@link #onStart}.
     */
    void onEnd() {
    }
}
