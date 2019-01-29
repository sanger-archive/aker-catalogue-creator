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
        QuickConstraints constraints = new QuickConstraints(new Insets(10,0,10,0));
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 2;
        add(headlineLabel, constraints);
        constraints.gridwidth = 1;
        constraints.insets.left = 10;
        add("Name:", nameField, constraints.incy());
        add("Description:", descField, constraints.incy());
        add("UUID:", uuidField, constraints.incy());
        add("Product version:", versionField, constraints.incy());
        add("Available:", availableCheckbox, constraints.incy());
        add("Requested bio type:", bioTypeField, constraints.incy());
        constraints.leftAnchor = GridBagConstraints.FIRST_LINE_END;
        add("Processes:", processList, constraints.incy());
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

    @Override
    protected void updateState() {
        save();
    }

    public void fireOpen() {
        nameField.requestFocusInWindow();
    }
}
