package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.Help;
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
    private JButton resizeButton;
    private JButton layoutButton;
    private JButton helpButton;

    private boolean loading;

    public ProcessPanel(AkerProcess process, CatalogueApp app) {
        this.process = process;
        this.app = app;
        initComponents();
        layOut();
    }

    @Override
    protected void updateState() {
        save();
    }

    private void initComponents() {
        headlineLabel = makeHeadline("Process");
        nameField = makeTextField();
        uuidField = new UuidField(process);
        tatField = makeSpinner(0, 0);
        classField = makeTextField();
        graphPanel = new ProcessModulePanel(app, process, this);
        helpButton = makeHelpButton();
        helpButton.setEnabled(!Help.MODULE_GRAPH_HELP.equals(Help.MISSING_TEXT));
        resizeButton = makeButton("Resize graph window", e -> resizeGraphWindow());
        layoutButton = makeButton("Auto layout", e -> autoLayoutGraph());
        load();
        nameField.getDocument().addDocumentListener(getDocumentListener());
        tatField.addChangeListener(getChangeListener());
        classField.getDocument().addDocumentListener(getDocumentListener());
        moduleCombo = makeCombo(app.getCatalogue().getModules().toArray(new Module[0]));
        moduleCombo.setRenderer(new ListNameRenderer());
        moduleCombo.addActionListener(e -> graphPanel.repaint());
        helpButton.addActionListener(e -> showGraphHelp());
    }

    @Override
    protected void load() {
        if (loading) {
            return;
        }
        loading = true;
        nameField.setText(process.getName());
        headlineLabel.setText("Process: "+process.getName());
        tatField.setValue(process.getTat());
        classField.setText(process.getProcessClass());
        uuidField.setText(process.getUuid());
        loading = false;
    }

    private void save() {
        if (loading) {
            return;
        }
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
        box.add(helpButton);
        box.add(Box.createHorizontalGlue());
        box.add(makeLabel("To add:"));
        box.add(Box.createHorizontalStrut(10));
        box.add(moduleCombo);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;
        JLabel modulesLabel = makeLabel("Modules:");
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD));

        add(modulesLabel, box, constraints.incy());
        constraints.rightAnchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets.top = 0;
        constraints.insets.bottom = 0;
        box = Box.createHorizontalBox();
        box.add(layoutButton);
        box.add(Box.createHorizontalStrut(10));
        box.add(resizeButton);
        add(box, constraints.incy().right());

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

    private void showGraphHelp() {
        app.getFrame().showHelp(Help.MODULE_GRAPH_HELP);
    }

    @Override
    public void fireOpen() {
        nameField.requestFocusInWindow();
    }

    private void resizeGraphWindow() {
        graphPanel.updateBounds();
        revalidate();
    }

    private void autoLayoutGraph() {
        graphPanel.autoLayout();
        revalidate();
    }

}
