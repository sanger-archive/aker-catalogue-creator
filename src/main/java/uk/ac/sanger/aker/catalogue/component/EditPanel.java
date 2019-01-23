package uk.ac.sanger.aker.catalogue.component;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

import java.awt.Component;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeLabel;

/**
 * @author dr6
 */
abstract class EditPanel extends JPanel {
    protected ChangeListener changeListener;
    protected DocumentListener documentListener;

    protected abstract void updateState();

    protected ChangeListener getChangeListener() {
        if (changeListener==null) {
            changeListener = e -> updateState();
        }
        return changeListener;
    }

    protected DocumentListener getDocumentListener() {
        if (documentListener==null) {
            documentListener = (QuickDocumentListener) (this::updateState);
        }
        return documentListener;
    }

    public void add(Component left, Component right, QuickConstraints quickConstraints) {
        if (left != null) {
            add(left, quickConstraints.left());
        }
        if (right != null) {
            add(right, quickConstraints.right());
        }
    }

    public void add(String leftLabelText, Component right, QuickConstraints quickConstraints) {
        Component left = (leftLabelText==null ? null : makeLabel(leftLabelText));
        add(left, right, quickConstraints);
    }
}
