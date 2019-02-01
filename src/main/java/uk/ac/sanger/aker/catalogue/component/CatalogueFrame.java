package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.Help;
import uk.ac.sanger.aker.catalogue.model.Catalogue;

import javax.swing.*;

/**
 * The main frame for the catalogue application.
 * The frame is split into two panes: the catalogue panel on the left, and
 * the edit panel on the right. Selecting items in the left pane brings
 * up their properties in the right pane.
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

    /**
     * Clears out any gui state and reload the contents of the current catalogue.
     */
    public void clear() {
        clearEditPanel();
        cataloguePanel.load();
    }

    /**
     * Gets the current catalogue from the app.
     */
    public Catalogue getCatalogue() {
        return app.getCatalogue();
    }

    /**
     * View the given item in the right-hand pane of the frame.
     * If the item is null, the right pane will be cleared.
     * Otherwise, it must be a type suitable for the {@link EditPanelFactory}.
     * @param item the item to view (part of the model)
     * @param open true to focus the right pane for editing
     * @param <E> the type of the item
     * @exception IllegalArgumentException if an appropriate editpanel cannot be created for the given item
     */
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

    /**
     * Trigger the current edit panel (if any) to reload its content from its model.
     */
    public void editPanelLoad() {
        if (editPanel!=null) {
            editPanel.load();
        }
    }

    /**
     * Notify the catalogue panel that the products have been updated.
     */
    public void productsUpdated() {
        cataloguePanel.productsUpdated();
    }

    /**
     * Notify the catalogue panel that the processes have been updated.
     */
    public void processesUpdated() {
        cataloguePanel.processesUpdated();
    }

    /**
     * Notify the catalogue panel that the modules have been updated.
     */
    public void modulesUpdated() {
        cataloguePanel.modulesUpdated();
    }

    /**
     * Remove the current edit panel (if any), replacing it with an empty space in the right hand pane.
     */
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

    /**
     * Show the given text in the help dialog.
     * If there is already a help dialog, it is disposed first.
     * The help dialog is non-modal: you can keep it open while you carry on working in the main frame.
     * @param helpText the text to show in the dialog.
     */
    public void showHelp(String helpText) {
        disposeHelp();

        JOptionPane optionPane = new JOptionPane(helpText, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, Help.HELP_ICON);
        helpDialog = optionPane.createDialog(this, "Help");
        helpDialog.setModal(false);
        helpDialog.setVisible(true);
        helpDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Dispose the current help dialog, if any.
     */
    public void disposeHelp() {
        if (helpDialog!=null) {
            helpDialog.dispose();
            helpDialog = null;
        }
    }
}
