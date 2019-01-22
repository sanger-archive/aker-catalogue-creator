package uk.ac.sanger.aker.catalogue.component.list;

import uk.ac.sanger.aker.catalogue.component.ListNameRenderer;
import uk.ac.sanger.aker.catalogue.model.HasName;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.*;

/**
 * @author dr6
 */
public class ListComponent<E extends HasName> extends JPanel {
    private JList<E> list;
    private DefaultListModel<E> model;
    private JButton addButton;
    private ListActor<E> listActor;

    public ListComponent(String desc, ListActor<E> listActor) {
        this.model = new DefaultListModel<>();
        this.list = new JList<>(model);
        this.listActor = listActor;
        list.setPrototypeCellValue(listActor.getPrototype());
        addButton = makeButton("Add", e -> addNew());
        list.setCellRenderer(new ListNameRenderer());
        setLayout(new GridBagLayout());
        Insets insets = new Insets(0,0,0,0);
        JPanel descPanel = new JPanel();
        descPanel.add(makeLabel(desc));
        add(descPanel, new GridBagConstraints(0,0,1,1,0.2, 0.2,
                GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 0.2, 0.8,
                GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 20));
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, new GridBagConstraints(1, 0, 1, 3, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

        registerDelete(list, this::fireDelete);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount()==2 && event.getButton()==MouseEvent.BUTTON1) {
                    int index = list.locationToIndex(event.getPoint());
                    if (index>=0) {
                        openIndex(index);
                    }
                }
            }
        });
    }

    public void addSelectionListener(ListSelectionListener listener) {
        list.addListSelectionListener(listener);
    }

    private void addNew() {
        E newElement = listActor.getNew();
        addElement(newElement);
        list.setSelectedValue(newElement, true);
        listActor.open(newElement);
    }

    public void addElement(E element) {
        model.addElement(element);
    }

    private void openIndex(int index) {
        E item = model.elementAt(index);
        if (item!=null) {
            listActor.open(item);
        }
    }

    public E getSelectedItem() {
        return list.getSelectedValue();
    }

    private void fireDelete() {
        int[] selectedIndices = list.getSelectedIndices();
        if (selectedIndices.length==0) {
            return;
        }
        Set<E> itemsToDelete = Arrays.stream(selectedIndices)
                .mapToObj(model::get)
                .collect(Collectors.toSet());
        List<E> remaining = listActor.delete(itemsToDelete);
        model.removeAllElements();
        remaining.forEach(model::addElement);
    }

}
