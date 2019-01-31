package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.HasName;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;

/**
 * A renderer to use in {@link JList}s and {@link javax.swing.JComboBox}es in this application.
 * If the value being rendered is an instance of {@link HasName}, then its name will be rendered.
 * Html rendering in instances of this class is disabled by default, so if a user creates an item
 * with the name {@code <html><b>Hi there</b></html>}, it will appear as
 * {@code <html><b>Hi there</b></html>} rather than <b>Hi there</b>.
 * Also, this list shows the selected item in grey when the list itself is not in focus.
 * @author dr6
 */
public class ListNameRenderer extends DefaultListCellRenderer {
    public ListNameRenderer() {
        putClientProperty("html.disable", Boolean.TRUE);
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
        if (value instanceof HasName) {
            value = ((HasName) value).getName();
        }
        Component comp = super.getListCellRendererComponent(list, value, index, selected, focus);
        if (selected && !focus) {
            comp.setBackground(Color.lightGray);
            comp.setForeground(Color.black);
        }
        return comp;
    }
}
