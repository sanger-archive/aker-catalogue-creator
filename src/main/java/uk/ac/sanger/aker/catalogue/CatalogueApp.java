package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.component.*;
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
 * The main controller class of the application. This links the view ({@link CatalogueFrame} etc.)
 * and the model ({@link Catalogue} etc.).
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

    /**
     * Creates a new catalogue and frame and shows it.
     * This method is invoked by the AWT thread when the application is run.
     * @see Main#main
     */
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

    /** Gets the current catalogue, which is the root model. */
    public Catalogue getCatalogue() {
        return this.catalogue;
    }

    /**
     * Activate the given item in the view.
     * This means either bring the item into focus ({@code open=true}) or simply select it ({@code open=false}).
     * This method enables/disabled the copy/paste map actions in the map, and then calls
     * a method in the frame to show the item.
     * @param item the item to view (null to clear the view)
     * @param open true to shift focus to the new edit panel for the item
     * @param <E> the type of item (a model class)
     */
    public <E> void view(E item, boolean open) {
        boolean isProcess = (item instanceof AkerProcess);
        copyModuleMapAction.setEnabled(isProcess);
        pasteModuleMapAction.setEnabled(isProcess && copiedModuleMap!=null);
        frame.view(item, open);
    }

    /** This is triggered when the products are updated, and it informs the frame. */
    public void productsUpdated() {
        frame.productsUpdated();
    }
    /** This is triggered when the modules are updated, and it informs the frame. */
    public void modulesUpdated() {
        frame.modulesUpdated();
    }
    /** This is triggered when the processes are updated, and it informs the frame. */
    public void processesUpdated() {
        frame.processesUpdated();
    }

    /** Tells the frame to clear its edit panel. */
    public void clearEditPanel() {
        frame.clearEditPanel();
    }

    /** Gets the main frame of the application. */
    public CatalogueFrame getFrame() {
        return this.frame;
    }

    /** Gets whatever process is currently showing in the edit panel of the main frame. */
    private AkerProcess getSelectedProcess() {
        CataloguePanel cp = frame.getCataloguePanel();
        return (cp==null ? null : cp.getSelectedProcess());
    }

    /** Sets the current catalogue to a new empty catalogue. */
    private void newCatalogue() {
        catalogue = new Catalogue();
        filePath = null;
        frame.clear();
        moduleLayoutCache.clear();
    }

    /** Shows a file dialog and loads a catalogue. */
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

    /**
     * Shows a save dialog and saves the catalogue.
     * Before that, validates the catalogue and warns the user of any problems.
     */
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

    /**
     * If there is a set file path, saves to it, otherwise shows a save dialog and saves.
     * Before that, validates the catalogue and warns the user of any problems.
     */
    private void saveCatalogue() {
        if (filePath==null) {
            saveCatalogueAs();
        } else if (validateForSave()) {
            savePath(filePath);
        }
    }

    /**
     * Loads the catalogue from the given file path.
     * If trying to load the file raises an exception, an error will be displayed, and
     * this method will return null.
     * @param path the path of the file to load
     * @return the loaded catalogue, or null if there was an error
     */
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

    /**
     * Checks the current catalogue (that has just been loaded) for cyclic paths.
     * This is done immediately so the user can be informed at once, instead of finding out
     * when they try to open the process for viewing.
     * When an invalid module graph is found, it is deleted.
     */
    private void checkPathValidity() {
        List<AkerProcess> problemProcesses = catalogue.getProcesses().stream()
                .filter(pro -> !layOutModules(pro))
                .collect(Collectors.toList());
        if (problemProcesses.isEmpty()) {
            return;
        }
        String desc = MessageVar.process("The following process{es} contain{s|} cyclic or invalid path " +
                "definitions that cannot be loaded:", problemProcesses.size());
        StringBuilder sb = new StringBuilder("<p>").append(desc).append("<ul>");
        for (AkerProcess pro : problemProcesses) {
            sb.append("<li>").append(escapeHtml4(pro.getName()));
        }
        sb.append("</ul>");
        showWarning(htmlWrap(sb.toString()), "Invalid routes");
    }

    /**
     * Lays out the modules for the given process, using the {@link ModuleLayoutUtil}.
     * The layout is saved in the {@link #getLayoutCache() layout cache}.
     * If the path in the process is invalid (i.e. contains cycles), then the path for that
     * process is deleted, and this method returns false.
     * @param pro the process to lay out
     * @return true if a layout was generated and stored; false if the graph was invalid and therefore deleted
     */
    private boolean layOutModules(AkerProcess pro) {
        try {
            ModuleLayout layout = ModuleLayoutUtil.layOut(pro.getModulePairs());
            getLayoutCache().put(pro, layout);
            return true;
        } catch (Exception e) {
            pro.setModulePairs(new ArrayList<>());
            return false;
        }
    }

    /**
     * Saves the catalogue to the given path.
     * If the save fails, an error message is shown to the user, and this method returns false.
     * @param path the file path to save to
     * @return true if successful, false if unsuccessful
     */
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

    /**
     * Fills in UUIDs for the products and processes in this catalogue.
     * If {@code force} is true, then existing UUIDs are replaced.
     * Otherwise, UUIDs will only be inserted where they are missing.
     * @param catalogue the catalogue to add UUIDs into
     * @param force true to replace existing UUIDs
     */
    private void fillInUuids(Catalogue catalogue, final boolean force) {
        Stream.<HasUuid>concat(catalogue.getProcesses().stream(), catalogue.getProducts().stream())
                .forEach(item -> {
                    String uuid = item.getUuid();
                    if (force || uuid==null || uuid.isEmpty()) {
                        item.setUuid(UUID.randomUUID().toString());
                    }
                });
        frame.editPanelLoad();
    }

    /**
     * Helper method: does the first given string end with the second given string, ignoring case?
     * If either argument is null, returns false.
     * This uses {@link String#regionMatches} to compare parts of the string, so it does not
     * require transforming the whole strings to upper or lower case (or both).
     * @param string the string to look inside
     * @param sub the string that might be at the end of the first string
     * @return true if {@code string} ends with {@code sub}, ignoring case.
     */
    private static boolean endsWithIgnoreCase(String string, String sub) {
        return (string!=null && sub!=null && string.length() >= sub.length() &&
                string.regionMatches(true, string.length()-sub.length(), sub, 0, sub.length()));
    }

    /**
     * Shows a file dialog.
     * @param path the initially selected file path
     * @param mode either {@link FileDialog#LOAD} or {@link FileDialog#SAVE}, indicating the type of
     *             file dialog to show
     * @return the path selected by the user in the file dialog, or null if the dialog was closed without
     *         selecting a file
     */
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

    /**
     * Copies the module positions and paths from the currently selected process (if any) to a field
     * so they can be later pasted somewhere else.
     * @see CopiedModuleMap
     */
    private void copyModuleMap() {
        AkerProcess pro = getSelectedProcess();
        if (pro==null) {
            return;
        }
        copiedModuleMap = new CopiedModuleMap(getLayoutCache().get(pro), pro.getModulePairs());
    }

    /**
     * Pastes the copied module positions and paths (if any) to the currently selected process (if any).
     * Notifies the frame to reopen the process to view its changed contents.
     * @see CopiedModuleMap
     */
    private void pasteModuleMap() {
        AkerProcess pro = getSelectedProcess();
        if (pro==null || copiedModuleMap==null) {
            return;
        }
        Set<Module> catalogueModules = new HashSet<>(catalogue.getModules());
        catalogueModules.add(Module.START);
        catalogueModules.add(Module.END);
        copiedModuleMap.filter(catalogueModules);
        getLayoutCache().put(pro, copiedModuleMap.getLayout());
        pro.setModulePairs(copiedModuleMap.getPairs());
        pasteModuleMapAction.setEnabled(false);
        frame.clearEditPanel();
        frame.view(pro, false);
    }

    /**
     * Wraps the given html body in tags for display in a {@link JOptionPane}
     * @param body html text
     * @return html text wrapped in {@code <html><body>} tags
     */
    public static String htmlWrap(String body) {
        return "<html><body style='width:400px; padding: 5px;'>" + body + "</body></html>";
    }

    /**
     * Shows the given error message and details of the exception.
     * The exception stack trace will be html-escaped and included in the message.
     * @param title the title for the message
     * @param message the text of the message (html)
     * @param exception the exception to show the stack trace for
     */
    private void showError(String title, String message, Exception exception) {
        String text = htmlWrap(message + "<br>" + escapeHtml4(exception.getMessage()));
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Checks the catalogue for problems, and display a message to the user.
     * @see Validator
     */
    private void validateCatalogue() {
        Validator validator = new Validator(process -> moduleLayoutCache.get(process));
        if (validator.findProblems(catalogue)) {
            showWarning(htmlWrap(validator.problemsHtml()), "Problems found");
        } else {
            showInfo("No problems found.", "Valid");
        }
    }

    /**
     * Shows a warning in a {@link JOptionPane}. The given text should be already prepared for html.
     * @param text the text of the warning
     * @param title the title of the warning
     */
    private void showWarning(String text, String title) {
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows information in {@link JOptionPane}. The given text should be already prepared for html.
     * @param text the text of the message
     * @param title the title of the message
     */
    private void showInfo(String text, String title) {
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Checks the catalogue for problems in preparation for a save.
     * If problems are found, the user must confirm that they want to proceed.
     * @return true to proceed, false to halt.
     */
    private boolean validateForSave() {
        Validator validator = new Validator(process -> moduleLayoutCache.get(process));
        if (!validator.findProblems(catalogue)) {
            return true;
        }
        String message = htmlWrap(validator.problemsHtml()
                + "<p>Do you want to ignore these problems and save anyway?</p>");
        int result = JOptionPane.showConfirmDialog(frame, message, "Problems found",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return (result==JOptionPane.OK_OPTION);
    }

    /**
     * Gets the map of process to the layout for that process.
     * This gives direct access to the map so it can be updated by a receiving method.
     */
    public Map<AkerProcess, ModuleLayout> getLayoutCache() {
        return this.moduleLayoutCache;
    }

}
