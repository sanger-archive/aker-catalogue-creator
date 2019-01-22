package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.HasName;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

/**
 * @author dr6
 */
public class ListNameRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
        if (value instanceof HasName) {
            value = ((HasName) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, selected, focus);
    }
}