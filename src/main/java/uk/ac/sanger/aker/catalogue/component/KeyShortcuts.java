package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;

/**
 * Keyboard helper for the catalogue application.
 * Stores appropriate keyboard shortcuts for a set of common operations (open, save etc.).
 * <p>Usage is e.g.
 * <br>{@code KeyShortcuts.SAVE.register(frame, e -> performSave());}
 * @author dr6
 */
public class KeyShortcuts {
    /**
     * The command key for a mac, the control key for windows or unix.
     * @see Toolkit#getMenuShortcutKeyMask
     */
    public static final int C_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /** The shortcuts for "new" (cmd N) */
    public static final KeyShortcuts NEW = key(VK_N, C_MASK);
    /** The shortcuts for "open" (cmd O) */
    public static final KeyShortcuts OPEN = key(VK_O, C_MASK);
    /** The shortcuts for "save" (cmd S) */
    public static final KeyShortcuts SAVE = key(VK_S, C_MASK);
    /** The shortcuts for "save as" (cmd shift S) */
    public static final KeyShortcuts SAVE_AS = key(VK_S, C_MASK|SHIFT_MASK);
    /** The key controls for delete (DELETE and BACKSPACE) */
    public static final KeyShortcuts DELETE = key(VK_DELETE, 0).andKey(VK_BACK_SPACE, 0);

    private List<KeyStroke> keyStrokes = new ArrayList<>();

    private KeyShortcuts andKey(int key, int modifiers) {
        keyStrokes.add(KeyStroke.getKeyStroke(key, modifiers));
        return this;
    }

    private static KeyShortcuts key(int key, int modifiers) {
        return new KeyShortcuts().andKey(key, modifiers);
    }

    private void register(JComponent component, ActionListener actionListener, int condition) {
        for (KeyStroke ks : keyStrokes) {
            component.registerKeyboardAction(actionListener, ks, condition);
        }
    }

    /**
     * Register the shortcuts for this action against the given action listener when the specified component
     * is focused.
     * @param component the component that should be in focus when the key is pressed
     * @param actionListener the action to take when the key is pressed
     */
    public void register(JComponent component, ActionListener actionListener) {
        register(component, actionListener, JComponent.WHEN_FOCUSED);
    }

    /**
     * Register the shortcuts for this action against the given action listener when the specified frame
     * is focused.
     * @param frame the frame of the window that should be in focus when the key is pressed
     * @param actionListener the action to take when the key is pressed
     */
    public void register(JFrame frame, ActionListener actionListener) {
        register(frame.getRootPane(), actionListener, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
