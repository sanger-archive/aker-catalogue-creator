package uk.ac.sanger.aker.catalogue.conversion;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * A tool with a set of general methods for creating JSON objects.
 * This is a base class for a custom JSON exporting class.
 * @author dr6
 */
public class JsonOutput {
    private JsonBuilderFactory builderFactory;
    private JsonWriterFactory writerFactory;

    /**
     * Constructs a JsonOutput object using the given factories to create builders and writers.
     */
    public JsonOutput(JsonBuilderFactory builderFactory, JsonWriterFactory writerFactory) {
        this.builderFactory = builderFactory;
        this.writerFactory = writerFactory;
    }

    /**
     * Constructs a JsonOutput object using default factories
     */
    public JsonOutput() {
        this(Json.createBuilderFactory(null),
                Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true)));
    }

    /** Gets the builder factory. */
    protected JsonBuilderFactory getBuilderFactory() {
        return this.builderFactory;
    }

    /** Create a builder for a new object */
    protected JsonObjectBuilder createObjectBuilder() {
        return getBuilderFactory().createObjectBuilder();
    }

    /** Create a builder for a new array */
    protected JsonArrayBuilder createArrayBuilder() {
        return getBuilderFactory().createArrayBuilder();
    }

    /**
     * Write the given JSON to a file at the given path
     * @param value the value to write
     * @param path the path to write to
     * @exception IOException the file could not be written
     * @exception JsonException the JSON could not be serialised
     */
    public void write(JsonValue value, Path path) throws IOException {
        try (JsonWriter out = writerFactory.createWriter(Files.newBufferedWriter(path))) {
            out.write(value);
        }
    }

    /**
     * Convert a string to {@link JsonValue#NULL} if null, or a new {@link JsonString} if non-null
     * @param string a string that might be null
     * @return {@link JsonValue#NULL} if {@code string} is null, otherwise a new {@link JsonString} object
     */
    public static JsonValue nullable(String string) {
        return (string==null ? JsonValue.NULL : Json.createValue(string));
    }

    /**
     * Convert an Integer to {@link JsonValue#NULL} if null, or a new {@link JsonNumber} if non-null
     * @param number an integer that might be null
     * @return {@link JsonValue#NULL} if {@code number} is null, otherwise a new {@link JsonNumber} object
     */
    public static JsonValue nullable(Integer number) {
        return (number==null ? JsonValue.NULL : Json.createValue(number));
    }
}
