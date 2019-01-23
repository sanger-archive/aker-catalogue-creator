package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;
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

    public void clear() {
        moduleLayoutCache.clear();
        clearEditPanel();
        cataloguePanel.load();
    }

    public Catalogue getCatalogue() {
        return cataloguePanel.getCatalogue();
    }

    public void view(Module module) {
        if (module==null) {
            clearEditPanel();
        } else {
            ModulePanel modulePanel = new ModulePanel(module, app);
            editScrollPane.setViewportView(modulePanel);
        }
    }

    public void view(Product product) {
        if (product==null) {
            clearEditPanel();
        } else {
            ProductPanel productPanel = new ProductPanel(product, app);
            editScrollPane.setViewportView(productPanel);
        }
    }

    public void view(AkerProcess process) {
        if (process==null) {
            clearEditPanel();
        } else {
            ProcessPanel processPanel = new ProcessPanel(process, app);
            editScrollPane.setViewportView(processPanel);
        }
    }

    public void productsUpdated() {
        cataloguePanel.productsUpdated();
    }

    public void processesUpdated() {
        cataloguePanel.processesUpdated();
    }

    public void modulesUpdated() {
        cataloguePanel.modulesUpdated();
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

    public CataloguePanel getCataloguePanel() {
        return cataloguePanel;
    }
}
