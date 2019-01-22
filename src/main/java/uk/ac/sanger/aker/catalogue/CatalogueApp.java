package uk.ac.sanger.aker.catalogue;

import uk.ac.sanger.aker.catalogue.component.CatalogueFrame;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;
import uk.ac.sanger.aker.catalogue.model.*;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author dr6
 */
public class CatalogueApp implements Runnable {
    private Catalogue catalogue;
    private CatalogueFrame frame;

    @Override
    public void run() {
        JsonImporter jim = new JsonImporter();
        try {
            catalogue = jim.importCatalogue(jim.readPath(Paths.get("/Users/dr6/Desktop/new_catalogue.json")));
        } catch (IOException e) {
            e.printStackTrace();
            catalogue = new Catalogue();
        }

        createFrame();
        frame.setJMenuBar(createMenuBar());
        frame.setVisible(true);
    }

    private CatalogueFrame createFrame() {
        frame = new CatalogueFrame(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        return menuBar;
    }

    public Catalogue getCatalogue() {
        return this.catalogue;
    }

    public void view(Module module) {
        frame.view(module);
    }

    public void view(Product product) {
        frame.view(product);
    }

    public void view(AkerProcess process) {
        frame.view(process);
    }

    public void productsUpdated() {
        frame.productsUpdated();
    }

    public void clearEditPanel() {
        frame.clearEditPanel();
    }

    public CatalogueFrame getFrame() {
        return this.frame;
    }
}
