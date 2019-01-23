package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.component.list.*;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.*;

/**
 * @author dr6
 */
public class CataloguePanel extends EditPanel {
    private CatalogueApp app;
    private JTextField pipelineField;
    private JTextField urlField;
    private JTextField limsIdField;
    private ListComponent<Module> moduleList;
    private ListComponent<AkerProcess> processList;
    private ListComponent<Product> productList;

    public CataloguePanel(CatalogueApp app) {
        this.app = app;
        initComponents();
        load();
        layOut();
    }

    private void initComponents() {
        pipelineField = makeTextField();
        urlField = makeTextField();
        limsIdField = makeTextField();
        moduleList = new ListComponent<>("Modules:", new ModuleListActor(app));
        processList = new ListComponent<>("Processes:", new ProcessListActor(app));
        productList = new ListComponent<>("Products:", new ProductListActor(app));
    }

    public void load() {
        Catalogue catalogue = getCatalogue();
        pipelineField.setText(catalogue.getPipeline());
        urlField.setText(catalogue.getUrl());
        limsIdField.setText(catalogue.getLimsId());
        moduleList.setItems(catalogue.getModules());
        processList.setItems(catalogue.getProcesses());
        productList.setItems(catalogue.getProducts());
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(10,10,10,10);
        add(makeHeadline("Catalogue"), new GridBagConstraints(0,0,2,1, 1.0, 0.1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

        add(makeLabel("Pipeline:"), new GridBagConstraints(0,1,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        add(pipelineField, new GridBagConstraints(1,1,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));

        add(makeLabel("URL:"), new GridBagConstraints(0,2,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        add(urlField, new GridBagConstraints(1,2,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));

        add(makeLabel("LIMS ID:"), new GridBagConstraints(0,3,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        add(limsIdField, new GridBagConstraints(1,3,1,1, 1.0, 0.1,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));

        add(moduleList, new GridBagConstraints(0,4,2,1, 1.0, 1.0,
                GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));

        add(processList, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0,
                GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));

        add(productList, new GridBagConstraints(0, 6, 2, 1, 1.0, 1.0,
                GridBagConstraints.PAGE_START, GridBagConstraints.NONE, insets, 0, 0));
        setMinimumSize(getPreferredSize());

        processList.addSelectionListener(e -> app.processSelectionChanged());
    }

    @Override
    protected void updateState() {
        //TODO
    }

    public AkerProcess getSelectedProcess() {
        return processList.getSelectedItem();
    }

    public Catalogue getCatalogue() {
        return app.getCatalogue();
    }

    public void modulesUpdated() {
        moduleList.repaint();
    }

    public void productsUpdated() {
        productList.repaint();
    }
}
