package uk.ac.sanger.aker.catalogue.graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A topological sort is a way of sorting a partially ordered collection.
 * @author dr6
 */
public class TopologicalSorter<E> {
    private Collection<? extends E> items;
    private Map<E, Set<E>> preceders;
    private Map<E, List<E>> followers;

    /**
     * Creates a {@code TopologicalSorter} to sort the given collection.
     */
    public TopologicalSorter(Collection<? extends E> items) {
        this.items = items;
    }

    /**
     * Set the relations (X must precede Y) for this sort.
     * It is given in a very generic form to avoid having to create unnecessary maps,
     * or items of some prescribed type, since the internals of this object create the required maps for itself.
     * The {@code relations} sequence will only be iterated once, so it is safe to pass a {@code Stream::iterator}
     * as the iterable.
     * @param relations a sequence of some kind of object representing an {@code X must precede Y} relation.
     * @param firstExtractor the function that will extract {@code X}, the preceder, from the relation
     * @param secondExtractor the function that will extract {@code Y}, the follower, from the relation
     * @param <P> the type of object used to represent the relation
     */
    public <P> void setRelations(Iterable<? extends P> relations,
                                 Function<? super P, ? extends E> firstExtractor,
                                 Function<? super P, ? extends E> secondExtractor) {
        setRelations(relations.iterator(), firstExtractor, secondExtractor);
    }

    private <P> void setRelations(Iterator<? extends P> relations,
                                  Function<? super P, ? extends E> firstExtractor,
                                  Function<? super P, ? extends E> secondExtractor) {
        preceders = new HashMap<>();
        followers = new HashMap<>();
        for (E item : items) {
            preceders.put(item, new HashSet<>());
            followers.put(item, new ArrayList<>());
        }
        while (relations.hasNext()) {
            P pair = relations.next();
            E first = firstExtractor.apply(pair);
            E second = secondExtractor.apply(pair);
            preceders.get(second).add(first);
            followers.get(first).add(second);
        }
    }

    /**
     * Gets the map of each element to the set of elements that are required to precede it.
     * This map is generated when {@link #setRelations} is called, and each set is emptied during a
     * successful {@link #sort}.
     * @return The map of each element to the set of elements that are required to preceded it
     */
    public Map<E, Set<E>> getPreceders() {
        return this.preceders;
    }

    /**
     * Topologically sort the given items by the given information.
     * @return The sorted list of items
     * @exception IllegalArgumentException if the list cannot be sorted from the given information
     */
    public List<E> sort() {
        if (preceders==null || followers==null) {
            throw new IllegalStateException("Must setRelations before calling TopologicalSort::sort");
        }
        List<E> output = new ArrayList<>(items.size());
        List<E> ready = items.stream()
                .filter(e -> {
                    Set<? extends E> v = preceders.get(e);
                    return (v==null || v.isEmpty());
                })
                .collect(Collectors.toList());
        for (int i = 0; i < ready.size(); ++i) {
            E item = ready.get(i);
            Collection<? extends E> fols = followers.get(item);
            if (fols!=null) {
                for (E fol : fols) {
                    Set<? extends E> precs = preceders.get(fol);
                    precs.remove(item);
                    if (precs.isEmpty()) {
                        ready.add(fol);
                    }
                }
            }
            output.add(item);
        }
        if (output.size() != items.size()) {
            throw new IllegalArgumentException("Invalid search data");
        }
        return output;
    }
}
