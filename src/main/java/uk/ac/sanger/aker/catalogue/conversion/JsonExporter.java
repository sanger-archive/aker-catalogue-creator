package uk.ac.sanger.aker.catalogue.conversion;

import uk.ac.sanger.aker.catalogue.model.*;

import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import java.util.HashSet;
import java.util.Set;

/**
 * This tool converts a catalogue to JSON.
 * @author dr6
 */
public class JsonExporter extends JsonOutput {
    /**
     * Create a JSON representation of a catalogue, ready to be saved to a file.
     * At the top level this is an object with one key, {@code "catalogue"},
     * whose value is given by {@link #toJson(Catalogue)}
     * @param catalogue the catalogue to export
     * @return a {@code JsonValue} that can be written to a file
     */
    public JsonValue toExportData(Catalogue catalogue) {
        return createObjectBuilder()
                .add("catalogue", toJson(catalogue))
                .build();
    }

    /**
     * A JSON version of a catalogue.
     * The catalogue data includes details of its products and processes.
     * @param catalogue the catalogue to serialise
     * @return a {@code JsonValue} representing the information in the catalogue
     */
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

    /**
     * A JSON version of a process.
     * The process data includes details of its modules and parameters.
     * @param process the process to serialise
     * @return a {@code JsonValue} representing the information in the process
     */
    public JsonValue toJson(AkerProcess process) {
        JsonArrayBuilder paramArrayBuilder = createArrayBuilder();
        JsonArrayBuilder pairArrayBuilder = createArrayBuilder();
        Set<String> moduleNames = new HashSet<>();
        for (ModulePair pair : process.getModulePairs()) {
            Module fromMod = pair.getFrom();
            Module toMod = pair.getTo();

            pairArrayBuilder.add(
                    createObjectBuilder()
                            .add("from_step", nullable(fromMod.serialisationName()))
                            .add("to_step", nullable(toMod.serialisationName()))
                            .add("default_path", pair.isDefaultPath())
            );

            if (toMod!=Module.END && moduleNames.add(toMod.getName()) && toMod.hasParameter()) {
                paramArrayBuilder.add(
                        createObjectBuilder()
                                .add("name", toMod.getName())
                                .add("min_value", nullable(toMod.getMinValue()))
                                .add("max_value", nullable(toMod.getMaxValue()))
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

    /**
     * A JSON version of a product.
     * The product data includes uuids of processes.
     * @param product the product to serialise
     * @return a {@code JsonValue} representing the information in the product
     */
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
