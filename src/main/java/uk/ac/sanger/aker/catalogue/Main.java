package uk.ac.sanger.aker.catalogue;

import javax.swing.SwingUtilities;

/**
 * The entry point of the application.
 * @author dr6
 */
public class Main {
    private Main() {}

    /**
     * Sets the apple properties for the application.
     * Creates a new {@link CatalogueApp} to be executed by the AWT thread.
     */
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Catalogue");
        SwingUtilities.invokeLater(new CatalogueApp());
    }
}
