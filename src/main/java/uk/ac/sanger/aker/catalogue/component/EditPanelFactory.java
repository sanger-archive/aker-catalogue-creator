package uk.ac.sanger.aker.catalogue.component;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.*;

import java.util.Objects;

/**
 * A factory to provide {@link EditPanel EditPanels} for a given model item.
 * @author dr6
 */
public class EditPanelFactory {
    private CatalogueApp app;

    /**
     * Makes a new instance of {@code EditPanelFactory} that will use the given app to create new panels.
     * @param app the catalogue app that will be required by new edit panels
     */
    public EditPanelFactory(CatalogueApp app) {
        this.app = app;
    }

    /**
     * Makes an {@code EditPanel} for the given item.
     * @param item the {@link Module}, {@link AkerProcess} or {@link Product} for the panel.
     * @param <E> the type of item
     * @return an instance of {@code EditPanel} appropriate to the given item
     * @exception IllegalArgumentException if the item is not one an instance of {@link Module},
     *            {@link AkerProcess} or {@link Product}
     */
    public <E> EditPanel makePanel(E item) {
        Objects.requireNonNull(item, "makePanel received null");
        if (item instanceof Module) {
            return new ModulePanel((Module) item, app);
        }
        if (item instanceof AkerProcess) {
            return new ProcessPanel((AkerProcess) item, app);
        }
        if (item instanceof Product) {
            return new ProductPanel((Product) item, app);
        }
        throw new IllegalArgumentException("makePanel received unexpected item: "+item);
    }
}
