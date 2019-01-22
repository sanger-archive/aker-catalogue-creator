package uk.ac.sanger.aker.catalogue.conversion;

import uk.ac.sanger.aker.catalogue.model.*;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dr6
 */
public class JsonImporter extends JsonInput {
    private enum PairPosition { FROM, TO };

    public Catalogue importCatalogue(JsonValue jsonValue) throws IOException {
        Objects.requireNonNull(jsonValue, "jsonValue is null");
        if (!(jsonValue instanceof JsonObject)) {
            throw exception("Expected a JSON object, but got "+jsonValue.getValueType());
        }

        Catalogue catalogue = new Catalogue();
        List<Module> modules = new ArrayList<>();
        List<AkerProcess> processes = new ArrayList<>();
        List<Product> products = new ArrayList<>();

        JsonObject catData = objectFrom(jsonValue.asJsonObject(), "catalogue");
        catalogue.setLimsId(stringFrom(catData, "lims_id"));
        catalogue.setPipeline(stringFrom(catData, "pipeline"));
        catalogue.setUrl(stringFrom(catData, "url"));

        readProcesses(catData, processes, modules);
        readProducts(catData, products, processes);

        catalogue.setModules(modules);
        catalogue.setProcesses(processes);
        catalogue.setProducts(products);

        return catalogue;
    }

    private Module getModule(String name, List<Module> modules, Map<String, Module> moduleNames, PairPosition pos) {
        if (name==null) {
            return (pos==PairPosition.FROM ? Module.START : Module.END);
        }
        Module mod = moduleNames.get(name);
        if (mod!=null) {
            return mod;
        }
        mod = new Module(name);
        modules.add(mod);
        moduleNames.put(name, mod);
        return mod;
    }

    public void readProcesses(JsonObject catData, List<AkerProcess> processes, List<Module> modules) throws IOException {
        Map<String, Module> moduleNames = new HashMap<>();
        for (JsonObject proData : iterObjects(catData, "processes")) {
            AkerProcess pro = new AkerProcess();
            pro.setName(stringFrom(proData, "name"));
            pro.setUuid(stringFrom(proData, "uuid"));
            pro.setTat(intFrom(proData, "TAT"));
            pro.setProcessClass(stringFrom(proData, "process_class"));
            List<ModulePair> modulePairs = new ArrayList<>();
            for (JsonObject pairData : iterObjects(proData, "process_module_pairings")) {
                String toName = pairData.getString("to_step", null);
                String fromName = pairData.getString("from_step", null);
                boolean defaultPath = pairData.getBoolean("default_path", false);
                Module toMod = getModule(toName, modules, moduleNames, PairPosition.TO);
                Module fromMod = getModule(fromName, modules, moduleNames, PairPosition.FROM);
                modulePairs.add(new ModulePair(fromMod, toMod, defaultPath));
            }
            for (JsonObject paramData : iterObjects(proData, "module_parameters")) {
                String name = stringFrom(paramData, "name");
                Integer minValue = integerFrom(paramData, "min_value");
                Integer maxValue = integerFrom(paramData, "max_value");
                if (minValue!=null || maxValue!=null) {
                    Module module = moduleNames.get(name);
                    if (module==null) {
                        throw exception("Param given for unlisted module: "+name);
                    }
                    module.setMinValue(minValue);
                    module.setMaxValue(maxValue);
                }
            }
            pro.setModulePairs(modulePairs);
            processes.add(pro);
        }
    }

    public void readProducts(JsonObject catData, List<Product> products, List<AkerProcess> processes) throws IOException {
        Map<String, AkerProcess> processUuids = processes.stream().collect(Collectors.toMap(AkerProcess::getUuid, p->p));
        for (JsonObject prodData : iterObjects(catData, "products")) {
            Product prod = new Product();
            prod.setName(stringFrom(prodData, "name"));
            prod.setDescription(stringFrom(prodData, "description"));
            prod.setUuid(stringFrom(prodData, "uuid"));
            prod.setProductVersion(intFrom(prodData, "product_version"));
            prod.setAvailability(intFrom(prodData, "availability"));
            prod.setBioType(stringFrom(prodData, "requested_biomaterial_type"));
            List<AkerProcess> productProcesses = new ArrayList<>();
            for (String uuid : asIter(streamStrings(prodData, "process_uuids"))) {
                AkerProcess pro = processUuids.get(uuid);
                if (pro==null) {
                    throw exception("Unknown process uuid: "+uuid);
                }
                productProcesses.add(pro);
            }
            prod.setProcesses(productProcesses);
            products.add(prod);
        }
    }
}
