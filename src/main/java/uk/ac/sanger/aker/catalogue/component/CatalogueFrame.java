package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dr6
 */
public class CatalogueFrame extends JFrame {
    private CatalogueApp app;
    private CataloguePanel cataloguePanel;
    private JScrollPane editScrollPane;
    private Map<AkerProcess, ModuleLayout> moduleLayoutCache = new HashMap<>();

    public CatalogueFrame(CatalogueApp app) {
        super("Catalogue");
        this.app = app;
        cataloguePanel = new CataloguePanel(app);
        editScrollPane = new JScrollPane(new JPanel());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(cataloguePanel), editScrollPane);
        splitPane.setDividerLocation(cataloguePanel.getPreferredSize().width + 80);
        splitPane.setResizeWeight(1);

        setContentPane(splitPane);
        setBounds(50,50,1200,700);
    }

    public Catalogue getCatalogue() {
        return cataloguePanel.getCatalogue();
    }

    public void view(Module module) {
        ModulePanel modulePanel = new ModulePanel(module, cataloguePanel::modulesUpdated);
        editScrollPane.setViewportView(modulePanel);
        modulePanel.claimFocus();
    }

    public void view(Product product) {
        ProductPanel productPanel = new ProductPanel(product, app);
        editScrollPane.setViewportView(productPanel);
        productPanel.claimFocus();
    }

    public void view(AkerProcess process) {
        ProcessPanel processPanel = new ProcessPanel(process, app);
        editScrollPane.setViewportView(processPanel);
        processPanel.claimFocus();
    }

    public void productsUpdated() {
        cataloguePanel.productsUpdated();
    }

    public void clearEditPanel() {
        editScrollPane.setViewportView(new JPanel());
    }

    public ModuleLayout getModuleLayout(AkerProcess process) {
        return moduleLayoutCache.get(process);
    }

    public void saveModuleLayout(AkerProcess process, ModuleLayout layout) {
        moduleLayoutCache.put(process, layout);
    }
}
