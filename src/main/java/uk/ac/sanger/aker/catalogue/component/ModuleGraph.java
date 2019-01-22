package uk.ac.sanger.aker.catalogue.component;

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
    private static final Color pathColour = new Color(0x80c8c800, true);
    private static final Color defaultPathColour = new Color(0x8000ff00, true);
    public static final int MODULE_WIDTH = 120, MODULE_HEIGHT = 40;

    private List<ModulePair> pairs;
    private ModuleLayout layout;
    private ModuleWrapper selected;

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

    public void draw(Graphics2D g) {
        FontMetrics fontMetrics = g.getFontMetrics();
        int textY = (MODULE_HEIGHT - fontMetrics.getHeight())/2 + fontMetrics.getAscent();

        drawModule(g, "START", layout.getStartPoint(), fontMetrics, textY, endFill);
        for (Map.Entry<Module, Point> e : layout.entries()) {
            Module module = e.getKey();
            Point pos = e.getValue();
            drawModule(g, module.getName(), pos, fontMetrics, textY, moduleFill);
        }
        drawModule(g, "END", layout.getEndPoint(), fontMetrics, textY, endFill);

        if (selected!=null) {
            drawSelected(g, selected);
        }
        drawPaths(g);
    }

    private void drawSelected(Graphics2D g, ModuleWrapper selected) {
        Point pos;
        if (selected.module!=null) {
            pos = layout.get(selected.module);
        } else if (selected.start) {
            pos = layout.getStartPoint();
        } else if (selected.end) {
            pos = layout.getEndPoint();
        } else {
            return;
        }
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
                int dx = end.x-start.x;
                int dy = end.y-start.y;
                double theta = Math.atan2(dy, dx);
                g.rotate(theta, end.x, end.y);
                int distance = (int) Math.hypot(dx, dy);
                int margin = MODULE_HEIGHT/2;
                distance -= 2*margin;
                g.drawLine(end.x - distance - margin, end.y, end.x-margin, end.y);
                g.drawLine(end.x - margin - 10, end.y - 5, end.x - margin, end.y);
                g.drawLine(end.x - margin - 10, end.y + 5, end.x - margin, end.y);
                g.rotate(-theta, end.x, end.y);
            }
        } finally {
            g.dispose();
        }
    }

    public void select(ModuleWrapper mw) {
        this.selected = mw;
    }

    public ModuleWrapper getSelected() {
        return selected;
    }

    public Point position(ModuleWrapper mw) {
        if (mw.module!=null) {
            return layout.get(mw.module);
        }
        if (mw.start) {
            return layout.getStartPoint();
        }
        if (mw.end) {
            return layout.getEndPoint();
        }
        return null;
    }

    public void moveSelected(int dx, int dy) {
        if (selected==null) {
            return;
        }
        Point pos = position(selected);
        move(selected, pos.x + dx, pos.y + dy);
    }

    private void move(ModuleWrapper wr, int newx, int newy) {
        Point pos = position(selected);
        pos.x = newx;
        for (ModulePair pair : pairs) {
            if (!wr.end && pair.getFrom()==wr.module) {
                int y = layout.getTo(pair).y;
                if (y <= newy) {
                    return;
                }
            }
            if (!wr.start && pair.getTo()==wr.module) {
                int y = layout.getFrom(pair).y;
                if (y >= newy) {
                    return;
                }
            }
        }
        pos.y = newy;
    }

    private static boolean inModuleRect(int x, int y, Point pos) {
        x += MODULE_WIDTH/2 - pos.x;
        y += MODULE_HEIGHT/2 - pos.y;
        return (x >= 0 && y >= 0 && x < MODULE_WIDTH && y < MODULE_HEIGHT);
    }

    public ModuleWrapper moduleAt(int x, int y) {
        for (Map.Entry<Module, Point> entry : layout.entries()) {
            if (inModuleRect(x, y, entry.getValue())) {
                return new ModuleWrapper(entry.getKey());
            }
        }
        if (inModuleRect(x, y, layout.getStartPoint())) {
            return ModuleWrapper.start();
        }
        if (inModuleRect(x, y, layout.getEndPoint())) {
            return ModuleWrapper.end();
        }
        return null;
    }

    public static class ModuleWrapper {
        private Module module;
        private boolean start;
        private boolean end;

        public ModuleWrapper(Module module) {
            this.module = module;
        }

        public static ModuleWrapper start() {
            ModuleWrapper mw = new ModuleWrapper(null);
            mw.start = true;
            return mw;
        }

        public static ModuleWrapper end() {
            ModuleWrapper mw = new ModuleWrapper(null);
            mw.end = true;
            return mw;
        }

        @Override
        public String toString() {
            if (module!=null) {
                return String.format("ModuleWrapper(\"%s\")", module.getName());
            }
            if (start) {
                return "ModuleWrapper.start";
            }
            if (end) {
                return "ModuleWrapper.end";
            }
            return "ModuleWrapper(null)";
        }
    }
}
