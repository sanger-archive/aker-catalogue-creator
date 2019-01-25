package uk.ac.sanger.aker.catalogue.model;

import org.testng.annotations.Test;
import uk.ac.sanger.aker.catalogue.conversion.JsonExporter;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;

import javax.json.*;
import java.io.IOException;
import java.util.*;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.testng.Assert.*;

/**
 * Tests for {@link JsonImporter} and {@link JsonExporter}
 * @author dr6
 */
@Test
public class JsonTest {
    private JsonImporter jim = new JsonImporter();
    private JsonExporter jex = new JsonExporter();

    public void testSimpleSerialisation() throws IOException {
        JsonObject catalogueObject = createObjectBuilder()
                .add("pipeline", "My pipeline")
                .add("url", "http://url")
                .add("lims_id", "Beep")
                .add("processes", createArrayBuilder())
                .add("products", createArrayBuilder())
                .build();
        JsonObject jsonData = createObjectBuilder()
                .add("catalogue", catalogueObject)
                .build();
        Catalogue catalogue = jim.importCatalogue(jsonData);
        assertEquals(catalogue.getPipeline(), "My pipeline");
        assertEquals(catalogue.getUrl(), "http://url");
        assertEquals(catalogue.getLimsId(), "Beep");
        assertEquals(catalogue.getProcesses(), Collections.emptyList());
        assertEquals(catalogue.getProducts(), Collections.emptyList());

        JsonValue output = jex.toExportData(catalogue);
        assertEquals(output, jsonData);
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    public void testSerialisationWithModuleParameters() throws IOException {
        JsonObject processObject = createObjectBuilder()
                .add("name", "processName")
                .add("uuid", uuid())
                .add("TAT", 12)
                .add("process_class", "top")
                .add("module_parameters", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("name", "module0")
                                .add("min_value", 2)
                                .add("max_value", 96)
                        ).add(createObjectBuilder()
                                .add("name", "module1")
                                .add("min_value", 5)
                                .addNull("max_value")
                        )
                )
                .add("process_module_pairings", createArrayBuilder()
                        .add(createObjectBuilder()
                                .addNull("from_step")
                                .add("to_step", "module0")
                                .add("default_path", true)
                        ).add(createObjectBuilder()
                                .add("from_step", "module0")
                                .add("to_step", "module1")
                                .add("default_path", true)
                        ).add(createObjectBuilder()
                                .add("from_step", "module1")
                                .addNull("to_step")
                                .add("default_path", true)
                        ).add(createObjectBuilder()
                                .add("from_step", "module1")
                                .add("to_step", "module2")
                                .add("default_path", false)
                        ).add(createObjectBuilder()
                                .add("from_step", "module2")
                                .addNull("to_step")
                                .add("default_path", false)
                        )
                ).build();
        JsonObject catalogueObject = createObjectBuilder()
                .add("pipeline", "My pipeline")
                .add("url", "http://url")
                .add("lims_id", "Beep")
                .add("processes", createArrayBuilder().add(processObject))
                .add("products", createArrayBuilder())
                .build();
        JsonObject jsonData = createObjectBuilder()
                .add("catalogue", catalogueObject)
                .build();

        Catalogue catalogue = jim.importCatalogue(jsonData);
        assertEquals(catalogue.getProcesses().size(), 1);
        AkerProcess process = catalogue.getProcesses().get(0);

        assertEquals(process.getName(), "processName");
        assertEquals(process.getUuid(), processObject.getString("uuid"));
        assertEquals(process.getTat(), 12);
        assertEquals(process.getProcessClass(), "top");
        assertEquals(process.getModulePairs().size(), 5);
        List<Module> modules = catalogue.getModules();
        assertEquals(modules.size(), 3);
        assertEquals(modules.get(0).getName(), "module0");
        assertEquals(modules.get(1).getName(), "module1");
        assertEquals(modules.get(2).getName(), "module2");
        assertTrue(modules.get(0).hasParameter());
        assertTrue(modules.get(1).hasParameter());
        assertFalse(modules.get(2).hasParameter());
        assertEquals(modules.get(0).getMinValue(), (Integer) 2);
        assertEquals(modules.get(0).getMaxValue(), (Integer) 96);
        assertEquals(modules.get(1).getMinValue(), (Integer) 5);
        assertNull(modules.get(2).getMaxValue());

        Integer[] expectedFrom = { null, 0, 1, 1, 2 };
        Integer[] expectedTo = { 0, 1, null, 2, null };
        boolean[] expectedDefault = { true, true, true, false, false };
        for (int i = 0; i < process.getModulePairs().size(); ++i) {
            ModulePair pair = process.getModulePairs().get(i);
            assertEquals(pair.getFrom(), expectedFrom[i]==null ? Module.START : modules.get(expectedFrom[i]));
            assertEquals(pair.getTo(), expectedTo[i]==null ? Module.END : modules.get(expectedTo[i]));
            assertEquals(pair.isDefaultPath(), expectedDefault[i]);
        }

        JsonValue output = jex.toExportData(catalogue);
        assertEquals(output, jsonData);
    }

    public void testSerialisationWithProduct() throws IOException {
        String uuid0 = uuid();
        String uuid1 = uuid();
        JsonArray processArray = createArrayBuilder()
                .add(createObjectBuilder()
                        .add("name", "process0")
                        .add("uuid", uuid0)
                        .add("TAT", 10)
                        .add("process_class", "top")
                        .add("module_parameters", JsonValue.EMPTY_JSON_ARRAY)
                        .add("process_module_pairings",
                                createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .addNull("from_step")
                                                .addNull("to_step")
                                                .add("default_path", true)
                                        )
                        )
                ).add(createObjectBuilder()
                        .add("name", "process1")
                        .add("uuid", uuid1)
                        .add("TAT", 11)
                        .add("process_class", "bottom")
                        .add("module_parameters", JsonValue.EMPTY_JSON_ARRAY)
                        .add("process_module_pairings",
                                createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .addNull("from_step")
                                                .addNull("to_step")
                                                .add("default_path", true)
                                        )
                        )
                ).build();
        JsonObject productObject = createObjectBuilder()
                .add("name", "my product")
                .add("description", "my description")
                .add("uuid", uuid())
                .add("product_version", 10)
                .add("availability", 0)
                .add("requested_biomaterial_type", "biotype")
                .add("process_uuids", createArrayBuilder().add(uuid0).add(uuid1))
                .build();

        JsonObject catalogueObject = createObjectBuilder()
                .add("pipeline", "My pipeline")
                .add("url", "http://url")
                .add("lims_id", "Beep")
                .add("processes", processArray)
                .add("products", createArrayBuilder().add(productObject))
                .build();
        JsonObject jsonData = createObjectBuilder()
                .add("catalogue", catalogueObject)
                .build();

        Catalogue catalogue = jim.importCatalogue(jsonData);
        assertEquals(catalogue.getProducts().size(), 1);
        List<AkerProcess> processes = catalogue.getProcesses();
        assertEquals(processes.size(), 2);
        Product product = catalogue.getProducts().get(0);
        assertEquals(product.getName(), "my product");
        assertEquals(product.getDescription(), "my description");
        assertEquals(product.getUuid(), productObject.getString("uuid"));
        assertEquals(product.getProductVersion(), 10);
        assertEquals(product.getAvailability(), 0);
        assertEquals(product.getBioType(), "biotype");
        assertEquals(product.getProcesses(), processes);

        JsonValue output = jex.toExportData(catalogue);
        assertEquals(output, jsonData);
    }
}
