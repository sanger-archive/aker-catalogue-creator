package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.component.*;
import uk.ac.sanger.aker.catalogue.component.ComponentFactory.RunnableAction;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dr6
 */
public class CatalogueApp implements Runnable {
    private Catalogue catalogue;
    private CatalogueFrame frame;
    private CopiedModuleMap copiedModuleMap;
    private Action newAction;
    private Action copyModuleMapAction;
    private Action pasteModuleMapAction;

    @Override
    public void run() {
        JsonImporter jim = new JsonImporter();
        try {
            catalogue = jim.importCatalogue(jim.readPath(Paths.get("/Users/dr6/Desktop/new_catalogue.json")));
        } catch (IOException e) {
            e.printStackTrace();
            catalogue = new Catalogue();
        }
        createActions();
        createFrame();
        frame.setJMenuBar(createMenuBar());
        frame.setVisible(true);
    }

    private CatalogueFrame createFrame() {
        frame = new CatalogueFrame(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    private void createActions() {
        newAction = new RunnableAction("New catalogue", this::newCatalogue);
        copyModuleMapAction = new RunnableAction("Copy module map", this::copyModuleMap);
        pasteModuleMapAction = new RunnableAction("Paste module map", this::pasteModuleMap);
        copyModuleMapAction.setEnabled(false);
        pasteModuleMapAction.setEnabled(false);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newAction);
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(copyModuleMapAction);
        editMenu.add(pasteModuleMapAction);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    public Catalogue getCatalogue() {
        return this.catalogue;
    }

    public void view(Module module) {
        frame.view(module);
    }

    public void view(Product product) {
        frame.view(product);
    }

    public void view(AkerProcess process) {
        frame.view(process);
    }

    public void productsUpdated() {
        frame.productsUpdated();
    }

    public void clearEditPanel() {
        frame.clearEditPanel();
    }

    public CatalogueFrame getFrame() {
        return this.frame;
    }

    private AkerProcess getSelectedProcess() {
        CataloguePanel cp = frame.getCataloguePanel();
        return (cp==null ? null : cp.getSelectedProcess());
    }

    private void newCatalogue() {
        catalogue = new Catalogue();
        frame.clear();
    }

    private void copyModuleMap() {
        AkerProcess pro = getSelectedProcess();
        if (pro==null) {
            return;
        }
        copiedModuleMap = new CopiedModuleMap(frame.getModuleLayout(pro), pro.getModulePairs());
    }

    private void pasteModuleMap() {
        AkerProcess pro = getSelectedProcess();
        if (pro==null || copiedModuleMap==null) {
            return;
        }
        Set<Module> catalogueModules = new HashSet<>(catalogue.getModules());
        catalogueModules.add(Module.START);
        catalogueModules.add(Module.END);
        copiedModuleMap.filter(catalogueModules);
        frame.saveModuleLayout(pro, copiedModuleMap.getLayout());
        pro.setModulePairs(copiedModuleMap.getPairs());
        pasteModuleMapAction.setEnabled(false);
        frame.clearEditPanel();
        frame.view(pro);
    }

    public void processSelectionChanged() {
        boolean proSel = (getSelectedProcess()!=null);
        copyModuleMapAction.setEnabled(proSel);
        pasteModuleMapAction.setEnabled(proSel && copiedModuleMap!=null);
    }
}
