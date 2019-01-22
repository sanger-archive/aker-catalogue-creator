package uk.ac.sanger.aker.catalogue.component;

import javax.swing.ListModel;

/**
 * @author dr6
 */
public interface EditableListModel<E> extends ListModel<E> {
    boolean contains(E o);
    default void add(E element) {
        insert(getSize(), element);
    }
    void insert(int index, E element);
    void remove(int index);
}
