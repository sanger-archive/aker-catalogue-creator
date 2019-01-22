package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.component.ModuleGraph.ModuleWrapper;
import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.sorting.ModuleLayoutUtil;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

/**
 * @author dr6
 */
public class ProcessModulePanel extends JPanel {
    private CatalogueFrame frame;

    private AkerProcess process;
    private ModuleGraph graph;
    private Dimension prefDim;
    private int mouseX, mouseY;
    private int x0, y0;
    private boolean dragged;
    private boolean addingPath;

    public ProcessModulePanel(CatalogueFrame frame, AkerProcess process) {
        this.frame = frame;
        this.process = process;
        setBackground(Color.white);
        MouseAdapter ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragged = false;
                addingPath = false;
                if (graph==null) {
                    return;
                }
                mouseX = e.getX() - x0;
                mouseY = e.getY() - y0;
                ModuleWrapper wr = graph.moduleAt(mouseX, mouseY);
                boolean left = SwingUtilities.isLeftMouseButton(e);
                boolean right = SwingUtilities.isRightMouseButton(e);
                if (left && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                    left = false;
                    right = true;
                }
                if (left) {
                    graph.select(wr);
                    repaint();
                } else if (right) {

                    // do something with the path
                    // TODO
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (graph==null || graph.getSelected()==null) {
                    return;
                }
                dragged = true;
                int x = e.getX()-x0;
                int y = e.getY()-y0;
                graph.moveSelected(x-mouseX, y-mouseY);
                mouseX = x;
                mouseY = y;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragged) {
                    updateDimension();
                    dragged = false;
                }
            }
        };
        addMouseListener(ml);
        addMouseMotionListener(ml);
    }

    private ModuleLayout getModuleLayout() {
        ModuleLayout layout = frame.getModuleLayout(process);
        if (layout==null) {
            layout = ModuleLayoutUtil.layOut(process.getModulePairs(), 160, 100);
            frame.saveModuleLayout(process, layout);
        }
        return layout;
    }

    private void updateDimension() {
        ModuleLayout layout = getModuleLayout();
        Point start = layout.getStartPoint();
        Point end = layout.getEndPoint();
        int minx = Math.min(start.x, end.x);
        int maxx = Math.max(start.x, end.x);
        int miny = Math.min(start.y, end.y);
        int maxy = Math.max(start.y, end.y);
        for (Map.Entry<Module, Point> entry : getModuleLayout().entries()) {
            Point pos = entry.getValue();
            int x = pos.x;
            int y = pos.y;
            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
        }
        minx -= 80;
        maxx += 80;
        miny -= 50;
        maxy += 50;
        prefDim = new Dimension(maxx-minx, maxy-miny);
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
        ModuleLayout layout = getModuleLayout();
        ModuleWrapper selected = (graph==null ? null : graph.getSelected());
        graph = new ModuleGraph(layout, process.getModulePairs());
        graph.select(selected);
        Graphics g2 = g.create();
        try {
            x0 = getWidth()/2;
            y0 = ModuleGraph.MODULE_HEIGHT;
            g2.translate(x0, y0);
            graph.draw((Graphics2D) g2);
        } finally {
            g2.dispose();
        }
    }
}
