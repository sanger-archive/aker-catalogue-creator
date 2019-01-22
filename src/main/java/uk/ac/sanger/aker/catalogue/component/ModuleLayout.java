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
    private Point startPoint;
    private Point endPoint;

    public ModuleLayout(Map<Module, Point> positions, Point startPoint, Point endPoint) {
        this.positions = positions;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Map<Module, Point> getPositions() {
        return this.positions;
    }

    public Point getStartPoint() {
        return this.startPoint;
    }

    public Point getEndPoint() {
        return this.endPoint;
    }

    public Point get(Module module) {
        return this.positions.get(module);
    }

    public Set<Entry<Module, Point>> entries() {
        return positions.entrySet();
    }

    public Point getFrom(ModulePair pair) {
        Module from = pair.getFrom();
        if (from==null) {
            return getStartPoint();
        }
        return get(from);
    }

    public Point getTo(ModulePair pair) {
        Module to = pair.getTo();
        if (to==null) {
            return getEndPoint();
        }
        return get(to);
    }
}
