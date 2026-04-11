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

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import hyperobject.keyboard.novakey.R;
import hyperobject.keyboard.novakey.core.IconView;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;

/**
 * Host activity for the interactive tutorial. Inflates
 * {@code R.layout.tutorial_layout} which contains:
 * <ul>
 *   <li>A {@link TaskView} for the scrolling task panel.</li>
 *   <li>An {@link EditText} the user types into.</li>
 *   <li>A {@link TextInputLayout} hint wrapper and two
 *       {@link IconView}s (help, clear).</li>
 * </ul>
 * The activity force-opens the soft keyboard on start, populates a
 * sequence of {@link Task}s via {@link #setInstructions()}, and wires
 * the index-change, text-change, and help/clear icon listeners that
 * tie them together.
 * <p>
 * Post-modernization {@link TextInputLayout} comes from the Material
 * Components artifact instead of the old support-design library.
 */
public class TutorialActivity extends Activity {

    private EditText mEditText;
    private TextInputLayout mTaskText;
    private IconView mClearIC, mHintIC;
    private TaskView mTaskView;
    private ArrayList<Task> mTasks;

    /**
     * Activity create hook: strips the title bar, installs the
     * tutorial layout, force-opens the soft keyboard, wires up every
     * child view, and finally calls {@link #setInstructions()} to
     * build the task list.
     * <p>
     * Highlights of the wiring:
     * <ul>
     *   <li>{@link TaskView.OnIndexChangeListener} updates the hint,
     *       toggles the help icon's visibility, triggers
     *       {@link Task#onEnd()} and {@link Task#onStart(EditText)},
     *       and re-disables Next on a forward move.</li>
     *   <li>{@link TaskView.OnFinishListener} just calls
     *       {@link #finish()}.</li>
     *   <li>The clear icon hides when the field is empty.</li>
     *   <li>A {@link TextWatcher} polls {@link TaskView#isComplete} on
     *       every change and enables Next when the goal is met.</li>
     *   <li>The help icon fires {@link Task#onTeach()} on the active
     *       task.</li>
     * </ul>
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//gets rid of title bar
        setContentView(R.layout.tutorial_layout);
        //forces keyboard to start
		final InputMethodManager im = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        mTaskView = (TaskView)findViewById(R.id.taskView);
        mTaskView.setOnIndexChangeListener(new TaskView.OnIndexChangeListener() {
            @Override
            public void onNewIndex(int index, int prev) {
                if (mTasks != null) {
                    mTaskText.setHint(mTasks.get(index).hintText());

                    mHintIC.setVisibility(mTasks.get(index).hasHint()
                            ? View.VISIBLE : View.INVISIBLE);

                    mTasks.get(prev).onEnd();
                    mEditText.setText("");
                    mTasks.get(index).onStart(mEditText);
                }
                if (index > prev)//moved forward
                    mTaskView.disableNext();
            }
        });
        mTaskView.setOnFinishListener(new TaskView.OnFinishListener() {
            @Override
            public void onFinish() {
                finish();
            }
        });

        mEditText = (EditText)findViewById(R.id.editText);
        mEditText.getBackground().setColorFilter(0xFFF0F0F0, PorterDuff.Mode.SRC_ATOP);

        mTaskText = (TextInputLayout)findViewById(R.id.editTextLayout);

        mHintIC = (IconView)findViewById(R.id.hintView);
        mHintIC.setIcon(Icons.get("help"));
        mHintIC.setSize(.6f);

        mClearIC = (IconView)findViewById(R.id.clearView);
        mClearIC.setIcon(Icons.get("clear"));
        mClearIC.setSize(.4f);
        mClearIC.setClickListener(new IconView.OnClickListener() {
            @Override
            public void onClick() {
                mEditText.getText().clear();
            }
        });


        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(""))
                    mClearIC.setVisibility(View.INVISIBLE);
                else
                    mClearIC.setVisibility(View.VISIBLE);

                if (mTaskView.isComplete(s.toString()))
                    mTaskView.enableNext();
            }
        });

        mHintIC.setClickListener(new IconView.OnClickListener() {
            @Override
            public void onClick() {
                if (mTasks != null)
                    mTasks.get(mTaskView.getIndex()).onTeach();
            }
        });
        setInstructions();
	}

    /**
     * Config-change hook that surfaces a toast whenever a hardware
     * keyboard is (un)plugged. Left over from the original
     * documentation link the author followed; has no functional
     * effect on the tutorial flow.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Builds the list of {@link Task}s driving the tutorial and
     * installs them on {@link #mTaskView}. Each task is an anonymous
     * subclass defined inline; the task sequence walks the user
     * through tap, swipe, space, delete, shift/caps, and enter
     * gestures before landing on a terminal "done" task.
     * <p>
     * All of the teach animation callbacks are commented out today —
     * they still reference the dropped {@code Controller.animate}
     * calls that haven't been rebuilt against the refactored
     * controller.
     * <p>
     * The tail of this method contains an unused {@code tasks}
     * String[] left over from the original hard-coded copy and a
     * commented-out task block for cursor rotation.
     */
    private void setInstructions() {
        mTasks = new ArrayList<>();
        mTasks.add(new Task("Press Next to begin!", "") {
            @Override
            void onTeach() {}

            @Override
            void onStart(EditText text) {
                text.setText("");
            }

            @Override
            boolean isComplete(String currText) {
                return true;
            }

            @Override
            boolean hasHint() {
                return false;
            }
        });
        final Location[] tappers = new Location[] {
                new Location(0, 0), new Location(1, 0), new Location(2, 0),
                new Location(3, 0), new Location(4, 0), new Location(5, 0) };
        mTasks.add(new Task("For the main letters just tap the area.", "Type \"call\"") {
            @Override
            void onTeach() {
                //Controller.animate(new TeachBadAnimator("call"));
            }

            @Override
            void onStart(EditText text) {
                text.setText("");
                //Controller.animate(new FocusAnimation(tappers).addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.equals("call");
            }
        });

        final Location[] swipers = new Location[] {
                new Location(0, 1), new Location(0, 2), new Location(0, 3), new Location(0, 4),
                new Location(0, 5),
                new Location(1, 1), new Location(1, 2), new Location(1, 3), new Location(1, 4),
                new Location(2, 1), new Location(2, 2), new Location(2, 3), new Location(2, 4),
                new Location(3, 1), new Location(3, 2), new Location(3, 3), new Location(3, 4),
                new Location(4, 1), new Location(4, 2), new Location(4, 3), new Location(4, 4),
                new Location(5, 1), new Location(5, 2), new Location(5, 3), new Location(5, 4),
        };
        mTasks.add(new Task("Nice Job!\nFor the remaining keys swipe over the line closest to it.",
                "Type \"novakey\"") {
            @Override
            void onTeach() {
                //Controller.animate(new TeachBadAnimator("novakey"));
            }

            @Override
            void onStart(EditText text) {
                text.setText("");
                //Controller.animate(new FocusAnimation(swipers).addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.equals("novakey");
            }


        });

        mTasks.add(new Task("You're doing great! Now to space, swipe from left to right over the small circle.",
                "Type \"hi there\"") {
            @Override
            void onTeach() {
                //Controller.animate(new TeachBadAnimator("hi there"));
            }

            @Override
            void onStart(EditText text) {
                //Controller.animate(new TeachBadAnimator(" ").addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.equals("hi there");
            }
        });
        mTasks.add(new Task("Oh no! A wild smiley has appeared...use delete to get rid of it!",
                "Delete the text") {
            @Override
            void onTeach() {
                //Controller.animate(new TeachBadAnimator("⌫⌫⌫"));
            }

            @Override
            void onStart(EditText text) {
                text.setText(">:)");
                text.setSelection(3);
                //Controller.animate(new TeachBadAnimator("⌫").addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.equals("");
            }
        });
        mTasks.add(new Task("You're awesome! Swipe up to shift, shift twice to lock the shift.",
                "Type \"CAPS ARE FUN\"") {
            @Override
            void onTeach() {
                //Controller.animate(new TeachBadAnimator("▲▲CAPS ARE FUN"));
            }

            @Override
            void onStart(EditText text) {
                //Controller.animate(new TeachBadAnimator("▲▲▲").addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.equals("CAPS ARE FUN");
            }
        });
        mTasks.add(new Task("You're a pro! Swipe down to enter over the circle.", "Type on multiple lines") {
            @Override
            void onTeach() {
                 //Controller.animate(new TeachBadAnimator("\n"));
            }

            @Override
            void onStart(EditText text) {
                //Controller.animate(new TeachBadAnimator("\n").addDelay(400));
            }

            @Override
            boolean isComplete(String currText) {
                return currText.contains("\n");
            }
        });
//        mTasks.add(new Task("You can move the cursor! Just rotate around the circle.",
//                "Fix the text to say: \"Apples\"") {
//            @Override
//            void onTeach() {
//                Controller.animate(new TeachBadAnimator("⑷⑸⑴⑵p").addDelay(400));
//            }
//            @Override
//            void onStart(EditText mEditText) {
//                mEditText.setText("Aples");
//                mEditText.setSelection(5);
//                Controller.animate(new TeachBadAnimator("⑷⑸⑴⑵").addDelay(400));
//            }
//            @Override
//            boolean isComplete(String currText) {
//                return currText.equals("Apples");
//            }
//        });
//        mTasks.add(new Task("You can move the cursor! Just rotate around the circle.",
//                "Fix the text to say: \"Apples\"") {
//            @Override
//            void onTeach() {
//                Controller.animate(new TeachBadAnimator("⑷⑸⑴⑵p").addDelay(400));
//            }
//            @Override
//            void onStart(EditText mEditText) {
//                mEditText.setText("Aples");
//                mEditText.setSelection(5);
//                Controller.animate(new TeachBadAnimator("⑷⑸⑴⑵").addDelay(400));
//            }
//            @Override
//             boolean isComplete(String currText) {
//                return currText.equals("Apples");
//            }
        mTasks.add(new Task("Great! You are done! Keep typing, practice makes perfect.",
                "") {
            @Override
            void onTeach() {
            }

            @Override
            void onStart(EditText text) {
            }

            @Override
            boolean isComplete(String currText) {
                return true;
            }

            @Override
            boolean hasHint() {
                return false;
            }
        });
//        });
        mTaskView.setTasks(mTasks);
        String[] tasks = new String[] {
                "You're a pro!", "Now just rotate around", "the circle to move the cursor.", "Fix the text1 to say: \"Apples\"" ,
                "Fantastic! While moving the\ncursor quickly go in and\nout of the circle to select\ndo it again to switch sides",
                "We cant forget other symbols\nclick on the #! to switch keyboard,\nshift while on there to access\nmore symbols. Type 123",
                "You can use your clipboard\nwhile moving the cursor\nhold the center down\nand release on what you want to do",
                "Almost done. Hold down\nany key for the special\ncharacters rotate to select",
                "Finally, You can hold\ndown with two fingers to\nmove and resize the keyboard",
                "Congratulations! You finished!\nKeep typing,\npractice makes perfect" };
    }

    /**
     * Plain (x, y) pair used to address keyboard slots in the
     * commented-out focus animations. Kept for symmetry with the
     * dropped animation wiring.
     */
    public static class Location {
        final int x, y;


        Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
