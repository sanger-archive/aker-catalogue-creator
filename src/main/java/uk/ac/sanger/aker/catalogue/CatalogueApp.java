package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.component.*;
import uk.ac.sanger.aker.catalogue.component.ComponentFactory.RunnableAction;
import uk.ac.sanger.aker.catalogue.conversion.JsonExporter;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;
import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.graph.ModuleLayoutUtil;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.*;
import java.awt.FileDialog;
import java.io.FilenameFilter;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
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
    private Action validateAction;
    private Action allUuidsAction;
    private final FilenameFilter filenameFilter = (dir, name) -> endsWithIgnoreCase(name, EXTENSION);
    private Path filePath;

    private Map<AkerProcess, ModuleLayout> moduleLayoutCache = new HashMap<>();


    @Override
    public void run() {
        catalogue = new Catalogue();
        createFrame();
        createActions();
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
        validateAction = new RunnableAction("Validate", this::validateCatalogue);
        allUuidsAction = new RunnableAction("Generate all new UUIDs", () -> fillInUuids(catalogue, true));
        copyModuleMapAction.setEnabled(false);
        pasteModuleMapAction.setEnabled(false);

        KeyShortcuts.NEW.register(frame, newAction);
        KeyShortcuts.OPEN.register(frame, openAction);
        KeyShortcuts.SAVE.register(frame, saveAction);
        KeyShortcuts.SAVE_AS.register(frame, saveAsAction);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = createMenu("File", newAction, openAction, null, saveAction, saveAsAction);
        JMenu editMenu = createMenu("Edit", validateAction, null, allUuidsAction, null,
                copyModuleMapAction, pasteModuleMapAction);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    private JMenu createMenu(String name, Action... actions) {
        JMenu menu = new JMenu(name);
        for (Action action : actions) {
            if (action==null) {
                menu.addSeparator();
            } else {
                menu.add(action);
            }
        }
        return menu;
    }

    public Catalogue getCatalogue() {
        return this.catalogue;
    }

    public void view(Module module) {
        copyModuleMapAction.setEnabled(false);
        pasteModuleMapAction.setEnabled(false);
        frame.view(module);
    }

    public void view(Product product) {
        copyModuleMapAction.setEnabled(false);
        pasteModuleMapAction.setEnabled(false);
        frame.view(product);
    }

    public void view(AkerProcess process) {
        copyModuleMapAction.setEnabled(process!=null);
        pasteModuleMapAction.setEnabled(process!=null && copiedModuleMap!=null);
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
        filePath = null;
        frame.clear();
        moduleLayoutCache.clear();
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
        checkPathValidity();
        filePath = path;
        frame.clear();
        moduleLayoutCache.clear();
    }

    private void saveCatalogueAs() {
        if (!validateForSave()) {
            return;
        }
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
        } else if (validateForSave()) {
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

    private void checkPathValidity() {
        List<AkerProcess> problemProcesses = catalogue.getProcesses().stream()
                .filter(pro -> !layOutModules(pro))
                .collect(Collectors.toList());
        if (problemProcesses.isEmpty()) {
            return;
        }
        String desc = MessageVar.process("The following process(es) contain(|s) cyclic or invalid path " +
                "definitions that cannot be loaded:", problemProcesses.size());
        StringBuilder sb = new StringBuilder("<p>").append(desc).append("<ul>");
        for (AkerProcess pro : problemProcesses) {
            sb.append("<li>").append(escapeHtml4(pro.getName()));
        }
        sb.append("</ul>");
        showWarning(htmlWrap(sb.toString()), "Invalid routes");

    }

    private boolean layOutModules(AkerProcess pro) {
        try {
            ModuleLayout layout = ModuleLayoutUtil.layOut(pro.getModulePairs());
            saveModuleLayout(pro, layout);
            return true;
        } catch (Exception e) {
            pro.setModulePairs(new ArrayList<>());
            return false;
        }
    }

    private boolean savePath(Path path) {
        fillInUuids(catalogue, false);
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

    private void fillInUuids(Catalogue catalogue, final boolean force) {
        Stream.<HasUuid>concat(catalogue.getProcesses().stream(), catalogue.getProducts().stream())
                .forEach(item -> {
                    String uuid = item.getUuid();
                    if (force || uuid==null || uuid.isEmpty()) {
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
        copiedModuleMap = new CopiedModuleMap(getModuleLayout(pro), pro.getModulePairs());
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
        saveModuleLayout(pro, copiedModuleMap.getLayout());
        pro.setModulePairs(copiedModuleMap.getPairs());
        pasteModuleMapAction.setEnabled(false);
        frame.clearEditPanel();
        frame.view(pro);
    }

    private static String htmlWrap(String body) {
        return "<html><body style='width:400px; padding: 5px;'>" + body + "</body></html>";
    }

    private void showError(String title, String message, Exception exception) {
        String text = htmlWrap(message + "<br>" + escapeHtml4(exception.getMessage()));
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.ERROR_MESSAGE);
    }

    private void validateCatalogue() {
        Validator validator = new Validator(this::getModuleLayout);
        if (validator.findProblems(catalogue)) {
            showWarning(htmlWrap(validator.problemsHtml()), "Problems found");
        } else {
            JOptionPane.showMessageDialog(frame, "No problems found.", "Valid", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showWarning(String text, String title) {
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.WARNING_MESSAGE);
    }

    private boolean validateForSave() {
        Validator validator = new Validator(this::getModuleLayout);
        if (!validator.findProblems(catalogue)) {
            return true;
        }
        String message = htmlWrap(validator.problemsHtml()
                + "<p>Do you want to ignore these problems and save anyway?</p>");
        int result = JOptionPane.showConfirmDialog(frame, message, "Problems found",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return (result==JOptionPane.OK_OPTION);
    }

    public ModuleLayout getModuleLayout(AkerProcess process) {
        return moduleLayoutCache.get(process);
    }

    public void saveModuleLayout(AkerProcess process, ModuleLayout layout) {
        moduleLayoutCache.put(process, layout);
    }
}
