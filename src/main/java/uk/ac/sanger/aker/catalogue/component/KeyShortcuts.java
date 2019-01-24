package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;

/**
 * @author dr6
 */
public class KeyShortcuts {
    public static final int C_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static final KeyShortcuts NEW = key(VK_N, C_MASK);
    public static final KeyShortcuts OPEN = key(VK_O, C_MASK);
    public static final KeyShortcuts SAVE = key(VK_S, C_MASK);
    public static final KeyShortcuts SAVE_AS = key(VK_S, C_MASK|SHIFT_MASK);
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
    public void register(JComponent component, ActionListener actionListener) {
        register(component, actionListener, JComponent.WHEN_FOCUSED);
    }

    public void register(JFrame frame, ActionListener actionListener) {
        register(frame.getRootPane(), actionListener, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
