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
        save();
    }


    private void initComponents() {
        headlineLabel = makeHeadline("Process");
        nameField = makeTextField();
        nameField.getDocument().addDocumentListener(getDocumentListener());
        uuidField = new UuidField(process);
        tatField = makeSpinner(0, 0);
        tatField.addChangeListener(getChangeListener());
        classField = makeTextField();
        classField.getDocument().addDocumentListener(getDocumentListener());
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

    private void save() {
        process.setName(nameField.getText());
        headlineLabel.setText("Process: "+process.getName());
        process.setTat((int) tatField.getValue());
        process.setProcessClass(classField.getText());
        app.processesUpdated();
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        QuickConstraints constraints = new QuickConstraints(new Insets(0, 0, 10, 0));
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(headlineLabel, constraints);
        constraints.insets.left = 10;
        constraints.gridwidth = 1;
        add("Name:", nameField, constraints.incy());
        add("UUID:", uuidField, constraints.incy());
        add("TAT:", tatField, constraints.incy());
        add("Process class:", classField, constraints.incy());

        Box box = Box.createHorizontalBox();
        box.add(makeLabel("To add:"));
        box.add(Box.createHorizontalStrut(10));
        box.add(moduleCombo);
        constraints.rightAnchor = GridBagConstraints.LINE_END;
        constraints.insets.top = 10;
        JLabel modulesLabel = makeLabel("Modules:");
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD));

        add(modulesLabel, box, constraints.incy());
        constraints.left().gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(graphPanel, constraints.incy());
    }

    public Module getModuleToAdd() {
        return (Module) moduleCombo.getSelectedItem();
    }

    public void clearModuleToAdd() {
        moduleCombo.setSelectedItem(null);
    }
}
