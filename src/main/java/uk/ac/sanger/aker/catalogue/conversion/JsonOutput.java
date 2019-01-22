package uk.ac.sanger.aker.catalogue.conversion;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author dr6
 */
public class JsonOutput {
    private JsonBuilderFactory builderFactory;
    private JsonWriterFactory writerFactory;

    public JsonOutput(JsonBuilderFactory builderFactory, JsonWriterFactory writerFactory) {
        this.builderFactory = builderFactory;
        this.writerFactory = writerFactory;
    }

    public JsonOutput() {
        this(Json.createBuilderFactory(null),
                Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true)));
    }

    protected JsonBuilderFactory getBuilderFactory() {
        return this.builderFactory;
    }

    protected JsonObjectBuilder createObjectBuilder() {
        return getBuilderFactory().createObjectBuilder();
    }

    protected JsonArrayBuilder createArrayBuilder() {
        return getBuilderFactory().createArrayBuilder();
    }

    public void write(JsonValue value, Path path) throws IOException {
        try (JsonWriter out = writerFactory.createWriter(Files.newBufferedWriter(path))) {
            out.write(value);
        }
    }
}
