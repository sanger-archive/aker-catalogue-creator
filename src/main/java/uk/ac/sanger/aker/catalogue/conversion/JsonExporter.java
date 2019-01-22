package uk.ac.sanger.aker.catalogue.conversion;

import uk.ac.sanger.aker.catalogue.model.*;

import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dr6
 */
public class JsonExporter extends JsonOutput {
    public JsonValue toExportData(Catalogue catalogue) {
        return createObjectBuilder()
                .add("catalogue", toJson(catalogue))
                .build();
    }

    public JsonValue toJson(Catalogue catalogue) {
        JsonArrayBuilder processArrayBuilder = createArrayBuilder();
        for (AkerProcess pro : catalogue.getProcesses()) {
            processArrayBuilder.add(toJson(pro));
        }
        JsonArrayBuilder productArrayBuilder = createArrayBuilder();
        for (Product prod : catalogue.getProducts()) {
            productArrayBuilder.add(toJson(prod));
        }
        return createObjectBuilder()
                .add("pipeline", catalogue.getPipeline())
                .add("url", catalogue.getUrl())
                .add("lims_id", catalogue.getLimsId())
                .add("processes", processArrayBuilder)
                .add("products", productArrayBuilder)
                .build();
    }

    public JsonValue toJson(AkerProcess process) {
        JsonArrayBuilder paramArrayBuilder = createArrayBuilder();
        JsonArrayBuilder pairArrayBuilder = createArrayBuilder();
        Set<String> moduleNames = new HashSet<>();
        for (ModulePair pair : process.getModulePairs()) {
            Module fromMod = pair.getFrom();
            Module toMod = pair.getTo();

            pairArrayBuilder.add(
                    createObjectBuilder()
                            .add("from_step", fromMod.serialisationName())
                            .add("to_step", toMod.serialisationName())
                            .add("default_path", pair.isDefaultPath())
            );

            if (toMod!=Module.END && moduleNames.add(toMod.getName()) && toMod.hasParameter()) {
                paramArrayBuilder.add(
                        createObjectBuilder()
                                .add("name", toMod.getName())
                                .add("min_value", toMod.getMinValue())
                                .add("max_value", toMod.getMaxValue())
                );
            }
        }
        return createObjectBuilder()
                .add("name", process.getName())
                .add("uuid", process.getUuid())
                .add("TAT", process.getTat())
                .add("process_class", process.getProcessClass())
                .add("module_parameters", paramArrayBuilder)
                .add("process_module_pairings", pairArrayBuilder)
                .build();
    }

    public JsonValue toJson(Product product) {
        JsonArrayBuilder processArrayBuilder = createArrayBuilder();
        for (AkerProcess pro : product.getProcesses()) {
            processArrayBuilder.add(pro.getUuid());
        }
        return createObjectBuilder()
                .add("name", product.getName())
                .add("description", product.getDescription())
                .add("uuid", product.getUuid())
                .add("product_version", product.getProductVersion())
                .add("availability", product.getAvailability())
                .add("requested_biomaterial_type", product.getBioType())
                .add("process_uuids", processArrayBuilder)
                .build();
    }
}
