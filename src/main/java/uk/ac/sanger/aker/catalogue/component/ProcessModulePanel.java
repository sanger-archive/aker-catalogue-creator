package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.sorting.ModuleLayoutUtil;

import javax.swing.JPanel;
import java.awt.*;
import java.util.Map;

/**
 * @author dr6
 */
public class ProcessModulePanel extends JPanel {
    private CatalogueFrame frame;

    private AkerProcess process;
    private ModuleGraph graph;
    private ModuleMouseControl mouseControl;
    private Dimension prefDim;

    public ProcessModulePanel(CatalogueFrame frame, AkerProcess process) {
        this.frame = frame;
        this.process = process;
        setBackground(Color.white);
        mouseControl = new ModuleMouseControl(this);
        addMouseListener(mouseControl);
        addMouseMotionListener(mouseControl);
        new ModuleKeyControl(this);
        graph = new ModuleGraph(getModuleLayout(), process.getModulePairs());
        setFocusable(true);
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
            int x0 = getWidth()/2;
            int y0 = ModuleGraph.MODULE_HEIGHT;
            mouseControl.setOrigin(x0, y0);
            g2.translate(x0, y0);
            graph.draw((Graphics2D) g2);
        } finally {
            g2.dispose();
        }
    }

    public ModuleGraph getGraph() {
        return this.graph;
    }
}
