package uk.ac.sanger.aker.catalogue.component.list;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.*;

import java.util.*;

/**
 * @author dr6
 */
public class ProcessListActor implements ListActor<AkerProcess> {
    private CatalogueApp app;

    public ProcessListActor(CatalogueApp app) {
        this.app = app;
    }

    @Override
    public AkerProcess getPrototype() {
        return new AkerProcess(ListActor.LONG_NAME);
    }

    @Override
    public AkerProcess getNew() {
        AkerProcess pro = new AkerProcess("New process");
        pro.setModulePairs(new ArrayList<>());
        app.getCatalogue().getProcesses().add(pro);
        return pro;
    }

    @Override
    public List<AkerProcess> delete(Collection<? extends AkerProcess> items) {
        Catalogue catalogue = app.getCatalogue();
        List<AkerProcess> processes = catalogue.getProcesses();
        if (!(items instanceof HashSet)) {
            items = new HashSet<>(items);
        }
        for (Product product : catalogue.getProducts()) {
            product.getProcesses().removeAll(items);
        }
        processes.removeAll(items);
        app.clearEditPanel();
        return processes;
    }

    @Override
    public void select(AkerProcess item, boolean open) {
        app.view(item, open);
    }
}
