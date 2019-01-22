package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.Module;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.awt.*;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.*;

/**
 * @author dr6
 */
public class ModulePanel extends EditPanel {
    private Module module;

    private JLabel headlineLabel;
    private JTextField nameField;
    private CheckedSpinner minField;
    private CheckedSpinner maxField;

    private Runnable updateAction;

    public ModulePanel(Module module, Runnable updateAction) {
        this.module = module;
        this.updateAction = updateAction;
        initComponents();
        layOut();
        updateState();
    }

    private void initComponents() {
        ChangeListener cl = getChangeListener();
        headlineLabel = makeHeadline("Module");
        nameField = makeTextField();
        minField = new CheckedSpinner(null);
        maxField = new CheckedSpinner(null);
        load();
        minField.addChangeListener(cl);
        maxField.addChangeListener(cl);
        nameField.getDocument().addDocumentListener(getDocumentListener());
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(10,0,10,0);
        add(headlineLabel, new GridBagConstraints(0,0,2,1,0,0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        GridBagConstraints cleft = new GridBagConstraints(0,0,1,1,0,0,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 5, 0);
        GridBagConstraints cright = new GridBagConstraints(1,0,1,1,0,0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
        addRow("Name:", nameField, 1, cleft, cright);
        addRow("Min value:", minField, 2, cleft, cright);
        addRow("Max value:", maxField, 3, cleft, cright);
    }

    private void addRow(Object o1, Object o2, int y, GridBagConstraints cleft, GridBagConstraints cright) {
        if (o1 instanceof String) {
            o1 = makeLabel((String) o1);
        }
        cleft.gridy = y;
        cright.gridy = y;
        add((Component) o1, cleft);
        add((Component) o2, cright);
    }

    @Override
    protected void updateState() {
        save();
    }

    protected void load() {
        headlineLabel.setText("Module: "+module.getName());
        nameField.setText(module.getName());
        minField.setValue(module.getMinValue());
        maxField.setValue(module.getMaxValue());
    }

    protected void save() {
        module.setName(nameField.getText());
        module.setMinValue(minField.getValue());
        module.setMaxValue(maxField.getValue());
        headlineLabel.setText("Module: "+module.getName());
        updateAction.run();
    }

    public void claimFocus() {
        nameField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ModulePanel(new Module("Fizzywhig"), frame::dispose));
        frame.pack();
        frame.setVisible(true);
    }
}
