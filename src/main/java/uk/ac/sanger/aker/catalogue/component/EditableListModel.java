package uk.ac.sanger.aker.catalogue.component;

import javax.swing.ListModel;

/**
 * An extension of {@code ListModel} that supports a few additional methods.
 * @author dr6
 */
public interface EditableListModel<E> extends ListModel<E> {
    /**
     * Does this model contain the given item?
     * @param o the item to check for
     * @return true if the item is in this list, otherwise false
     */
    boolean contains(E o);

    /**
     * Adds an element to the end of this list.
     * By default, this delegates to {@link #insert insert}.
     * @param element the element to add
     */
    default void add(E element) {
        insert(getSize(), element);
    }

    /**
     * Insert an element at the given position in this list
     * @param index the index to add the item. {@code 0} will add at the beginning of the list;
     * {@code size() will add at the end}.
     * @param element the element to add to the list
     */
    void insert(int index, E element);

    /**
     * Removes the item at the given index.
     * @param index the index of the item to remove.
     * @exception IndexOutOfBoundsException if the index is not in the range 0 to {@code size()-1}
     */
    void remove(int index);
}
