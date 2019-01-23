package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.graph.ModuleLayout;
import uk.ac.sanger.aker.catalogue.graph.ModuleLayoutUtil;
import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Module;

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
        MAKE_DEFAULT("ENTER", "make route default"),
        MAKE_NOT_DEFAULT("ENTER", "make route not default"),
        ADD_MODULE("Double click", "Add module"),
        SELECT("Left click", "Select module or route"),
        ADD_ROUTE("Right drag", "Add new route"),
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

    private CatalogueFrame frame;
    private ProcessPanel processPanel;
    private AkerProcess process;
    private ModuleGraph graph;
    private ModuleMouseControl mouseControl;
    private Dimension prefDim;

    public ProcessModulePanel(CatalogueFrame frame, AkerProcess process, ProcessPanel processPanel) {
        this.frame = frame;
        this.processPanel = processPanel;
        this.process = process;
        setBackground(Color.white);
        mouseControl = new ModuleMouseControl(this);
        addMouseListener(mouseControl);
        addMouseMotionListener(mouseControl);
        new ModuleKeyControl(this);
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
    }

    private ModuleLayout getModuleLayout() {
        ModuleLayout layout = frame.getModuleLayout(process);
        if (layout==null) {
            layout = ModuleLayoutUtil.layOut(process.getModulePairs(), 160, 100);
            frame.saveModuleLayout(process, layout);
        }
        return layout;
    }

    public void updateDimension() {
        ModuleLayout layout = getModuleLayout();
        Point start = layout.get(Module.START);
        int minx = start.x;
        int miny = start.y;
        int maxx = start.x;
        int maxy = start.y;
        for (Map.Entry<Module, Point> entry : getModuleLayout().entries()) {
            Point pos = entry.getValue();
            int x = pos.x;
            int y = pos.y;
            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
        }
        prefDim = new Dimension(maxx-minx + 130, maxy-miny + 130);
    }

    @Override
    public Dimension getPreferredSize() {
        if (prefDim==null) {
            updateDimension();
        }
        return prefDim;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics g2 = g.create();
        try {
            showHints(g);
            int x0 = getWidth()/2;
            int y0 = ModuleGraph.MODULE_HEIGHT;
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
        int y = fm.getMaxAscent();
        int h = y + fm.getMaxDescent();
        for (Hint hint : hints) {
            g.drawString(hint.toString(), 2, y);
            y += h;
        }
    }

    private Set<Hint> getHints() {
        ModuleGraph graph = getGraph();
        if (graph.anySelected() && hasFocus()) {
            return EnumSet.of(Hint.DEL_MODULE);
        }
        if (graph.anyPairSelected() && hasFocus()) {
            if (graph.getSelectedPair().isDefaultPath()) {
                return EnumSet.of(Hint.DEL_ROUTE, Hint.MAKE_NOT_DEFAULT);
            }
            return EnumSet.of(Hint.DEL_ROUTE, Hint.MAKE_DEFAULT);
        }
        Module module = getModuleToAdd();
        if (module!=null && !graph.hasModule(module)) {
            return EnumSet.of(Hint.ADD_MODULE, Hint.SELECT, Hint.ADD_ROUTE);
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
}
