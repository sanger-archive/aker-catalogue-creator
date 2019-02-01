package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * Validate various features of a catalogue.
 * Types of problem are enumerated by {@link Validator.Problem}.
 * <ul>
 *     <li>A module added to a process graph is not connected via any path.</li>
 *     <li>A process graph contains no modules.</li>
 *     <li>A module is not part of any process.</li>
 *     <li>A process graph doesn't have a valid default route.</li>
 *     <li>Multiple {process/modules/products} have the same name.</li>
 *     <li>Multiple objects in the catalogue have the same UUID.</li>
 * </ul>
 * Call {@link #findProblems findProblems findProblems} to see if there are any problems, and
 * {@link #problemsHtml problemsHtml} to get an html description of the problems found.
 * @author dr6
 */
public class Validator {
    enum Problem {
        PROCESS_WITH_DISCONNECTED_MODULES("The following process{es} contain{s|} modules with no routes. " +
                "Those modules will not be linked to the process{es} when the catalogue is saved:"),
        PROCESS_WITH_NO_MODULES("The following process{es} contain{s|} no modules:"),
        ORPHAN_MODULE("The following module{s} {is|are} not part of any process, and will not be " +
                "included when the catalogue is saved:"),
        INVALID_DEFAULT_ROUTE("The following process{es} {has|have} invalid or missing default routes:"),
        DUPLICATE_MODULE_NAME("There are multiple modules with the following name{s}:"),
        DUPLICATE_PROCESS_NAME("There are multiple processes with the following name{s}:"),
        DUPLICATE_PRODUCT_NAME("There are multiple products with the following name{s}:"),
        DUPLICATE_UUIDS("The following UUID{s} {is|are} duplicated:"),
        ;

        private final String desc;

        Problem(String desc) {
            this.desc = desc;
        }

        public String getText(int number) {
            return MessageVar.process(this.desc, number);
        }
    }

    private Map<Problem, List<String>> problems;
    private boolean anyProblems;
    private Function<AkerProcess, ModuleLayout> layoutProvider;

    /**
     * Constructs a new validator.
     * @param layoutProvider the function that will give the {@link ModuleLayout} for a
     *                       given {@link AkerProcess process}.
     */
    public Validator(Function<AkerProcess, ModuleLayout> layoutProvider) {
        this.layoutProvider = layoutProvider;
        problems = new EnumMap<>(Problem.class);
        for (Problem problem : Problem.values()) {
            problems.put(problem, new ArrayList<>());
        }
    }

    /**
     * Finds problems with the catalogue. The problems are stored inside the {@code Validator}, and
     * details can be retrieved subsequently with {@link #problemsHtml problemsHtml}.
     * @param catalogue the catalogue to check for problems
     * @return true if any problems were found, otherwise false
     */
    public boolean findProblems(Catalogue catalogue) {
        anyProblems = false;
        Set<Module> usedModules = new HashSet<>(catalogue.getModules().size());
        for (AkerProcess pro : catalogue.getProcesses()) {
            Set<Module> proModules = pro.getModulePairs().stream()
                    .map(ModulePair::getTo)
                    .filter(mod -> mod!=Module.END)
                    .collect(Collectors.toSet());
            ModuleLayout layout = layoutProvider.apply(pro);
            if (layout!=null && !layout.modules().stream().allMatch(mod -> mod.isEndpoint() || proModules.contains(mod))) {
                addProblem(Problem.PROCESS_WITH_DISCONNECTED_MODULES, pro);
            }
            usedModules.addAll(proModules);
            if (proModules.isEmpty()) {
                addProblem(Problem.PROCESS_WITH_NO_MODULES, pro);
            }
            if (!defaultRouteValid(pro.getModulePairs())) {
                addProblem(Problem.INVALID_DEFAULT_ROUTE, pro);
            }
        }
        for (Module module : catalogue.getModules()) {
            if (!usedModules.contains(module)) {
                addProblem(Problem.ORPHAN_MODULE, module);
            }
        }
        findDuplicateNames(Problem.DUPLICATE_MODULE_NAME, catalogue.getModules());
        findDuplicateNames(Problem.DUPLICATE_PROCESS_NAME, catalogue.getProcesses());
        findDuplicateNames(Problem.DUPLICATE_PRODUCT_NAME, catalogue.getProducts());
        checkUuids(Stream.of(catalogue.getProducts(), catalogue.getProcesses()).flatMap(Collection::stream));
        return anyProblems;
    }

    /**
     * Get an html description of the problems found.
     * This will comprise a series of paragraphs (the types of problem), each containing an unordered list
     * (the items found with that problem).
     * {@link #findProblems findProblems} should have been called before this method is called.
     * @return a string describing the problems found in html
     */
    public String problemsHtml() {
        StringBuilder sb = new StringBuilder();
        for (Problem problem : Problem.values()) {
            List<String> items = problems.get(problem);
            if (items.isEmpty()) {
                continue;
            }
            sb.append("<p>").append(problem.getText(items.size()));
            sb.append("<ul>");
            for (String item : items) {
                sb.append("<li>").append(item);
            }
            sb.append("</ul>");
            sb.append("</p>");
        }
        return sb.toString();
    }

    private void addProblem(Problem problem, HasName named) {
        addProblem(problem, named.getName());
    }

    private void addProblem(Problem problem, String name) {
        addHtmlProblem(problem, escapeHtml4(name));
    }

    private void addHtmlProblem(Problem problem, String text) {
        problems.get(problem).add(text);
        anyProblems = true;
    }

    private void findDuplicateNames(Problem problem, List<? extends HasName> items) {
        Map<String, Integer> nameCounter = new HashMap<>(items.size());
        for (HasName item : items) {
            String name = item.getName();
            nameCounter.put(name, nameCounter.getOrDefault(name, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : nameCounter.entrySet()) {
            if (entry.getValue() > 1) {
                addHtmlProblem(problem, escapeHtml4(entry.getKey()));
            }
        }
    }

    private void checkUuids(Stream<HasUuid> itemStream) {
        Map<String, List<HasUuid>> uuidMap = new HashMap<>();
        itemStream.forEach(item -> {
            String uuid = item.getUuid();
            if (uuid!=null && !uuid.isEmpty()) {
                uuidMap.computeIfAbsent(uuid, k -> new ArrayList<>()).add(item);
            }
        });
        for (Map.Entry<String, List<HasUuid>> entry : uuidMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                addHtmlProblem(Problem.DUPLICATE_UUIDS, escapeHtml4(entry.getKey())+makeUL(entry.getValue()));
            }
        }
    }

    /**
     * Constructs a string containing an unordered list ({@code <ul>...</ul>} in html).
     * Each item will be listed as {@code "Product: <name>"} for a {@link Product product}, and
     * {@code "Process: <name>"} for a {@link AkerProcess process}.
     * The names of the items will be escaped using
     * {@link org.apache.commons.lang3.StringEscapeUtils#escapeHtml4 StringEscapeUtils.escapeHtml4}.
     * @param items the items to include in the list
     * @return a string containing the html for an unordered list.
     */
    private static String makeUL(List<?> items) {
        StringBuilder sb = new StringBuilder("<ul>");
        for (Object item : items) {
            sb.append("<li>");
            if (item instanceof Product) {
                sb.append("Product: ");
            } else if (item instanceof AkerProcess) {
                sb.append("Process: ");
            }
            sb.append(escapeHtml4(item.toString()));
        }
        sb.append("</ul>");
        return sb.toString();
    }

    /**
     * This method checks if there is a valid default path in the given list of paths.
     * @param pairs The pairs of modules representing paths between modules (each of which may be marked as "default")
     * @return true if there is a single default path through the graph, otherwise false
     */
    public static boolean defaultRouteValid(List<ModulePair> pairs) {
        Map<Module, Module> defaultPath = new HashMap<>(pairs.size());
        for (ModulePair pair : pairs) {
            if (!pair.isDefaultPath()) {
                continue;
            }
            if (defaultPath.containsKey(pair.getFrom())) {
                return false;
            }
            defaultPath.put(pair.getFrom(), pair.getTo());
        }
        Module cur = Module.START;
        int length = 0;
        while (cur != Module.END) {
            cur = defaultPath.get(cur);
            length += 1;
            if (cur==null || length > defaultPath.size()) {
                return false;
            }
        }
        return (length == defaultPath.size());
    }
}
