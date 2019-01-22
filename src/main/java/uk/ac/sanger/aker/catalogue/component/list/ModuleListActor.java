package uk.ac.sanger.aker.catalogue.component.list;

import uk.ac.sanger.aker.catalogue.CatalogueApp;
import uk.ac.sanger.aker.catalogue.model.*;

import java.util.*;

/**
 * @author dr6
 */
public class ModuleListActor implements ListActor<Module> {
    private CatalogueApp app;

    public ModuleListActor(CatalogueApp app) {
        this.app = app;
    }

    @Override
    public Module getPrototype() {
        return new Module(ListActor.LONG_NAME);
    }

    @Override
    public Module getNew() {
        Module mod = new Module("New module");
        app.getCatalogue().getModules().add(mod);
        return mod;
    }

    @Override
    public List<Module> delete(Collection<? extends Module> items) {
        if (!(items instanceof HashSet)) {
            items = new HashSet<>(items);
        }
        Catalogue catalogue = app.getCatalogue();
        for (AkerProcess process : catalogue.getProcesses()) {
            Iterator<ModulePair> pairIter = process.getModulePairs().iterator();
            while (pairIter.hasNext()) {
                ModulePair pair = pairIter.next();
                if (items.contains(pair.getFrom()) || items.contains(pair.getTo())) {
                    pairIter.remove();
                }
            }
        }
        catalogue.getModules().removeAll(items);
        app.clearEditPanel();
        return catalogue.getModules();
    }

    @Override
    public void open(Module item) {
        app.view(item);
    }
}
