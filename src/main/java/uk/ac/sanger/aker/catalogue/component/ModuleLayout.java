package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.Point;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author dr6
 */
public class ModuleLayout {
    private Map<Module, Point> positions;

    public ModuleLayout(Map<Module, Point> positions) {
        this.positions = positions;
    }

    public Point get(Module module) {
        return this.positions.get(module);
    }

    public Set<Entry<Module, Point>> entries() {
        return positions.entrySet();
    }

    public Point getFrom(ModulePair pair) {
        return get(pair.getFrom());
    }

    public Point getTo(ModulePair pair) {
        return get(pair.getTo());
    }
}
