package uk.ac.sanger.aker.catalogue.component;

import javax.swing.AbstractListModel;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * An implementation of {@code EditableListModel}.
 * The items themselves are supplied by a {@link Supplier Supplier&lt;List&lt;E&gt;&gt;} which must
 * return a list that supports testing and modifications.
 * The supplier is used each time access to the list is required, so the provider is free to return
 * new lists as appropriate.
 * @author dr6
 */
public class DefaultEditableListModel<E> extends AbstractListModel<E> implements EditableListModel<E> {
    private Supplier<List<E>> listSupplier;

    /**
     * Constructs a new model that uses the given supplier to get access to its list.
     * @param listSupplier a function that will supply the list of items in this list
     */
    public DefaultEditableListModel(Supplier<List<E>> listSupplier) {
        this.listSupplier = listSupplier;
    }

    protected List<E> getItems() {
        return listSupplier.get();
    }

    @Override
    public boolean contains(E o) {
        return getItems().contains(o);
    }

    @Override
    public void insert(int index, E element) {
        getItems().add(index, element);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public void remove(int index) {
        getItems().remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public int getSize() {
        return getItems().size();
    }

    @Override
    public E getElementAt(int index) {
        return getItems().get(index);
    }

    /** Remove the items at the given indexes. */
    public void removeIndexes(int[] indexes) {
        if (indexes==null || indexes.length==0) {
            return;
        }
        if (indexes.length > 1) {
            Arrays.sort(indexes);
        }
        List<E> items = getItems();
        for (int i = 0; i < indexes.length; ++i) {
            items.remove(indexes[i] - i);
        }
        fireContentsChanged(this, indexes[0], indexes[indexes.length-1]);
    }
}
