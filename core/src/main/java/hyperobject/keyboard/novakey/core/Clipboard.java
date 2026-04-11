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

package hyperobject.keyboard.novakey.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hyperobject.keyboard.novakey.core.actions.Action;
import hyperobject.keyboard.novakey.core.actions.Actions;
import hyperobject.keyboard.novakey.core.actions.ClipboardAction;
import hyperobject.keyboard.novakey.core.actions.NoAction;
import hyperobject.keyboard.novakey.core.actions.SetOverlayAction;
import hyperobject.keyboard.novakey.core.actions.input.DeleteAction;
import hyperobject.keyboard.novakey.core.actions.input.InputAction;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.InfiniteMenu;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.Menu;
import hyperobject.keyboard.novakey.core.elements.keyboards.overlays.menus.OnUpMenu;
import hyperobject.keyboard.novakey.core.utils.drawing.Icons;

/**
 * App-level clipboard history plus the popup menu that exposes it.
 * <p>
 * This is NovaKey's own ring buffer, not the system clipboard. New
 * entries go on the front of {@link #clips} via {@link #add(String)},
 * the tail is dropped once the list passes {@link #MAX_CLIP_SIZE}, and
 * {@link #createClipboard()} snapshots the buffer into an
 * {@link InfiniteMenu} so the user can pick an old copy to re-paste.
 * <p>
 * {@link #createMenu()} builds the static top-level {@link #MENU} — a
 * {@link OnUpMenu} exposing copy / paste / select-all / cut / deselect,
 * each entry wired to a {@link ClipboardAction} carrying one of the
 * integer action-type constants below. Paste is special: its on-up
 * handler swaps the wheel overlay to the clipboard-history menu.
 */
public class Clipboard {

    private static final int MAX_CLIP_SIZE = 20;
    private static List<String> clips = new ArrayList<String>();

    /** Action-type constants passed to {@link ClipboardAction}. */
    public static int COPY = 1, SELECT_ALL = 2, PASTE = 3, DESELECT_ALL = 4, CUT = 5;


    /**
     * Pushes {@code text} onto the front of the clipboard history and
     * evicts the oldest entry if the buffer is over {@link #MAX_CLIP_SIZE}.
     */
    public static void add(String text) {
        clips.add(0, text);
        if (clips.size() > MAX_CLIP_SIZE)
            clips.remove(MAX_CLIP_SIZE);
    }


    /**
     * Returns the clip at {@code index} (0 = most recent), or
     * {@code null} if the index is out of range.
     */
    public static String get(int index) {
        if (clips.size() > index)
            return clips.get(index);
        return null;
    }


    /** Current number of stored clips (0–{@value #MAX_CLIP_SIZE}). */
    public static int clipCount() {
        return clips.size();
    }


    /** The cached top-level clipboard popup, built by {@link #createMenu()}. */
    public static OnUpMenu MENU;


    /**
     * Snapshots the current clipboard ring buffer into a fresh
     * {@link InfiniteMenu} whose entries each re-input their text via
     * an {@link InputAction}. A {@link Menu#CANCEL} entry is appended
     * so the user can back out. {@link IndexOutOfBoundsException}s
     * during population are caught and logged so a racy mutation can't
     * tear the menu down mid-build.
     *
     * @return an infinite menu with all clipboard entries plus a cancel entry
     */
    private static InfiniteMenu createClipboard() {
        List<Menu.Entry> entries = new ArrayList<>();
        for (int i = 0; i < Clipboard.clipCount(); i++) {
            try {
                String text = Clipboard.get(i);
                if (text != null)
                    entries.add(new Menu.Entry(text, new InputAction(text)));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        entries.add(Menu.CANCEL);

        return new InfiniteMenu(entries);
    }


    /**
     * Populates the static {@link #MENU} with a 5-slot {@link OnUpMenu}
     * of clipboard operations.
     * <p>
     * Each slot is an {@link OnUpMenu.Entry} whose <em>primary</em>
     * action fires immediately on selection and whose <em>on-up</em>
     * action fires when the finger lifts. Three slots are plain
     * actions ({@link NoAction} on-up), two open follow-up menus:
     * <ul>
     *   <li>select-all: on-up swaps the overlay to an infinite menu of
     *       compound variants (select+copy, select+cut, select+clear)</li>
     *   <li>paste: on-up builds a fresh clipboard-history menu via
     *       {@link #createClipboard()} and installs it as the overlay</li>
     * </ul>
     * Must be called once at startup before anything reads {@link #MENU}.
     */
    public static void createMenu() {
        Action copy = new ClipboardAction(COPY);
        Action cut = new ClipboardAction(CUT);
        Action paste = new ClipboardAction(PASTE);
        Action select_all = new ClipboardAction(SELECT_ALL);
        Action deselect_all = new ClipboardAction(DESELECT_ALL);

        //select all list
        List<Menu.Entry> select = Arrays.asList(
                new Menu.Entry(Icons.get("select_all"),
                        select_all),

                new Menu.Entry(Icons.get("select_all_copy"),
                        new Actions(select_all, copy)),

                new Menu.Entry(Icons.get("select_all_cut"),
                        new Actions(select_all, cut)),

                new Menu.Entry(Icons.get("select_all_clear"),
                        new Actions(select_all, new DeleteAction())),

                Menu.CANCEL
        );
        InfiniteMenu selectAll = new InfiniteMenu(select);


        //Main list
        List<Menu.Entry> main = Arrays.asList(
                new OnUpMenu.Entry(Icons.get("content_copy"),
                        copy, new NoAction()),

                new OnUpMenu.Entry(Icons.get("select_all"),
                        select_all, new SetOverlayAction(selectAll)),

                new OnUpMenu.Entry(Icons.get("content_paste"),
                        paste, (ime, control, model) -> {
                    InfiniteMenu clipboard = createClipboard();
                    control.fire(new SetOverlayAction(clipboard));
                    return null;
                }),

                new OnUpMenu.Entry(Icons.get("deselect_all"),
                        deselect_all, new NoAction()),

                new OnUpMenu.Entry(Icons.get("content_cut"),
                        cut, new NoAction())
        );
        MENU = new OnUpMenu(main);
    }
}
