package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.Help;
import uk.ac.sanger.aker.catalogue.model.Catalogue;

import javax.swing.*;

/**
 * @author dr6
 */
public class CatalogueFrame extends JFrame {
    private CatalogueApp app;
    private CataloguePanel cataloguePanel;
    private JScrollPane editScrollPane;
    private JDialog helpDialog;
    private EditPanel editPanel;
    private EditPanelFactory editPanelFactory;

    public CatalogueFrame(CatalogueApp app) {
        super("Catalogue");
        this.app = app;
        this.editPanelFactory = new EditPanelFactory(app);
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

    public <E> void view(E item, boolean open) {
        disposeHelp();
        if (item==null) {
            clearEditPanel();
        } else {
            editPanel = editPanelFactory.makePanel(item);
            editScrollPane.setViewportView(editPanel);
            if (open) {
                editPanel.fireOpen();
            }
        }
    }

    public void editPanelLoad() {
        if (editPanel!=null) {
            editPanel.load();
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
        if (editPanel!=null) {
            disposeHelp();
            editScrollPane.setViewportView(new JPanel());
            editPanel = null;
        }
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
