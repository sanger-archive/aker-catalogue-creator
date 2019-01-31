package uk.ac.sanger.aker.catalogue.graph;

import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility to calculate a suitable {@link ModuleLayout layout} for modules given a collection of paths between them.
 * @author dr6
 */
public class ModuleLayoutUtil {
    private static final int XSEP = 160, YSEP = 80;

    private static List<List<Module>> getRows(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs) {
        TopologicalSorter<Module> sorter = new TopologicalSorter<>(modules);
        sorter.setRelations(pairs, ModulePair::getFrom, ModulePair::getTo);
        Map<Module, Set<Module>> preceders = sorter.getPreceders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
        modules = sorter.sort();
        List<List<Module>> rows = new ArrayList<>();
        List<Module> current = new ArrayList<>();
        rows.add(current);
        for (Module module : modules) {
            final Set<Module> precs = preceders.get(module);
            if (module==Module.END || current.size()==1 && current.contains(Module.START)
                    || current.stream().anyMatch(precs::contains)) {
                current = new ArrayList<>();
                rows.add(current);
            }
            current.add(module);
        }
        return rows;
    }

    private static ModuleLayout layOut(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs) {
        List<List<Module>> rows = getRows(modules, pairs);
        // Start is at (0,0).
        // Rows are below, to the left and right of zero, so (0,0) should be centre-top
        Map<Module, Point> positions = new HashMap<>(modules.size());
        int y = 0;
        int[] xoffsets = new int[rows.size()];
        for (int i = 1; i < rows.size()-1; ++i) {
            int size = rows.get(i).size();
            if (size==rows.get(i-1).size() && size==rows.get(i+1).size()) {
                if (xoffsets[i-1]!=0) {
                    xoffsets[i] = -xoffsets[i-1];
                } else {
                    xoffsets[i] = XSEP /2;
                }
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            List<Module> row = rows.get(i);
            int x = xoffsets[i] - XSEP * (row.size() - 1) / 2;
            for (Module mod : row) {
                positions.put(mod, new Point(x, y));
                x += XSEP;
            }
            y += YSEP;
        }
        return new ModuleLayout(positions);
    }

    /**
     * Generates a layout from the described paths.
     * The modules are ordered using {@link TopologicalSorter},
     * then arrayed into rows based on keeping path-end modules below their respective path-start module.
     * The start module is positioned at {@code (0,0)}, and subsequent rows are
     * positioned below, a fixed distance apart, centred around x=0.
     * In cases where there are three consecutive rows of the same size, they will be offset in x
     * to reduce the chance of paths hiding each other.
     * @param pairs the paths between modules
     * @return the layout specifying the positions of the modules
     */
    public static ModuleLayout layOut(Collection<? extends ModulePair> pairs) {
        List<Module> modules = pairs.stream()
                .map(ModulePair::getFrom)
                .distinct()
                .collect(Collectors.toList());
        modules.add(Module.END);
        if (!modules.contains(Module.START)) {
            modules.add(0, Module.START);
        }
        return layOut(modules, pairs);
    }
}
