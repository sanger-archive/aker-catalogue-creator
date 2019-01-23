package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Module;

import javax.swing.*;
import java.awt.*;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.*;

/**
 * @author dr6
 */
public class ProcessPanel extends EditPanel {
    private CatalogueApp app;
    private AkerProcess process;

    private JLabel headlineLabel;
    private JTextField nameField;
    private UuidField uuidField;
    private JSpinner tatField;
    private JTextField classField;
    private JComboBox<Module> moduleCombo;
    private ProcessModulePanel graphPanel;

    public ProcessPanel(AkerProcess process, CatalogueApp app) {
        this.process = process;
        this.app = app;
        initComponents();
        load();
        layOut();
    }

    @Override
    protected void updateState() {
        process.setName(nameField.getText());
        headlineLabel.setText("Process: "+process.getName());
        process.setTat((int) tatField.getValue());
        process.setProcessClass(classField.getText());
    }

    private void initComponents() {
        headlineLabel = makeHeadline("Process");
        nameField = makeTextField();
        uuidField = new UuidField(process);
        tatField = makeSpinner(0, 0);
        classField = makeTextField();
        graphPanel = new ProcessModulePanel(app.getFrame(), process, this);
        moduleCombo = makeCombo(app.getCatalogue().getModules().toArray(new Module[0]));
        moduleCombo.setRenderer(new ListNameRenderer());
        moduleCombo.addActionListener(e -> graphPanel.repaint());
    }

    private void load() {
        nameField.setText(process.getName());
        headlineLabel.setText("Process: "+process.getName());
        tatField.setValue(process.getTat());
        classField.setText(process.getProcessClass());
        uuidField.setText(process.getUuid());
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        QuickConstraints constraints = new QuickConstraints(new Insets(10, 0, 10, 0));
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(headlineLabel, constraints);
        constraints.insets.left = 10;
        constraints.gridwidth = 1;
        add("Name:", constraints.incy().left());
        add(nameField, constraints.right());
        add("UUID:", constraints.incy().left());
        add(uuidField, constraints.right());
        add("TAT:", constraints.incy().left());
        add(tatField, constraints.right());
        add("Process class:", constraints.incy().left());
        add(classField, constraints.right());
        Box box = Box.createHorizontalBox();
        box.add(makeLabel("To add:"));
        box.add(Box.createHorizontalStrut(10));
        box.add(moduleCombo);
        constraints.rightAnchor = GridBagConstraints.LINE_END;
        JLabel modulesLabel = makeLabel("Modules:");
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD));
        add(modulesLabel, constraints.incy().left());
        add(box, constraints.right());
        constraints.left().gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(graphPanel, constraints.incy());
    }

    public void claimFocus() {
        nameField.requestFocusInWindow();
    }

    public Module getModuleToAdd() {
        return (Module) moduleCombo.getSelectedItem();
    }

    public void clearModuleToAdd() {
        moduleCombo.setSelectedItem(null);
    }
}
