package uk.ac.sanger.aker.catalogue.component.list;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.Product;

import java.util.*;

/**
 * @author dr6
 */
public class ProductListActor implements ListActor<Product> {
    private CatalogueApp app;

    public ProductListActor(CatalogueApp app) {
        this.app = app;
    }

    @Override
    public Product getPrototype() {
        return new Product(ListActor.LONG_NAME);
    }

    @Override
    public Product getNew() {
        Product prod = new Product("New product");
        prod.setProcesses(new ArrayList<>());
        app.getCatalogue().getProducts().add(prod);
        return prod;
    }

    @Override
    public List<Product> delete(Collection<? extends Product> items) {
        List<Product> products = app.getCatalogue().getProducts();
        products.removeAll(items);
        app.clearEditPanel();
        return products;
    }

    @Override
    public void open(Product item) {
        app.view(item);
    }
}
