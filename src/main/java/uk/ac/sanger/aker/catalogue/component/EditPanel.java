package uk.ac.sanger.aker.catalogue.component;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.Component;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeLabel;

/**
 * A base class for panels that are editing part of the model.
 * @author dr6
 */
abstract class EditPanel extends JPanel {
    protected ChangeListener changeListener;
    protected DocumentListener documentListener;

    /**
     * Gets a change listener that will trigger {@link #updateState updateState}.
     * Multiple calls to this method will return the same listener object.
     */
    protected ChangeListener getChangeListener() {
        if (changeListener==null) {
            changeListener = e -> updateState();
        }
        return changeListener;
    }

    /**
     * Gets a document listener that will trigger {@link #updateState updateState}.
     * Multiple calls to this method will return the same listener object.
     */
    protected DocumentListener getDocumentListener() {
        if (documentListener==null) {
            documentListener = (QuickDocumentListener) (this::updateState);
        }
        return documentListener;
    }

    /**
     * A convenience method for adding two components (in a row)
     * using a {@link QuickConstraints} object to place them on the left and right.
     * If either given component is null, it is skipped.
     * @param left the component to add on the left
     * @param right the component to add on the right
     * @param quickConstraints the constraints indicating the position of the components
     */
    public void add(Component left, Component right, QuickConstraints quickConstraints) {
        if (left != null) {
            add(left, quickConstraints.left());
        }
        if (right != null) {
            add(right, quickConstraints.right());
        }
    }

    /**
     * A convenience method for adding two components (in a row)
     * using a {@link QuickConstraints} object to place them on the left and right.
     * The {@code leftLabelText} will be placed as a {@code JLabel} on the left.
     * If either of the given string or the given component is null, that element is skipped.
     * @param leftLabelText the string for the label
     * @param right the component to add on the right
     * @param quickConstraints the constraints indicating the position of the components
     */
    public void add(String leftLabelText, Component right, QuickConstraints quickConstraints) {
        Component left = (leftLabelText==null ? null : makeLabel(leftLabelText));
        add(left, right, quickConstraints);
    }

    /**
     * This is triggered when the user edits the fields in the panel,
     * so the information can be saved to the model.
     */
    protected abstract void updateState();

    /**
     * Load the data from this panel's model into its GUI fields.
     */
    protected abstract void load();

    /**
     * This is triggered when the user double-clicks on an item in the catalogue panel.
     * Its action is to bring edit focus to this panel.
     */
    protected abstract void fireOpen();

}
