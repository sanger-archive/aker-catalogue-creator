package uk.ac.sanger.aker.catalogue.sorting;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A topological sort is a way of sorting a partially ordered collection.
 * @author dr6
 */
public class TopologicalSorter<E> {
    private Collection<? extends E> items;
    private Map<E, Set<E>> preceders;
    private Map<E, List<E>> followers;

    public TopologicalSorter(Collection<? extends E> items) {
        this.items = items;
    }

    public <P> void setRelations(Stream<? extends P> relations,
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

    public Map<E, Set<E>> getPreceders() {
        return this.preceders;
    }

    public Map<E, List<E>> getFollowers() {
        return this.followers;
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
