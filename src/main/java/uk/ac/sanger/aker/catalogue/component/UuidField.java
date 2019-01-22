package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.model.HasUuid;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.UUID;

import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeButton;
import static uk.ac.sanger.aker.catalogue.component.ComponentFactory.makeTextField;

/**
 * @author dr6
 */
public class UuidField extends JPanel {
    public static final FixInputFilter FILTER = new FixInputFilter(UuidField::filterString);
    private static final Color INVALID_BG = new Color(0xffc0c0);
    private static final Color VALID_BG = Color.white;

    private JTextField textField;
    private HasUuid model;

    public UuidField(HasUuid model) {
        this.model = model;
        textField = makeTextField();
        JButton generateButton = makeButton("Generate", e -> generateUuid());
        Document doc = textField.getDocument();
        doc.addDocumentListener((QuickDocumentListener) this::changed);
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).setDocumentFilter(FILTER);
        }

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(textField);
        add(Box.createHorizontalStrut(5));
        add(generateButton);
        add(Box.createHorizontalGlue());
        textField.addActionListener(e -> reloadUuid());
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                reloadUuid();
            }
        });
    }

    private void reloadUuid() {
        setText(model.getUuid());
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public String getText() {
        return textField.getText();
    }

    private void generateUuid() {
        setText(UUID.randomUUID().toString());
    }

    public UUID getUuid() {
        try {
            return UUID.fromString(getText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String filterString(String string) {
        return string.toLowerCase().replaceAll("[^0-9a-f-]", "");
    }

    private void changed() {
        UUID uuid = getUuid();
        Color bg = uuid==null ? INVALID_BG : VALID_BG;
        textField.setBackground(bg);
        if (uuid!=null) {
            model.setUuid(uuid.toString());
        }
    }
}
