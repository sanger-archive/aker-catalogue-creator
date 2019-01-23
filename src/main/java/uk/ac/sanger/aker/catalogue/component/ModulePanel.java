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
        QuickConstraints constraints = new QuickConstraints(insets);
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(headlineLabel, constraints);
        insets.left = 10;
        constraints.gridwidth = 1;
        add("Name:", constraints.incy().left());
        add(nameField, constraints.right());
        add("Min value:", constraints.incy().left());
        add(minField, constraints.right());
        add("Max value:", constraints.incy().left());
        add(maxField, constraints.right());
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
