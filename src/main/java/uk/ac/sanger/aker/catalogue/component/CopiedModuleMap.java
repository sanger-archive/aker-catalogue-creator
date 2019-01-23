package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dr6
 */
public class CopiedModuleMap {
    private ModuleLayout layout;
    private List<ModulePair> pairs;

    public CopiedModuleMap(ModuleLayout layout, List<? extends ModulePair> pairs) {
        this.layout = copy(layout);
        this.pairs = copy(pairs);
    }

    private static ModuleLayout copy(ModuleLayout layout) {
        if (layout==null) {
            return null;
        }
        return new ModuleLayout(layout.entries().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Point(e.getValue()))));
    }

    public static List<ModulePair> copy(List<? extends ModulePair> list) {
        if (list==null) {
            return null;
        }
        return list.stream().map(ModulePair::new).collect(Collectors.toList());
    }

    public ModuleLayout getLayout() {
        return copy(this.layout);
    }

    public List<ModulePair> getPairs() {
        return copy(this.pairs);
    }

    public void filter(Set<Module> modules) {
        if (layout!=null) {
            layout.retainModules(modules);
        }
        if (pairs!=null) {
            pairs.removeIf(pair -> !pairStillValid(pair, modules));
        }
    }

    private static boolean pairStillValid(ModulePair pair, Set<Module> modules) {
        return (modules.contains(pair.getFrom()) && modules.contains(pair.getTo()));
    }
}
