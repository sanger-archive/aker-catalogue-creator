package uk.ac.sanger.aker.catalogue.model;

import org.testng.annotations.Test;
import uk.ac.sanger.aker.catalogue.graph.TopologicalSorter;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Test of {@link TopologicalSorter}
 * @author dr6
 */
@Test
public class TopoligicalSortTest {
    public void testValidSort() {
        List<String> items = Arrays.asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta");
        TopologicalSorter<String> sorter = new TopologicalSorter<>(items);
        String[][] relations = new String[][] {
                { "Epsilon", "Delta" }, {"Zeta", "Epsilon" }, {"Eta", "Delta" } };
        sorter.setRelations(Arrays.stream(relations)::iterator, arr -> arr[0], arr -> arr[1]);
        List<String> sorted = sorter.sort();
        assertEquals(new HashSet<>(sorted), new HashSet<>(items));
        assertEquals(sorted.subList(0,3), Arrays.asList("Alpha", "Beta", "Gamma"));
        for (String[] relation : relations) {
            assertTrue(sorted.indexOf(relation[0]) < sorted.indexOf(relation[1]));
        }
    }

    public void testInvalidSort() {
        List<String> items = Arrays.asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta");
        TopologicalSorter<String> sorter = new TopologicalSorter<>(items);
        String[][] relations = new String[][] {
                { "Epsilon", "Delta" }, {"Zeta", "Epsilon" }, {"Delta", "Zeta" } };
        sorter.setRelations(Arrays.stream(relations)::iterator, arr -> arr[0], arr -> arr[1]);
        IllegalArgumentException ex = null;
        try {
            sorter.sort();
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull(ex, "An exception should have been thrown.");
    }
}
