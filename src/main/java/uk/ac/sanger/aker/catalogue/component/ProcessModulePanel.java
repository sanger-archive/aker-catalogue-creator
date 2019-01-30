package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.graph.ModuleLayoutUtil;
import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Module;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;

/**
 * @author dr6
 */
public class ProcessModulePanel extends JPanel {
    enum Hint {
        DEL_MODULE("DEL", "delete selected module"),
        DEL_ROUTE("DEL", "delete selected route"),
        ADD_MODULE("Double click", "Add module"),
        SELECT("Left click", "Select module or route"),
        ADD_ROUTE("Right drag down from module", "Add new route"),
        TOGGLE_DEFAULT("Shift-click", "toggle route default"),
        DRAG_MODULE("Drag", "move module"),
        ;

        private final String command, desc;

        Hint(String command, String desc) {
            this.command = command;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return command + "—" + desc;
        }
    }

    private CatalogueApp app;
    private ProcessPanel processPanel;
    private AkerProcess process;
    private ModuleGraph graph;
    private ModuleMouseControl mouseControl;
    private Rectangle graphBounds;

    public ProcessModulePanel(CatalogueApp app, AkerProcess process, ProcessPanel processPanel) {
        this.app = app;
        this.processPanel = processPanel;
        this.process = process;
        setBackground(Color.white);
        mouseControl = new ModuleMouseControl(this);
        addMouseListener(mouseControl);
        addMouseMotionListener(mouseControl);
        KeyShortcuts.DELETE.register(this, e -> fireDelete());
        graph = new ModuleGraph(getModuleLayout(), process.getModulePairs());
        setFocusable(true);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        setBorder(BorderFactory.createLineBorder(Color.lightGray));
    }

    private ModuleLayout getModuleLayout() {
        Map<AkerProcess, ModuleLayout> layoutCache = app.getLayoutCache();
        ModuleLayout layout = layoutCache.get(process);
        if (layout==null) {
            layout = ModuleLayoutUtil.layOut(process.getModulePairs());
            layoutCache.put(process, layout);
        }
        return layout;
    }

    public void autoLayout() {
        ModuleLayout layout = ModuleLayoutUtil.layOut(process.getModulePairs());
        app.getLayoutCache().put(process, layout);
        graph.setLayout(layout);
        updateBounds();
        repaint();
    }

    public void updateBounds() {
        ModuleLayout layout = getModuleLayout();
        Point start = layout.get(Module.START);
        int minx = start.x;
        int miny = start.y;
        int maxx = start.x;
        int maxy = start.y;
        for (Map.Entry<Module, Point> entry : getModuleLayout().entries()) {
            Point pos = entry.getValue();
            minx = Math.min(pos.x, minx);
            maxx = Math.max(pos.x, maxx);
            miny = Math.min(pos.y, miny);
            maxy = Math.max(pos.y, maxy);
        }
        int centreX = (minx + maxx)/2;
        int centreY = (miny + maxy)/2;
        int width = Math.max(300, maxx - minx + ModuleGraph.MODULE_WIDTH + 80);
        int height = Math.max(300, maxy - miny + ModuleGraph.MODULE_HEIGHT + 80);
        graphBounds = new Rectangle(centreX - width/2, centreY - height/2, width, height);
    }

    @Override
    public Dimension getPreferredSize() {
        if (graphBounds==null) {
            updateBounds();
        }
        return graphBounds.getSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics g2 = g.create();
        try {
            showHints(g);
            int x0 = (getWidth() - graphBounds.width)/2 - graphBounds.x;
            int y0 = (getHeight() - graphBounds.height)/2 - graphBounds.y + 8;
            mouseControl.setOrigin(x0, y0);
            g2.translate(x0, y0);
            graph.draw((Graphics2D) g2);
        } finally {
            g2.dispose();
        }
    }

    private void showHints(Graphics g) {
        Set<Hint> hints = getHints();
        g.setColor(Color.gray);
        FontMetrics fm = g.getFontMetrics();
        int y = fm.getMaxAscent() + 8;
        int h = y + fm.getMaxDescent() - 8;
        for (Hint hint : hints) {
            g.drawString(hint.toString(), 10, y);
            y += h;
        }
    }

    private Set<Hint> getHints() {
        ModuleGraph graph = getGraph();
        if (graph.anySelected() && hasFocus()) {
            return EnumSet.of(Hint.DEL_MODULE, Hint.DRAG_MODULE);
        }
        if (graph.anyPairSelected() && hasFocus()) {
            return EnumSet.of(Hint.DEL_ROUTE, Hint.TOGGLE_DEFAULT);
        }
        Module module = getModuleToAdd();
        if (module!=null && !graph.hasModule(module)) {
            return EnumSet.of(Hint.ADD_MODULE, Hint.SELECT);
        }
        return EnumSet.of(Hint.SELECT, Hint.ADD_ROUTE);
    }

    public ModuleGraph getGraph() {
        return this.graph;
    }

    public Module getModuleToAdd() {
        return processPanel.getModuleToAdd();
    }

    public void clearModuleToAdd() {
        processPanel.clearModuleToAdd();
    }

    private void fireDelete() {
        ModuleGraph graph = getGraph();
        if (graph.anySelected()) {
            graph.deleteSelected();
            repaint();
        } else if (graph.anyPairSelected()) {
            graph.deleteSelectedPair();
            repaint();
        }
    }
}
