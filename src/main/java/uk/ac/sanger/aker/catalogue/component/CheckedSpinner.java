package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeCheckbox;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeSpinner;

/**
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

    public Integer getValue() {
        return (checkBox.isSelected() ? (Integer) spinner.getValue() : null);
    }

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
