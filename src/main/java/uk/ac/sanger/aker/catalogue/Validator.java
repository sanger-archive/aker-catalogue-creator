package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author dr6
 */
public class Validator {
    enum Problem {
        PROCESS_WITH_DISCONNECTED_MODULES("The following process(es) contain(s|) modules with no routes. " +
                "Those modules will not be linked to the process(es) when the catalogue is saved:"),
        PROCESS_WITH_NO_MODULES("The following process(es) contain(s|) no modules:"),
        ORPHAN_MODULE("The following module(s) (is|are) not part of any process, and will not be " +
                "included when the catalogue is saved:"),
        INVALID_DEFAULT_ROUTE("The following process(es) (has|have) invalid or missing default routes:"),
        ;

        private final String desc;

        Problem(String desc) {
            this.desc = desc;
        }

        public String getText(int number) {
            return pluralise(this.desc, (number!=1));
        }
    }

    private Map<Problem, List<String>> problems;
    private boolean anyProblems;
    private Function<AkerProcess, ModuleLayout> layoutProvider;

    public Validator(Function<AkerProcess, ModuleLayout> layoutProvider) {
        this.layoutProvider = layoutProvider;
        problems = new EnumMap<>(Problem.class);
        for (Problem problem : Problem.values()) {
            problems.put(problem, new ArrayList<>());
        }
    }

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
        return anyProblems;
    }

    public void addProblem(Problem problem, HasName named) {
        problems.get(problem).add(named.getName());
        anyProblems = true;
    }

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

    public static String pluralise(String text, boolean plural) {
        StringBuilder sb = new StringBuilder(text);
        int i = sb.indexOf("(");
        while (i >= 0) {
            int j = sb.indexOf("|", i);
            int k = sb.indexOf(")", i);
            if (j < i || j > k) {
                j = i;
            }
            if (plural) {
                if (j+1 < k) {
                    sb.replace(i, k+1, sb.substring(j+1, k));
                } else {
                    sb.delete(i, k+1);
                }
            } else {
                if (i+1 < j) {
                    sb.replace(i, k + 1, sb.substring(i + 1, j));
                } else {
                    sb.delete(i, k+1);
                }
            }

            i = sb.indexOf("(", i);
        }
        return sb.toString();
    }
}
