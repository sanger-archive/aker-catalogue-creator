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
        Insets insets = new Insets(10, 0, 10, 0);
        GridBagConstraints cleft = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0);
        GridBagConstraints cright = new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
        add(headlineLabel, new GridBagConstraints(0, 0, 2, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        addRow("Name:", nameField, 1, cleft, cright);
        addRow("UUID:", uuidField, 2, cleft, cright);
        addRow("TAT:", tatField, 3, cleft, cright);
        addRow("Process class:", classField, 4, cleft, cright);
        Box box = Box.createHorizontalBox();
        box.add(makeLabel("To add:"));
        box.add(Box.createHorizontalStrut(10));
        box.add(moduleCombo);
        cright.anchor = GridBagConstraints.LINE_END;
        JLabel modulesLabel = makeLabel("Modules:");
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD));
        addRow(modulesLabel, box, 5, cleft, cright);
        add(graphPanel, new GridBagConstraints(0, 6, 2, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    }

    private void addRow(Object o1, Object o2, int y, GridBagConstraints cleft, GridBagConstraints cright) {
        cleft.gridy = y;
        cright.gridy = y;
        if (o1 instanceof String) {
            o1 = makeLabel((String) o1);
        }
        if (o2 instanceof String) {
            o2 = makeLabel((String) o2);
        }
        add((Component) o1, cleft);
        add((Component) o2, cright);
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
