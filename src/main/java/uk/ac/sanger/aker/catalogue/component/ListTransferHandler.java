package uk.ac.sanger.aker.catalogue.component;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.stream.IntStream;

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

    protected String exportElement(JComponent c) {
        JList<E> list = (JList<E>) c;
        sourceIndex = list.getSelectedIndex();
        draggedItem = list.getSelectedValue();
        return String.valueOf(draggedItem);
    }

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

    public static <E> ListTransferHandler<E> applyTo(JList<E> list) {
        ListTransferHandler<E> handler = new ListTransferHandler<E>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);
        list.setTransferHandler(handler);
        return handler;
    }

    public static void main(String[] args) {

        DefaultListModel<String> model = new DefaultListModel<String>() {};
        IntStream.range(0, 10).mapToObj(i -> "Item "+i).forEach(model::addElement);
        JList<String> list = new JList<>(model);
        applyTo(list);
        JPanel panel = new JPanel();
        panel.add(new JScrollPane(list));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setBounds(100,100,200,400);
        frame.setVisible(true);
    }
}