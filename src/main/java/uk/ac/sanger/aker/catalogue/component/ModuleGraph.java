package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * This class draws the module graph.
 * Up to one module or path can be selected at a time (not both).
 * A projected path may be in progress from the selected module
 * to some point in the graph, while the user is part way through adding a new path.
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
    private static final Color selectionColour = Color.magenta;
    /** The size that a module is drawn */
    public static final int MODULE_WIDTH = 120, MODULE_HEIGHT = 40;

    private List<ModulePair> pairs;
    private ModuleLayout layout;
    private Module selected;
    private Point projectedTarget;
    private ModulePair selectedPair;

    /**
     * Creates a {@code ModuleGraph} to draw the given layout (module positions) and paths between them.
     * @param layout the positions of the modules
     * @param pairs the paths between modules
     */
    public ModuleGraph(ModuleLayout layout, List<ModulePair> pairs) {
        this.layout = layout;
        this.pairs = pairs;
    }

    /**
     * Draws a module at the given position.
     * @param g the graphics context used to draw
     * @param text the text of the module
     * @param pos the position of the centre of the module
     * @param fontMetrics the metrics of the font, used to find the width of the string
     * @param textY the offset of the text from the centre of the module
     * @param fillColour the colour to fill the module
     */
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

    /**
     * Sets the layout (module positions) for future drawing
     */
    public void setLayout(ModuleLayout layout) {
        this.layout = layout;
    }

    /**
     * Gets the appropriate fill colour for the given module.
     * The special START and END modules get the {@link #endFill} colour;
     * others get {@link #moduleFill}
     */
    private static Color moduleColour(Module module) {
        if (module==Module.START || module==Module.END) {
            return endFill;
        }
        return moduleFill;
    }

    /**
     * Draws the graph.
     * First all the modules.
     * Then the indicator for the selected module (if any).
     * Then the paths.
     * @param g the graphics context
     */
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

    /**
     * Draws the indicator for the selected module.
     * The indicator is a box of colour {@link #selectionColour}.
     * @param g the graphics context
     * @param selected the selected module
     */
    private void drawSelected(Graphics2D g, Module selected) {
        Point pos = layout.get(selected);
        int x = pos.x - MODULE_WIDTH/2;
        int y = pos.y - MODULE_HEIGHT/2;
        g.setColor(selectionColour);
        g.drawRect(x-1, y-1, MODULE_WIDTH+2, MODULE_HEIGHT+2);
        g.drawRect(x-2, y-2, MODULE_WIDTH+4, MODULE_HEIGHT+4);
    }

    /**
     * Draws the paths between modules.
     * Also draws the projected path (when the user is dragging a new path),
     * and indicators for the selected path (if any).
     * The paths are thick, transparent arrows.
     * The default path is coloured {@link #defaultPathColour}; others are {@link #pathColour}.
     * @param g the graphics context
     */
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

    /**
     * Draws an arrow from the start to the end.
     * The arrow is truncated a little way from the start and usually a little way from the end.
     * @param g the graphics context
     * @param start the start point
     * @param end the end point
     * @param toPoint true if the arrow should go all the way to the end point, instead
     *                of ending just before it.
     */
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

    /**
     * Draws a provisional arrow from the start module towards the projected target.
     * The arrow will be a darker colour if the path is strong (i.e. if the projected path
     * is a valid one that can be added to the graph).
     * @param g the graphics context
     * @see #isPathStrong
     */
    private void drawProjectedPath(Graphics2D g) {
        g.setColor(isPathStrong() ? projectedPathStrongColour : projectedPathWeakColour);
        drawArrow(g, position(selected), projectedTarget, true);
    }

    /**
     * Indicates the selected path.
     * The indicator is a box around the path.
     * @param g the graphics context
     */
    private void drawSelectedPath(Graphics2D g) {
        Point start = position(selectedPair.getFrom());
        Point end = position(selectedPair.getTo());
        int dx = end.x-start.x;
        int dy = end.y-start.y;
        double theta = Math.atan2(dy, dx);
        int distance = (int) Math.hypot(dx, dy);
        g.rotate(theta, end.x, end.y);
        g.setColor(selectionColour);
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1, new float[] { 10 }, 5));
        g.drawRect(end.x - distance + 15, end.y - 5, distance - 30, 10);
        g.rotate(-theta, end.x, end.y);
    }

    /**
     * Is there a projected path? The projected path is one that the user is mid-way through adding to the graph.
     */
    public boolean hasProjectedPath() {
        return (selected!=null && projectedTarget!=null);
    }

    /**
     * If the current projected path creates a valid new path, add it. Otherwise, just drop it.
     * @param defaultPath the value of {@link ModulePair#isDefaultPath defaultPath} for the new path
     */
    public void releaseProjectedPath(boolean defaultPath) {
        if (isPathStrong()) {
            Module target = moduleAt(projectedTarget.x, projectedTarget.y);
            selectedPair = new ModulePair(selected, target, defaultPath);
            pairs.add(selectedPair);
            selected = null;
        }
        projectedTarget = null;
    }

    /** Select the specified module. Deselect any other selected module or path. Drop any projected path. */
    public void select(Module mw) {
        this.selected = mw;
        this.selectedPair = null;
        this.projectedTarget = null;
    }

    /** Is any module currently selected? */
    public boolean anySelected() {
        return (this.selected!=null);
    }

    /** Select the specified path. Deselect any other selected module or path. Drop any projected path. */
    public void selectPair(ModulePair pair) {
        this.selectedPair = pair;
        this.selected = null;
        this.projectedTarget = null;
    }

    /** Is any path currently selected? */
    public boolean anyPairSelected() {
        return (this.selectedPair!=null);
    }

    /**
     * The projected path is <i>strong</i> if it ends at a module (target), and a valid new path can be added
     * from the current selected module (the source) and the target.
     * The target must be below the source in the graph.
     * The START module cannot be a target, and the END module cannot be a source.
     * If source and target already have a path in the list of {@link ModulePair}s, then the
     * new path is not valid.
     */
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

    /**
     * Gets the position (if any) of the centre of the given module in the graph.
     * @param module the module to get the position of
     * @return the position, or null if no position is held for the given module
     */
    public Point position(Module module) {
        return layout.get(module);
    }

    /**
     * Moves the selected module (if any) by the given amount.
     * @param dx amount to move in x
     * @param dy amount to move in y
     */
    public void moveSelected(int dx, int dy) {
        if (selected==null) {
            return;
        }
        Point pos = position(selected);
        move(selected, pos.x + dx, pos.y + dy);
    }

    /**
     * Deletes the selected module (if any) from the graph.
     * The START and END modules cannot be deleted.
     * Any paths linked to the deleted module will also be deleted.
     */
    public void deleteSelected() {
        if (selected==null || selected.isEndpoint()) {
            return;
        }
        pairs.removeIf(pair -> pair.getTo()==selected || pair.getFrom()==selected);
        layout.remove(selected);
        selected = null;
        projectedTarget = null;
    }

    /**
     * Deletes the selected path (if any) from the graph.
     */
    public void deleteSelectedPair() {
        if (selectedPair!=null) {
            pairs.remove(selectedPair);
            selectedPair = null;
        }
    }

    /**
     * Moves a module to a new position.
     * The module must already be positioned in the graph.
     * All non-START modules must be below the START module.
     * All non-END modules must be above the END module.
     * Any module must be above any module that it has a path to, and below any module that has a path to it.
     * The new y position that the module is given is pushed into the acceptable range according to the constraints
     * described.
     * @param module the module to move
     * @param newx the new x position
     * @param newy the new y position
     */
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

    /**
     * Set the projected path from the selected module to the specified point.
     */
    public void projectPath(int tx, int ty) {
        selectedPair = null;
        if (selected!=null) {
            projectedTarget = new Point(tx, ty);
        }
    }

    /**
     * Is the given x,y within the bounds of a module centred at the given pos?
     */
    private static boolean inModuleRect(int x, int y, Point pos) {
        x += MODULE_WIDTH/2 - pos.x;
        y += MODULE_HEIGHT/2 - pos.y;
        return (x >= 0 && y >= 0 && x < MODULE_WIDTH && y < MODULE_HEIGHT);
    }

    /**
     * Gets the module containing the specified position.
     * Returns the first matching module found, or null if none is found.
     * @param x the x of the position
     * @param y the y of the position
     * @return a module containing the specified position, or null if none is found
     */
    public Module moduleAt(int x, int y) {
        for (Map.Entry<Module, Point> entry : layout.entries()) {
            if (inModuleRect(x, y, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Gets the path at the specified position.
     * The indicated position must be within a short distance some point on the line to match.
     * If multiple paths match, the closest is returned.
     * If none match, null is returned.
     * @param x x of the position
     * @param y the y of the position
     * @return the closest matched path
     */
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

    /**
     * Helper: the distance between the given x,y and any point on a specified line.
     * If the x,y is beyond the line's endpoints, {@link Double#MAX_VALUE} is returned.
     * @param x the x of the position
     * @param y the y of the position
     * @param source the start of the line
     * @param target the end of the line
     * @return the distance from the x,y to the nearest point on the line; or {@code Double.MAX_VALUE}
     * if the x,y is considered out of bounds of the line
     */
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

    /** Is v between 0 and dv, within the given margin? */
    private static boolean inRangeOf(int v, int dv, int margin) {
        return (dv < 0 ? (v >= dv-margin && v < margin) : (v <= dv+margin && v > -margin));
    }

    /**
     * Adds a module at the given position.
     * If the module already has a position in this graph, select it and return true.
     * Otherwise, adjust the y-position to make sure it is between the START and END modules on the graph.
     * If that is impossible, return false.
     * Otherwise, add the module at the given position and return true.
     * @param module the module to add
     * @param x the x to add the module at
     * @param y the y to add the module at
     * @return true if the specified module is now in the graph; otherwise, false
     */
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

    /**
     * Is the given module in the graph?
     * @param module the module to check
     * @return true if the given module has a position in the graph; otherwise false
     */
    public boolean hasModule(Module module) {
        return (position(module)!=null);
    }
}
