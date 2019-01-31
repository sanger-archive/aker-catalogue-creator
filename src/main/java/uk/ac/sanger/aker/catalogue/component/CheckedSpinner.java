package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeCheckbox;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeSpinner;

/**
 * A component containing a checkbox and an integer spinner.
 * The spinner is disabled unless the checkbox is ticked.
 * The {@link #getValue value} of the {@code CheckedSpinner} is
 * the spinner's value if the checkbox is ticked, otherwise null.
 * @author dr6
 */
public class CheckedSpinner extends JPanel {
    private JSpinner spinner;
    private JCheckBox checkBox;

    public CheckedSpinner(Integer value) {
        spinner = makeSpinner(value==null ? 0 : value);
        checkBox = makeCheckbox();
        checkBox.setSelected(value!=null);
        spinner.setEnabled(value!=null);
        checkBox.addChangeListener(e -> updateState());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(checkBox);
        add(Box.createHorizontalStrut(10));
        add(spinner);
        add(Box.createHorizontalGlue());
    }

    /**
     * Gets the input value for this component.
     * If the checkbox is ticked, the value is the spinner's current value.
     * Otherwise, the value is null.
     * @return the current value, or null
     */
    public Integer getValue() {
        return (checkBox.isSelected() ? (Integer) spinner.getValue() : null);
    }

    /**
     * Sets the current value for this component.
     * If {@code value} is null, the checkbox will be unticked.
     * Otherwise, the checkbox will be ticked and the value set in the spinner.
     * @param value the value to set
     */
    public void setValue(Integer value) {
        checkBox.setSelected(value!=null);
        if (value!=null) {
            spinner.setValue(value);
        }
    }

    private void updateState() {
        spinner.setEnabled(this.isEnabled() && checkBox.isSelected());
    }

    public void addChangeListener(ChangeListener changeListener) {
        spinner.addChangeListener(changeListener);
        checkBox.addChangeListener(changeListener);
    }
}
