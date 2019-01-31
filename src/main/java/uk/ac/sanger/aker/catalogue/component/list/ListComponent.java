package uk.ac.sanger.aker.catalogue.component.list;

import uk.ac.sanger.aker.catalogue.component.KeyShortcuts;
import uk.ac.sanger.aker.catalogue.component.ListNameRenderer;
import uk.ac.sanger.aker.catalogue.model.HasName;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeButton;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeLabel;

/**
 * A component containing a label, a list, and an add button.
 * The add button can add new items to the list.
 * Selecting or double-clicking an item will activate the item.
 * When the list has focus, the delete or backspace key will delete the selected item from the list.
 * The actions themselves are controlled by a callback object implementing {@link ListActor}.
 * @author dr6
 */
public class ListComponent<E extends HasName> extends JPanel implements ListSelectionListener {
    private JList<E> list;
    private DefaultListModel<E> model;
    private ListActor<E> listActor;

    public ListComponent(String desc, ListActor<E> listActor) {
        this.model = new DefaultListModel<>();
        this.list = new JList<>(model);
        this.listActor = listActor;
        list.setPrototypeCellValue(listActor.getPrototype());
        list.setCellRenderer(new ListNameRenderer());
        setLayout(new GridBagLayout());
        Insets insets = new Insets(0,0,0,0);
        JPanel descPanel = new JPanel();
        descPanel.add(makeLabel(desc));
        add(descPanel, new GridBagConstraints(0,0,1,1,0.2, 0.2,
                GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 0));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(makeButton("Add", e -> addNew()));
        add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 0.2, 0.8,
                GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, insets, 0, 20));
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, new GridBagConstraints(1, 0, 1, 3, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

        KeyShortcuts.DELETE.register(list, e -> fireDelete());

        list.addListSelectionListener(this);
        list.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                listActor.select(singleSelectedItem(), false);
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount()==2 && SwingUtilities.isLeftMouseButton(event)) {
                    int index = list.locationToIndex(event.getPoint());
                    if (index >= 0) {
                        E item = model.getElementAt(index);
                        E selectedItem = singleSelectedItem();
                        if (selectedItem==null || selectedItem==item) {
                            listActor.select(item, true);
                        }
                    }
                }
            }
        });
    }

    private void addNew() {
        E newElement = listActor.getNew();
        addElement(newElement);
        list.setSelectedValue(newElement, true);
    }

    public void addElement(E element) {
        model.addElement(element);
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

    public void setItems(List<E> items) {
        model.clear();
        items.forEach(model::addElement);
    }

    public E singleSelectedItem() {
        int index = list.getMinSelectionIndex();
        if (index>=0 && index==list.getMaxSelectionIndex()) {
            return model.getElementAt(index);
        }
        return null;
    }

    // ListSelectionListener
    @Override
    public void valueChanged(ListSelectionEvent event) {
        listActor.select(singleSelectedItem(), false);
    }
}
