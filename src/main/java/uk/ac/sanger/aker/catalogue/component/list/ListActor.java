package uk.ac.sanger.aker.catalogue.component.list;

import java.util.Collection;
import java.util.List;

public interface ListActor<E> {
    String LONG_NAME = "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM";
    E getPrototype();
    E getNew();
    List<E> delete(Collection<? extends E> items);
    void open(E item);
}
