package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.component.list.*;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeHeadline;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeTextField;

/**
 * The panel in the left hand pane of the {@link CatalogueFrame main frame}.
 * It contains the catalogue's simple data fields (url, pipeline etc.),
 * and lists the catalogue's contents (modules, processes and products).
 * The simple data and lists of contents can all be edited, and those
 * edits are saved to the {@link Catalogue model}.
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
    private boolean loading;

    public CataloguePanel(CatalogueApp app) {
        this.app = app;
        initComponents();
        layOut();
    }

    private void initComponents() {
        pipelineField = makeTextField();
        urlField = makeTextField();
        limsIdField = makeTextField();
        moduleList = new ListComponent<>("Modules:", new ModuleListActor(app));
        processList = new ListComponent<>("Processes:", new ProcessListActor(app));
        productList = new ListComponent<>("Products:", new ProductListActor(app));
        load();
        DocumentListener docListener = getDocumentListener();
        for (JTextField tf : new JTextField[]{pipelineField, urlField, limsIdField}) {
            tf.getDocument().addDocumentListener(docListener);
        }
    }

    @Override
    public void load() {
        if (loading) {
            return;
        }
        loading = true;
        Catalogue catalogue = getCatalogue();
        pipelineField.setText(catalogue.getPipeline());
        urlField.setText(catalogue.getUrl());
        limsIdField.setText(catalogue.getLimsId());
        moduleList.setItems(catalogue.getModules());
        processList.setItems(catalogue.getProcesses());
        productList.setItems(catalogue.getProducts());
        loading = false;
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(0,0,10,0);
        QuickConstraints constraints = new QuickConstraints(0,0,2,1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0);
        add(makeHeadline("Catalogue"), constraints);

        constraints.insets.left = 10;
        constraints.gridwidth = 1;
        add("Pipeline:", pipelineField, constraints.incy());
        add("URL:", urlField, constraints.incy());
        add("LIMS ID:", limsIdField, constraints.incy());

        constraints.left();
        constraints.gridwidth = 2;
        constraints.insets.top = 10;
        add(moduleList, constraints.incy());
        add(processList, constraints.incy());
        add(productList, constraints.incy());

        setMinimumSize(getPreferredSize());
    }

    @Override
    protected void updateState() {
        if (loading) {
            return;
        }
        Catalogue catalogue = getCatalogue();
        catalogue.setPipeline(pipelineField.getText().trim());
        catalogue.setUrl(urlField.getText().trim());
        catalogue.setLimsId(limsIdField.getText().trim());
    }

    @Override
    protected void fireOpen() {
        // nothing
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

    public void processesUpdated() {
        processList.repaint();
    }

}
