package uk.ac.sanger.aker.catalogue.component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A {@link FunctionalInterface functional} version of {@code DocumentListener}.
 * The {@link #anyChange anyChange} method is called for any update operation.
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

    /**
     * The method that is called when any update occurs.
     */
    void anyChange();
}
