package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author dr6
 */
public class ModuleGraph {
    private static final Color endFill = new Color(128,128,255);
    private static final Color moduleFill = Color.cyan;
    private static final Color moduleOutline = Color.black;
    private static final Color moduleTextColour = Color.black;
    private static final Color projectedPathWeakColour = new Color(0x80ffaaaa, true);
    private static final Color projectedPathStrongColour = new Color(0x80800080, true);
    private static final Color pathColour = new Color(0x80808080, true);
    private static final Color defaultPathColour = new Color(0x8000c000, true);
    public static final int MODULE_WIDTH = 120, MODULE_HEIGHT = 40;

    private List<ModulePair> pairs;
    private ModuleLayout layout;
    private Module selected;
    private Point projectedTarget;
    private ModulePair selectedPair;

    public ModuleGraph(ModuleLayout layout, List<ModulePair> pairs) {
        this.layout = layout;
        this.pairs = pairs;
    }

    private void drawModule(Graphics2D g, String text, Point pos, FontMetrics fontMetrics, int textY, Color fillColour) {
        int x = pos.x - MODULE_WIDTH/2;
        int y = pos.y - MODULE_HEIGHT/2;
        g.setColor(fillColour);
        g.fillRect(x, y, MODULE_WIDTH, MODULE_HEIGHT);
        g.setColor(moduleOutline);
        g.drawRect(x, y, MODULE_WIDTH, MODULE_HEIGHT);
        Shape oldClip = g.getClip();
        g.clipRect(x, y, MODULE_WIDTH, MODULE_HEIGHT);
        g.setColor(moduleTextColour);
        int wid = fontMetrics.stringWidth(text);
        x += Math.max(0, (MODULE_WIDTH -wid)/2);
        y += textY;
        g.drawString(text, x, y);
        g.setClip(oldClip);
    }

    private static Color moduleColour(Module module) {
        if (module==Module.START || module==Module.END) {
            return endFill;
        }
        return moduleFill;
    }

    public void draw(Graphics2D g) {
        FontMetrics fontMetrics = g.getFontMetrics();
        int textY = (MODULE_HEIGHT - fontMetrics.getHeight())/2 + fontMetrics.getAscent();

        for (Map.Entry<Module, Point> e : layout.entries()) {
            Module module = e.getKey();
            Point pos = e.getValue();
            drawModule(g, module.getName(), pos, fontMetrics, textY, moduleColour(module));
        }

        if (selected!=null) {
            drawSelected(g, selected);
        }
        drawPaths(g);
    }

    private void drawSelected(Graphics2D g, Module selected) {
        Point pos = layout.get(selected);
        int x = pos.x - MODULE_WIDTH/2;
        int y = pos.y - MODULE_HEIGHT/2;
        g.setColor(Color.blue);
        g.drawRect(x-1, y-1, MODULE_WIDTH+2, MODULE_HEIGHT+2);
        g.drawRect(x-2, y-2, MODULE_WIDTH+4, MODULE_HEIGHT+4);
    }

    public void drawPaths(Graphics2D g) {
        g = (Graphics2D) g.create();
        try {
            g.setStroke(new BasicStroke(4));
            for (ModulePair pair : pairs) {
                Point start = layout.getFrom(pair);
                Point end = layout.getTo(pair);
                g.setColor(pair.isDefaultPath() ? defaultPathColour : pathColour);
                drawArrow(g, start, end, false);
            }
            if (selected!=null && projectedTarget!=null) {
                drawProjectedPath(g);
            }
            if (selectedPair!=null) {
                drawSelectedPath(g);
            }
        } finally {
            g.dispose();
        }
    }

    private void drawArrow(Graphics2D g, Point start, Point end, boolean toPoint) {
        int dx = end.x-start.x;
        int dy = end.y-start.y;
        double theta = Math.atan2(dy, dx);
        g.rotate(theta, end.x, end.y);
        int distance = (int) Math.hypot(dx, dy);
        int margin1 = MODULE_HEIGHT/2;
        int margin2 = (toPoint ? 0 : margin1);
        g.drawLine(end.x - distance + margin1, end.y, end.x-margin2, end.y);
        g.drawLine(end.x - margin2 - 10, end.y - 5, end.x - margin2, end.y);
        g.drawLine(end.x - margin2 - 10, end.y + 5, end.x - margin2, end.y);
        g.rotate(-theta, end.x, end.y);
    }

    private void drawProjectedPath(Graphics2D g) {
        g.setColor(isPathStrong() ? projectedPathStrongColour : projectedPathWeakColour);
        drawArrow(g, position(selected), projectedTarget, true);
    }

