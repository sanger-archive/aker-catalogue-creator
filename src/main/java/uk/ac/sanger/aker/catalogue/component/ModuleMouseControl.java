package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.Module;
import uk.ac.sanger.aker.catalogue.model.ModulePair;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author dr6
 */
public class ModuleMouseControl extends MouseAdapter {
    private enum Button { LEFT, RIGHT }

    private Button heldButton;

    private ProcessModulePanel panel;
    private int lastX, lastY;
    private int x0, y0;

    public ModuleMouseControl(ProcessModulePanel panel) {
        this.panel = panel;
    }

    public void setOrigin(int x0, int y0) {
        this.x0 = x0;
        this.y0 = y0;
    }

    public ModuleGraph getGraph() {
        return panel.getGraph();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        panel.requestFocusInWindow();
        Button button = getButton(e);
        if (button==null) {
            return;
        }
        ModuleGraph graph = getGraph();
        if (graph==null) {
            return;
        }
        heldButton = button;
        lastX = e.getX() - x0;
        lastY = e.getY() - y0;
        Module module = graph.moduleAt(lastX, lastY);
        graph.select(module);
        if (module==null) {
            ModulePair pair = graph.pathAt(lastX, lastY);
            graph.selectPair(pair);
            if (pair != null && e.isShiftDown()) {
                pair.setDefaultPath(!pair.isDefaultPath());
            }
        }
        panel.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (getButton(e)==Button.LEFT && e.getClickCount()==2) {
            ModuleGraph graph = getGraph();
            if (graph.moduleAt(lastX, lastY)!=null) {
                return;
            }
            Module moduleToAdd = panel.getModuleToAdd();
            if (moduleToAdd!=null && graph.addModule(moduleToAdd, lastX, lastY)) {
                panel.clearModuleToAdd();
                panel.repaint();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (heldButton==null) {
            return;
        }
        ModuleGraph graph = getGraph();
        if (graph==null || !graph.anySelected()) {
            return;
        }
        int x = e.getX() - x0;
        int y = e.getY() - y0;
        if (heldButton==Button.LEFT) {
            graph.moveSelected(x - lastX, y - lastY);
        } else if (heldButton==Button.RIGHT) {
            graph.projectPath(x, y);
        }
        lastX = x;
        lastY = y;
        panel.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (getGraph().anySelected()) {
            panel.updateDimension();
        }
        if (heldButton==Button.RIGHT) {
            ModuleGraph graph = getGraph();
            if (graph.hasProjectedPath()) {
                graph.releaseProjectedPath(e.isShiftDown());
                panel.repaint();
            }
        }
        heldButton = null;
    }

    private static Button getButton(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            return e.isControlDown() ? Button.RIGHT : Button.LEFT;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            return Button.RIGHT;
        }
        return null;
    }

}
