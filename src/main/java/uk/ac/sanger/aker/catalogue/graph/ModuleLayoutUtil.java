package uk.ac.sanger.aker.catalogue.graph;

import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dr6
 */
public class ModuleLayoutUtil {
    private static final int XSEP = 160, YSEP = 80;

    public static List<List<Module>> getRows(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs) {
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

    public static ModuleLayout layOut(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs) {
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
