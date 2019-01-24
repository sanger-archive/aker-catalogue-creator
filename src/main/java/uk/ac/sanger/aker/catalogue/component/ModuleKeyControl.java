package uk.ac.sanger.aker.catalogue.component;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.registerEnter;

/**
 * @author dr6
 */
public class ModuleKeyControl {
    private ProcessModulePanel panel;

    public ModuleKeyControl(ProcessModulePanel panel) {
        this.panel = panel;
        KeyShortcuts.DELETE.register(panel, e -> fireDelete());
        registerEnter(panel, e -> fireEnter());
    }

    private ModuleGraph getGraph() {
        return panel.getGraph();
    }

    private void fireDelete() {
        ModuleGraph graph = getGraph();
        if (graph.anySelected()) {
            graph.deleteSelected();
            panel.repaint();
        } else if (graph.anyPairSelected()) {
            graph.deleteSelectedPair();
            panel.repaint();
        }
    }

    private void fireEnter() {
        ModuleGraph graph = getGraph();
        if (graph.anyPairSelected()) {
            graph.editSelectedPair();
            panel.repaint();
        }
    }
}
