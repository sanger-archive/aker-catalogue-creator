package uk.ac.sanger.aker.catalogue.sorting;

import uk.ac.sanger.aker.catalogue.component.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dr6
 */
public class ModuleLayoutUtil {

    public static List<List<Module>> getRows(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs) {
        Stream<? extends ModulePair> pairStream = pairs.stream()
                .filter(pair -> pair.getFrom()!=null && pair.getTo()!=null);

        TopologicalSorter<Module> sorter = new TopologicalSorter<>(modules);
        sorter.setRelations(pairStream, ModulePair::getFrom, ModulePair::getTo);
        Map<Module, Set<Module>> preceders = sorter.getPreceders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
        modules = sorter.sort();
        List<List<Module>> rows = new ArrayList<>();
        List<Module> current = new ArrayList<>();
        rows.add(current);
        for (Module module : modules) {
            final Set<Module> precs = preceders.get(module);
            if (current.stream().anyMatch(precs::contains)) {
                current = new ArrayList<>();
                rows.add(current);
            }
            current.add(module);
        }
        return rows;
    }

    public static ModuleLayout layOut(Collection<? extends Module> modules, Collection<? extends ModulePair> pairs,
                                      int dx, int dy) {
        List<List<Module>> rows = getRows(modules, pairs);
        // Start is at (0,0).
        // Rows are below, to the left and right of zero, so (0,0) should be centre-top
        Map<Module, Point> positions = new HashMap<>(modules.size());
        int y = dy;
        int[] xoffsets = new int[rows.size()];
        for (int i = 0; i < rows.size(); ++i) {
            int prev = (i==0 ? 1 : rows.get(i).size());
            int next = (i==rows.size()-1 ? 1 : rows.get(rows.size()-1).size());
            if (prev==next && prev==rows.get(i).size()) {
                if (i > 0 && xoffsets[i-1]!=0) {
                    xoffsets[i] = -xoffsets[i-1];
                } else {
                    xoffsets[i] = dx/2;
                }
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            List<Module> row = rows.get(i);
            int x = xoffsets[i] -dx * (row.size() - 1) / 2;
            for (Module mod : row) {
                positions.put(mod, new Point(x, y));
                x += dx;
            }
            y += dy;
        }
        return new ModuleLayout(positions, new Point(0, 0), new Point(0, y));
    }

    public static ModuleLayout layOut(Collection<? extends ModulePair> pairs, int dx, int dy) {
        List<Module> modules = pairs.stream()
                .map(ModulePair::getTo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return layOut(modules, pairs, dx, dy);
    }
}
