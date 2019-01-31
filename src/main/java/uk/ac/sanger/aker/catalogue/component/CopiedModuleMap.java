package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A clipboard record of a module map. That includes the layout (positions of modules)
 * and {@code ModulePair}s (paths between modules).
 * The data is copied defensively, both when this object is created, and when the data is
 * interrogated.
 * @author dr6
 */
public class CopiedModuleMap {
    private ModuleLayout layout;
    private List<ModulePair> pairs;

    /**
     * Creates a copy of the given layout and paths.
     * @param layout the positions of some modules
     * @param pairs paths between the modules
     */
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

    private static List<ModulePair> copy(List<? extends ModulePair> list) {
        if (list==null) {
            return null;
        }
        return list.stream().map(ModulePair::new).collect(Collectors.toList());
    }

    /**
     * Gets the layout (module positions) saved in this instance. They are copied.
     * @return a copy of the layout
     */
    public ModuleLayout getLayout() {
        return copy(this.layout);
    }

    /**
     * Gets the paths saved in this instance.
     * They are copied.
     * @return a copy of the paths
     */
    public List<ModulePair> getPairs() {
        return copy(this.pairs);
    }

    private static boolean pairStillValid(ModulePair pair, Set<Module> modules) {
        return (modules.contains(pair.getFrom()) && modules.contains(pair.getTo()));
    }

    /**
     * Remove references in this instance to modules that are not in the given set.
     * References includes positions in the layout, and paths in the {@code ModulePair}s.
     * @param modules the modules to retain
     */
    public void filter(Set<Module> modules) {
        if (layout!=null) {
            layout.retainModules(modules);
        }
        if (pairs!=null) {
            pairs.removeIf(pair -> !pairStillValid(pair, modules));
        }
    }
}
