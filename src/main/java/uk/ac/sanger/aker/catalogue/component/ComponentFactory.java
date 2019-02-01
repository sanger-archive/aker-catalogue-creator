package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.Help;

import javax.swing.*;
import java.awt.Font;
import java.awt.event.ActionListener;

/**
 * Collection of static methods used to create swing components for the UI.
 * @author dr6
 */
public class ComponentFactory {
    /**
     * Makes an unbounded integer {@code JSpinner} with the initial value zero.
     */
    public static JSpinner makeSpinner() {
        return makeSpinner(0);
    }

    /**
     * Makes an unbounded integer {@code JSpinner} with the given initial value.
     */
    public static JSpinner makeSpinner(int value) {
        return makeSpinner(value, null);
    }

    /**
     * Makes an integer {@code JSpinner} with the given initial value and lower bound.
     * If the given lower bound is null, it is ignored.
     * @param value the initial value
     * @param min the lower bound, or null for no lower bound.
     */
    public static JSpinner makeSpinner(int value, Integer min) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, null, 1);
        JSpinner spinner = new JSpinner(model);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(3);
        }
        return spinner;
    }

    /** Makes a new checkbox. */
    public static JCheckBox makeCheckbox() {
        return new JCheckBox();
    }

    /** Makes a new textfield with a decent number of columns. */
    public static JTextField makeTextField() {
        return new JTextField(26);
    }

    /** Makes a new label showing the given string. */
    public static JLabel makeLabel(String text) {
        return new JLabel(text);
    }

    /** Makes a label with an expanded font size. */
    public static JLabel makeHeadline(String text) {
        JLabel label = makeLabel(text);
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getSize()*1.3f));
        return label;
    }

    /** Makes a button with the given text and action listener. */
    public static JButton makeButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    /** Makes a combobox with a default model containing the given items, and no value initially selected. */
    public static <E> JComboBox<E> makeCombo(E[] items) {
        DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>(items);
        JComboBox<E> combo = new JComboBox<>(model);
        combo.setSelectedItem(null);
        return combo;
    }

    public static JButton makeHelpButton() {
        JButton button = new JButton(Help.HELP_ICON);
        button.setToolTipText("Help");
        return button;
    }

}
