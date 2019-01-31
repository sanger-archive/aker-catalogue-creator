package uk.ac.sanger.aker.catalogue.component.list;

import java.util.Collection;
import java.util.List;

/**
 * Some callbacks for the {@link ListComponent} when various things happen.
 * @param <E> The type of item being listed.
 */
public interface ListActor<E> {
    /** A long string used to give appropriate size to list elements. */
    String LONG_NAME = "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM";

    /** Returns a prototype item used to size the list elements. */
    E getPrototype();

    /** Add a new item in the catalogue and return it for display in the list. */
    E getNew();

    /** Delete the specified items from the catalogue, and return the revised list of items. */
    List<E> delete(Collection<? extends E> items);

    /**
     * Notify that the specified item has been selected in the list.
     * @param item the selected item (or null indicating no item is selected)
     * @param open true indicates that the item should be actively opened, not just selected
     */
    void select(E item, boolean open);
}