    private void drawSelectedPath(Graphics2D g) {
        Point start = position(selectedPair.getFrom());
        Point end = position(selectedPair.getTo());
        int dx = end.x-start.x;
        int dy = end.y-start.y;
        double theta = Math.atan2(dy, dx);
        int distance = (int) Math.hypot(dx, dy);
        g.rotate(theta, end.x, end.y);
        g.setColor(Color.blue);
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1, new float[] { 10 }, 5));
        g.drawRect(end.x - distance + 15, end.y - 5, distance - 30, 10);
        g.rotate(-theta, end.x, end.y);
    }

    public boolean hasProjectedPath() {
        return (selected!=null && projectedTarget!=null);
    }

    public void releaseProjectedPath(boolean defaultPath) {
        if (isPathStrong()) {
            Module target = moduleAt(projectedTarget.x, projectedTarget.y);
            selectedPair = new ModulePair(selected, target, defaultPath);
            pairs.add(selectedPair);
            selected = null;
        }
        projectedTarget = null;
    }

    public void select(Module mw) {
        this.selected = mw;
        this.selectedPair = null;
        this.projectedTarget = null;
    }

    public boolean anySelected() {
        return (this.selected!=null);
    }

    public void selectPair(ModulePair pair) {
        this.selectedPair = pair;
        this.selected = null;
        this.projectedTarget = null;
    }

    public boolean anyPairSelected() {
        return (this.selectedPair!=null);
    }

    private boolean isPathStrong() {
        if (projectedTarget==null) {
            return false;
        }
        if (selected==Module.END) {
            return false;
        }
        Module target = moduleAt(projectedTarget.x, projectedTarget.y);
        if (target==null || target==selected || target==Module.START) {
            return false;
        }
        if (position(target).y <= position(selected).y) {
            return false;
        }
        return pairs.stream().noneMatch(pair -> pair.getFrom()==selected && pair.getTo()==target);
    }

    public Point position(Module module) {
        return layout.get(module);
    }

    public void moveSelected(int dx, int dy) {
        if (selected==null) {
            return;
        }
        Point pos = position(selected);
        move(selected, pos.x + dx, pos.y + dy);
    }

    public void deleteSelected() {
        if (selected==null || selected.isEndpoint()) {
            return;
        }
        pairs.removeIf(pair -> pair.getTo()==selected || pair.getFrom()==selected);
        layout.remove(selected);
        selected = null;
        projectedTarget = null;
    }

    public void deleteSelectedPair() {
        if (selectedPair!=null) {
            pairs.remove(selectedPair);
            selectedPair = null;
        }
    }

    private void move(Module module, int newx, int newy) {
        Point pos = position(module);
        pos.x = newx;
        if (module==Module.START) {
            if (layout.entries().stream().anyMatch(e -> e.getKey()!=module && e.getValue().y <= newy)) {
                return;
            }
        } else if (module==Module.END) {
            if (layout.entries().stream().anyMatch(e -> e.getKey()!=module && e.getValue().y >= newy)) {
                return;
            }
        } else {
            if (position(Module.START).y >= newy || position(Module.END).y <= newy) {
                return;
            }
            if (pairs.stream().anyMatch(pair -> (pair.getTo()==module && position(pair.getFrom()).y >= newy
                    || (pair.getFrom()==module && position(pair.getTo()).y <= newy)))) {
                return;
            }
        }

        pos.y = newy;
    }

    public void projectPath(int tx, int ty) {
        selectedPair = null;
        if (selected!=null) {
            projectedTarget = new Point(tx, ty);
        }
    }

    private static boolean inModuleRect(int x, int y, Point pos) {
        x += MODULE_WIDTH/2 - pos.x;
        y += MODULE_HEIGHT/2 - pos.y;
        return (x >= 0 && y >= 0 && x < MODULE_WIDTH && y < MODULE_HEIGHT);
    }

    public Module moduleAt(int x, int y) {
        for (Map.Entry<Module, Point> entry : layout.entries()) {
            if (inModuleRect(x, y, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ModulePair pathAt(int x, int y) {
        ModulePair best = null;
        double bestDist = 6;
        for (ModulePair pair : pairs) {
            Point source = position(pair.getFrom());
            Point target = position(pair.getTo());
            double dist = distToLine(x, y, source, target);
            if (dist < bestDist) {
                best = pair;
                bestDist = dist;
            }
        }
        return best;
    }

    private static double distToLine(int x, int y, Point source, Point target) {
        x -= source.x;
        y -= source.y;
        int margin = 10;
        int dx = target.x - source.x;
        int dy = target.y - source.y;
        if (!(inRangeOf(x, dx, margin) && inRangeOf(y, dy, margin))) {
            return Double.MAX_VALUE;
        }
        double scale = ((double) (dx*x + dy*y)) / (dx*dx + dy*dy);
        return Math.hypot(scale*dx - x, scale*dy - y);
    }

    private static boolean inRangeOf(int v, int dv, int margin) {
        return (dv < 0 ? (v >= dv-margin && v < margin) : (v <= dv+margin && v > -margin));
    }


    public boolean addModule(Module module, int x, int y) {
        if (position(module)!=null) {
            select(module);
            return true;
        }
        int miny = position(Module.START).y;
        int maxy = position(Module.END).y;
        if (miny + 1 >= maxy - 1) {
            return false;
        }
        y = Math.max(miny, Math.min(maxy, y));
        layout.put(module, new Point(x,y));
        selected = module;
        return true;
    }

    public boolean hasModule(Module module) {
        return (position(module)!=null);
    }
}
