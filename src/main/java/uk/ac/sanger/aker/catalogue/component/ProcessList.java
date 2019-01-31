package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Product;

import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeButton;

/**
 * @author dr6
 */
public class ProcessList extends JPanel {
    private DefaultEditableListModel<AkerProcess> listModel;
    private JList<AkerProcess> list;
    private JComboBox<AkerProcess> combo;
    private JButton addButton;

    public ProcessList(List<AkerProcess> allProcesses, Product product) {
        listModel = new DefaultEditableListModel<>(product::getProcesses);
        list = new JList<>(listModel);
        list.setPrototypeCellValue(new AkerProcess("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"));
        list.setCellRenderer(new ListNameRenderer());
        ListTransferHandler.applyTo(list);
        combo = new JComboBox<>(allProcesses.toArray(new AkerProcess[0]));
        combo.setRenderer(new ListNameRenderer());
        combo.setSelectedItem(null);
        addButton = makeButton("Add", e -> fireAdd());

        layOut();

        KeyShortcuts.DELETE.register(list, e -> fireDelete());
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        QuickConstraints constraints = new QuickConstraints(new Insets(10,0,10,0));
        constraints.gridwidth = 2;
        add(new JScrollPane(list), constraints);
        constraints.gridwidth = 1;
        add(combo, constraints.incy().left());
        add(addButton, constraints.right());
    }

    private void fireAdd() {
        AkerProcess pro = (AkerProcess) combo.getSelectedItem();
        if (pro==null || listModel.contains(pro)) {
            return;
        }
        listModel.add(pro);
        list.repaint();
    }

    private void fireDelete() {
        listModel.removeIndexes(list.getSelectedIndices());
        list.repaint();
    }
}
