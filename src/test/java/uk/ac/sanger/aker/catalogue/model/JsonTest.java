package uk.ac.sanger.aker.catalogue.model;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.ac.sanger.aker.catalogue.conversion.JsonExporter;
import uk.ac.sanger.aker.catalogue.conversion.JsonImporter;

import javax.json.JsonValue;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link JsonImporter} and {@link JsonExporter}
 * @author dr6
 */
@Test
public class JsonTest {
    private JsonImporter jim = new JsonImporter();
    private JsonExporter jex = new JsonExporter();

    private JsonValue catalogueData;
    private Catalogue catalogue;

    @BeforeClass
    private void loadCatalogue() throws IOException, URISyntaxException {
        catalogueData = fileJson("catalogue.json");
        catalogue = jim.importCatalogue(catalogueData);
    }

    private JsonValue fileJson(String filename) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(filename);
        assert resource!=null;
        Path path = Paths.get(resource.toURI());
        return jim.readPath(path);
    }

    public void testLoadFile() throws URISyntaxException, IOException {
        assertEquals(jex.toExportData(catalogue), catalogueData);
        JsonValue variantData = fileJson("variant_catalogue.json");
        // The variant version has some missing fields that will be filled in
        Catalogue catalogueFromVariant = jim.importCatalogue(variantData);
        assertEquals(jex.toExportData(catalogueFromVariant), catalogueData);
    }

    public void testCatalogueFields() {
        assertEquals(catalogue.getPipeline(), "My pipeline");
        assertEquals(catalogue.getUrl(), "http://localhost:3400");
        assertEquals(catalogue.getLimsId(), "The LIMS");
    }

    public void testModules() {
        String[] moduleNames = { "Quantification", "Fluidigm", "Singleplex pooling", "Multiplex pooling",
                "NovaSeq", "HiSeq X" };
        Integer[][] params = new Integer[6][2];
        params[3][0] = 2;
        params[3][1] = 96;
        params[5][0] = 0;
        List<Module> modules = catalogue.getModules();
        assertEquals(modules.size(), moduleNames.length);
        for (int i = 0; i < moduleNames.length; ++i) {
            Module module = modules.get(i);
            assertEquals(module.getName(), moduleNames[i]);
            assertEquals(module.getMinValue(), params[i][0]);
            assertEquals(module.getMaxValue(), params[i][1]);
            assertEquals(module.hasParameter(), (params[i][0]!=null || params[i][1]!=null));
        }
    }

    public void testProcesses() {
        String[] processNames = { "Quality Control (GBS)", "20x Human Whole Genome Sequencing (HWGS)",
                "30x Human Whole Genome Sequencing (HWGS)" };
        String[] processUuids = {"16a0c919-0f7e-4e23-a2e0-915bb29fcc1d", "2dee6168-d310-4005-9c45-904ace2c6295",
                "e4ee2122-495e-49da-be8a-b47ea4314584"};
        int[] tats = {5, 42, 42};
        String[] proClasses = { "genotyping", "sequencing", "sequencing" };

        List<AkerProcess> pros = catalogue.getProcesses();
        assertEquals(pros.size(), processNames.length);
        for (int i = 0; i < processNames.length; ++i) {
            AkerProcess pro = pros.get(i);
            assertEquals(pro.getName(), processNames[i]);
            assertEquals(pro.getUuid(), processUuids[i]);
            assertEquals(pro.getTat(), tats[i]);
            assertEquals(pro.getProcessClass(), proClasses[i]);
        }

        List<Module> modules = catalogue.getModules();
        Module quant = modules.get(0);
        Module fluid = modules.get(1);
        List<ModulePair> pairs = pros.get(0).getModulePairs();
        Module[] from = { Module.START, Module.START, quant, quant, fluid };
        Module[] to = { quant, fluid, Module.END, fluid, Module.END };
        boolean[] defaultPath = { true, false, false, true, true };
        assertEquals(pairs.size(), from.length);
        for (int i = 0; i < from.length; ++i) {
            ModulePair pair = pairs.get(i);
            assertEquals(pair.getFrom(), from[i]);
            assertEquals(pair.getTo(), to[i]);
            assertEquals(pair.isDefaultPath(), defaultPath[i]);
        }
    }

    public void testProducts() {
        String[] productUuids = { "5801409a-1623-491e-89cb-90919fa17bf4", "614f9f8e-ae08-4947-9df8-73b9d18ca503" };
        String[] bioTypes = { "dna/rna", "cake" };
        String[] numxs = { "20x", "30x" };
        List<AkerProcess> pros = catalogue.getProcesses();
        AkerProcess qc = pros.get(0);
        AkerProcess[] seq = { pros.get(1), pros.get(2) };
        List<Product> products = catalogue.getProducts();
        assertEquals(products.size(), productUuids.length);
        for (int i = 0; i < products.size(); ++i) {
            Product product = products.get(i);
            String numx = numxs[i];
            assertEquals(product.getName(), "Quality Control with "+numx);
            assertEquals(product.getDescription(), numx+" Human Whole Genome Sequencing");
            assertEquals(product.getUuid(), productUuids[i]);
            assertEquals(product.getProductVersion(), 1+i);
            assertEquals(product.getAvailability(), i);
            assertEquals(product.getBioType(), bioTypes[i]);
            assertEquals(product.getProcesses().size(), 2);
            assertEquals(product.getProcesses().get(0), qc);
            assertEquals(product.getProcesses().get(1), seq[i]);
        }
    }

}
