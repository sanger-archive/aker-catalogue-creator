package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.Help;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;

/**
 * @author dr6
 */
public class CatalogueFrame extends JFrame {
    private CatalogueApp app;
    private CataloguePanel cataloguePanel;
    private JScrollPane editScrollPane;
    private JDialog helpDialog;

    public CatalogueFrame(CatalogueApp app) {
        super("Catalogue");
        this.app = app;
        cataloguePanel = new CataloguePanel(app);
        editScrollPane = new JScrollPane(new JPanel());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(cataloguePanel), editScrollPane);
        splitPane.setDividerLocation(cataloguePanel.getPreferredSize().width + 80);
        splitPane.setResizeWeight(0);

        setContentPane(splitPane);
        setBounds(50, 50, 1200, 700);
    }

    public void clear() {
        clearEditPanel();
        cataloguePanel.load();
    }

    public Catalogue getCatalogue() {
        return cataloguePanel.getCatalogue();
    }

    public void view(Module module) {
        disposeHelp();
        if (module == null) {
            clearEditPanel();
        } else {
            ModulePanel modulePanel = new ModulePanel(module, app);
            editScrollPane.setViewportView(modulePanel);
        }
    }

    public void view(Product product) {
        disposeHelp();
        if (product == null) {
            clearEditPanel();
        } else {
            ProductPanel productPanel = new ProductPanel(product, app);
            editScrollPane.setViewportView(productPanel);
        }
    }

    public void view(AkerProcess process) {
        disposeHelp();
        if (process == null) {
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
        disposeHelp();
    }

    public CataloguePanel getCataloguePanel() {
        return cataloguePanel;
    }

    public void showHelp(String helpText) {
        disposeHelp();

        JOptionPane optionPane = new JOptionPane(helpText, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, Help.HELP_ICON);
        helpDialog = optionPane.createDialog(this, "Help");
        helpDialog.setModal(false);
        helpDialog.setVisible(true);
    }

    public void disposeHelp() {
        if (helpDialog!=null) {
            helpDialog.dispose();
            helpDialog = null;
        }
    }
}
