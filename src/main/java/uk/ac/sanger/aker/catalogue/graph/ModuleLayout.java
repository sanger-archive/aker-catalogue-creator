package uk.ac.sanger.aker.catalogue.graph;

import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container for the positions of modules in a graph.
 * This is a simple wrapper for a map of {@link Module} to {@link Point}.
 * At one point it had additional data, and is retained in case that becomes useful in future.
 * @author dr6
 */
public class ModuleLayout {
    private Map<Module, Point> positions;

    /** Constructs a layout object holding the given map of positions. */
    public ModuleLayout(Map<Module, Point> positions) {
        this.positions = positions;
    }

    /**
     * Gets the position of the given module.
     * @param module the module to location
     * @return the position of the module, if specified; otherwise null
     */
    public Point get(Module module) {
        return this.positions.get(module);
    }

    /** Gets the entries of the map of module locations. */
    public Set<Entry<Module, Point>> entries() {
        return positions.entrySet();
    }

    /**
     * Gets the position of {@link ModulePair#getFrom from} in the given {@code ModulePair}
     * @param pair the pair to get the {@code start} position for
     * @return the position of the {@code start} module, if specified; otherwise null
     */
    public Point getFrom(ModulePair pair) {
        return get(pair.getFrom());
    }

    /**
     * Gets the position of {@link ModulePair#getTo to} in the given {@code ModulePair}
     * @param pair the pair to get the {@code end} position for
     * @return the position of the {@code end} module, if specified; otherwise null
     */
    public Point getTo(ModulePair pair) {
        return get(pair.getTo());
    }

    /** Removes the given module from the positions map, if present. */
    public void remove(Module module) {
        positions.remove(module);
    }

    /** Store the position for the given module. */
    public void put(Module module, Point pos) {
        positions.put(module, pos);
    }

    /** Delete positions for any modules that are not in the given set */
    public void retainModules(Set<Module> modules) {
        positions.keySet().retainAll(modules);
    }

    /** Get the set of modules whose position is given in this layout */
    public Set<Module> modules() {
        return positions.keySet();
    }
}
