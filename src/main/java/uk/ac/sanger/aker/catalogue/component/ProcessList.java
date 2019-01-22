package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.AkerProcess;
import uk.ac.sanger.aker.catalogue.model.Product;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeButton;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.registerDelete;

/**
 * @author dr6
 */
public class ProcessList extends JPanel {
    private AbstractEditableListModel<AkerProcess> listModel;
    private JList<AkerProcess> list;
    private JComboBox<AkerProcess> combo;
    private JButton addButton;

    public ProcessList(List<AkerProcess> allProcesses, Product product) {
        listModel = new AbstractEditableListModel<>(product::getProcesses);
        list = new JList<>(listModel);
        list.setPrototypeCellValue(new AkerProcess("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"));
        ListTransferHandler.applyTo(list);
        combo = new JComboBox<>(allProcesses.toArray(new AkerProcess[0]));
        combo.setSelectedItem(null);
        addButton = makeButton("Add", e -> fireAdd());

        layOut();

        registerDelete(list, this::fireDelete);
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(10,0,10,0);
        add(new JScrollPane(list), new GridBagConstraints(0,0,2, 1, 0,0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        add(combo, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
        add(addButton, new GridBagConstraints(1, 1, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0));
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
