package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.component.*;
import uk.ac.sanger.aker.catalogue.component.ComponentFactory.RunnableAction;
import uk.ac.sanger.aker.catalogue.conversion.JsonExporter;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;
import java.awt.FileDialog;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * @author dr6
 */
public class CatalogueApp implements Runnable {
    private static final String EXTENSION = ".json";
    private static final String DEFAULT_FILENAME = "catalogue" + EXTENSION;

    private Catalogue catalogue;
    private CatalogueFrame frame;
    private CopiedModuleMap copiedModuleMap;
    private Action newAction;
    private Action openAction;
    private Action saveAction;
    private Action saveAsAction;
    private Action copyModuleMapAction;
    private Action pasteModuleMapAction;
    private final FilenameFilter filenameFilter = (dir, name) -> endsWithIgnoreCase(name, EXTENSION);
    private Path filePath;

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
        saveAction = new RunnableAction("Save", this::saveCatalogue);
        saveAsAction = new RunnableAction("Save as...", this::saveCatalogueAs);
        openAction = new RunnableAction("Open...", this::openCatalogue);
        copyModuleMapAction.setEnabled(false);
        pasteModuleMapAction.setEnabled(false);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newAction);
        fileMenu.add(openAction);
        fileMenu.add(saveAction);
        fileMenu.add(saveAsAction);
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
        copyModuleMapAction.setEnabled(false);
        frame.view(module);
    }

    public void view(Product product) {
        copyModuleMapAction.setEnabled(false);
        frame.view(product);
    }

    public void view(AkerProcess process) {
        copyModuleMapAction.setEnabled(process!=null);
        frame.view(process);
    }

    public void productsUpdated() {
        frame.productsUpdated();
    }
    public void modulesUpdated() {
        frame.modulesUpdated();
    }
    public void processesUpdated() {
        frame.processesUpdated();
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

    private void openCatalogue() {
        Path path = requestFilePath(filePath, FileDialog.LOAD);
        if (path==null) {
            return;
        }

        Catalogue catalogue = loadPath(path);
        if (catalogue==null) {
            return;
        }
        this.catalogue = catalogue;
        filePath = path;
        frame.clear();
    }

    private void saveCatalogueAs() {
        Path path = requestFilePath(filePath, FileDialog.SAVE);
        if (path==null) {
            return;
        }
        if (savePath(path)) {
            this.filePath = path;
        }
    }

    private void saveCatalogue() {
        if (filePath==null) {
            saveCatalogueAs();
        } else {
            savePath(filePath);
        }
    }

    private Catalogue loadPath(Path path) {
        JsonImporter jim = new JsonImporter();
        Catalogue catalogue;
        try {
            catalogue = jim.importCatalogue(jim.readPath(path));
        } catch (Exception e) {
            e.printStackTrace();
            showError("File error", "An error occurred trying to load the file.", e);
            catalogue = null;
        }
        return catalogue;
    }

    private boolean savePath(Path path) {
        fillInMissingUuids(catalogue);
        JsonExporter jex = new JsonExporter();
        try {
            jex.write(jex.toExportData(catalogue), path);
        } catch (Exception e) {
            e.printStackTrace();
            showError("File error", "An error occurred trying to save the file.", e);
            return false;
        }
        return true;
    }

    private void fillInMissingUuids(Catalogue catalogue) {
        Stream.<HasUuid>concat(catalogue.getProcesses().stream(), catalogue.getProducts().stream())
                .forEach(item -> {
                    String uuid = item.getUuid();
                    if (uuid==null || uuid.isEmpty()) {
                        item.setUuid(UUID.randomUUID().toString());
                    }
                });
        frame.clearEditPanel();
    }

    private static boolean endsWithIgnoreCase(String string, String sub) {
        return (string!=null && sub!=null && string.length() >= sub.length() &&
                string.regionMatches(true, string.length()-sub.length(), sub, 0, sub.length()));
    }

    private Path requestFilePath(Path path, int mode) {
        FileDialog fd = new FileDialog(frame, mode==FileDialog.SAVE ? "Save catalogue" : "Load catalogue", mode);
        if (path!=null) {
            fd.setDirectory(path.getParent().toAbsolutePath().toString());
            fd.setFile(path.getFileName().toString());
        } else {
            fd.setFile(DEFAULT_FILENAME);
        }
        fd.setFilenameFilter(filenameFilter);
        fd.setVisible(true);
        String filename = fd.getFile();
        if (filename==null) {
            return null;
        }
        String dir = fd.getDirectory();
        if (!endsWithIgnoreCase(filename, EXTENSION) && mode==FileDialog.SAVE) {
            filename += EXTENSION;
            path = Paths.get(dir, filename);
            if (Files.exists(path)) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        String.format("The file %s already exists. Overwrite it?", path),
                        "Overwrite", JOptionPane.OK_CANCEL_OPTION);
                if (confirm!=JOptionPane.OK_OPTION) {
                    return null;
                }
            }
            return path;
        }
        return Paths.get(dir, filename);
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

    private void showError(String title, String message, Exception exception) {
        String text = String.format("<html>%s<br>%s</html>", message, escapeHtml4(exception.getMessage()));
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.ERROR_MESSAGE);
    }

}
