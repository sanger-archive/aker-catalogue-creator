package uk.ac.sanger.aker.catalogue.component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author dr6
 */
@FunctionalInterface
public interface QuickDocumentListener extends DocumentListener {
    @Override
    default void insertUpdate(DocumentEvent e) {
        anyChange();
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        anyChange();
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        anyChange();
    }

    void anyChange();
}
