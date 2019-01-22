package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.Product;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.*;

/**
 * @author dr6
 */
public class ProductPanel extends EditPanel {
    private CatalogueApp app;
    private Product product;

    private JLabel headlineLabel;
    private JTextField nameField;
    private UuidField uuidField;
    private JTextField descField;
    private JTextField bioTypeField;
    private JSpinner versionField;
    private JCheckBox availableCheckbox;
    private ProcessList processList;

    private boolean loading;

    public ProductPanel(Product product, CatalogueApp app) {
        this.product = product;
        this.app = app;
        initComponents();
        load();
        layOut();
    }

    private void initComponents() {
        headlineLabel = makeHeadline("Product");
        nameField = makeTextField();
        uuidField = new UuidField(product);
        descField = makeTextField();
        bioTypeField = makeTextField();
        versionField = makeSpinner(0, 0);
        availableCheckbox = makeCheckbox();
        processList = new ProcessList(app.getCatalogue().getProcesses(), product);

        ChangeListener cl = getChangeListener();
        DocumentListener dl = getDocumentListener();
        nameField.getDocument().addDocumentListener(dl);
        descField.getDocument().addDocumentListener(dl);
        bioTypeField.getDocument().addDocumentListener(dl);
        versionField.addChangeListener(cl);
        availableCheckbox.addChangeListener(cl);
    }

    private void layOut() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(10,0,10,0);
        GridBagConstraints cleft = new GridBagConstraints(0,0,1,1,0,0,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 10, 0);
        GridBagConstraints cright = new GridBagConstraints(1,0,1,1,0,0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
        add(headlineLabel, new GridBagConstraints(0,0,2,1,0,0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        addRow("Name:", nameField, 1, cleft, cright);
        addRow("Description:", descField, 2, cleft, cright);
        addRow("UUID:", uuidField, 3, cleft, cright);
        addRow("Product version:", versionField, 4, cleft, cright);
        addRow("Availability:", availableCheckbox, 5, cleft, cright);
        addRow("Requested bio type:", bioTypeField, 6, cleft, cright);
        cleft.anchor = GridBagConstraints.FIRST_LINE_END;
        addRow("Processes:", processList, 7, cleft, cright);
    }

    private void addRow(Object o1, Object o2, int y, GridBagConstraints cleft, GridBagConstraints cright) {
        cleft.gridy = y;
        cright.gridy = y;
        if (o1 instanceof String) {
            o1 = makeLabel((String) o1);
        }
        if (o2 instanceof String) {
            o2 = makeLabel((String) o2);
        }
        add((Component) o1, cleft);
        add((Component) o2, cright);
    }

    private void load() {
        if (loading) {
            return;
        }
        loading = true;

        nameField.setText(product.getName());
        headlineLabel.setText("Product: "+product.getName());
        uuidField.setText(product.getUuid());
        descField.setText(product.getDescription());
        versionField.setValue(product.getProductVersion());
        availableCheckbox.setSelected(product.getAvailability()!=0);
        bioTypeField.setText(product.getBioType());

        loading = false;
    }

    private void save() {
        if (loading) {
            return;
        }

        product.setName(nameField.getText());
        headlineLabel.setText("Product: "+product.getName());
        product.setDescription(descField.getText());
        //product.setUuid(uuidField.getText());
        product.setProductVersion((int) versionField.getValue());
        product.setAvailability(availableCheckbox.isSelected() ? 1 : 0);
        product.setBioType(bioTypeField.getText());
        app.productsUpdated();
    }

    public void claimFocus() {
        nameField.requestFocusInWindow();
    }

    @Override
    protected void updateState() {
        save();
    }
}
