package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import java.awt.datatransfer.*;

/**
 * A handler to let you drag an item up and down inside a {@link JList}.
 * Call {@link #applyTo(JList)} to set it up.
 * Only supports single-item selection.
 * @param <E> the type of item in the list
 */
public class ListTransferHandler<E> extends TransferHandler {
    private int sourceIndex = -1;
    private int dropIndex = -1;
    private E draggedItem = null;

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return new StringSelection(exportElement(c));
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop() || sourceIndex < 0) {
            return false;
        }
        try {
            dropIndex = ((JList.DropLocation) info.getDropLocation()).getIndex();
            if (dropIndex==sourceIndex) {
                dropIndex = -1;
                return false;
            }
            return importElement((JComponent) info.getComponent());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c);
    }

    @SuppressWarnings("unchecked")
    protected String exportElement(JComponent c) {
        JList<E> list = (JList<E>) c;
        sourceIndex = list.getSelectedIndex();
        draggedItem = list.getSelectedValue();
        return String.valueOf(draggedItem);
    }

    @SuppressWarnings("unchecked")
    protected boolean importElement(JComponent c) {
        ListModel<E> model = ((JList) c).getModel();
        if (model instanceof EditableListModel) {
            ((EditableListModel<E>) model).insert(dropIndex, draggedItem);
        } else {
            ((DefaultListModel<E>) model).insertElementAt(draggedItem, dropIndex);
        }
        return true;
    }

    protected void cleanup(JComponent c) {
        if (sourceIndex >= 0 && dropIndex>=0) {
            JList source = (JList) c;
            int index = sourceIndex;
            if (index > dropIndex) {
                index += 1;
            }
            ListModel model  = source.getModel();
            if (index >= 0 && index < model.getSize()) {
                if (model instanceof EditableListModel) {
                    ((EditableListModel) model).remove(index);
                } else {
                    ((DefaultListModel) model).removeElementAt(index);
                }
                if (index < dropIndex) {
                    dropIndex -= 1;
                }
            }
            source.setSelectedIndex(dropIndex);
        }
        sourceIndex = -1;
        dropIndex = -1;
        draggedItem = null;
    }

    /**
     * Set up drag-reordering in the given {@code JList}
     * @param list the {@code JList} that you want to support drag-reordering
     * @param <E> the type of item in the list
     * @return the handler created to support the list (may safely be ignored)
     */
    public static <E> ListTransferHandler<E> applyTo(JList<E> list) {
        ListTransferHandler<E> handler = new ListTransferHandler<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);
        list.setTransferHandler(handler);
        return handler;
    }
}