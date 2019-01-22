package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import java.awt.Font;
import java.awt.event.*;

/**
 * @author dr6
 */
public class ComponentFactory {
    public static JSpinner makeSpinner() {
        return makeSpinner(0);
    }

    public static JSpinner makeSpinner(int value) {
        return makeSpinner(value, null);
    }

    public static JSpinner makeSpinner(int value, Integer min) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, null, 1);
        JSpinner spinner = new JSpinner(model);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(3);
        }
        return spinner;
    }

    public static JCheckBox makeCheckbox() {
        return new JCheckBox();
    }

    public static JTextField makeTextField() {
        return new JTextField(26);
    }

    public static JLabel makeLabel(String text) {
        return new JLabel(text);
    }

    public static JLabel makeHeadline(String text) {
        JLabel label = makeLabel(text);
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getSize()*1.3f));
        return label;
    }

    public static JButton makeButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    public static Action registerDelete(JComponent component, Runnable runnable) {
        return registerDelete(component, new RunnableAction(runnable));
    }

    public static Action registerDelete(JComponent component, Action action) {
        component.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_FOCUSED);
        component.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                JComponent.WHEN_FOCUSED);
        return action;
    }

    public static Action registerEnter(JComponent component, Runnable runnable) {
        return registerEnter(component, new RunnableAction(runnable));
    }

    public static Action registerEnter(JComponent component, Action action ) {
        component.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_FOCUSED);
        return action;
    }

    private static class RunnableAction extends AbstractAction {
        private final Runnable runnable;
        private RunnableAction(Runnable runnable) {
            this.runnable = runnable;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            runnable.run();
        }
    }
}
