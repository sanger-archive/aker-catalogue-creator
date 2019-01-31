package uk.ac.sanger.aker.catalogue.conversion;

import javax.json.*;
import javax.json.JsonValue.ValueType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A tool with a set of general methods for reading and understanding JSON.
 * This is a base class for a custom JSON importing class.
 * @author dr6
 */
public class JsonInput {
    private JsonReaderFactory readerFactory;

    /**
     * Constructs a JsonInput object that will use a given {@code readerFactory}
     * @param readerFactory the factory to use to create {@link JsonReader}s
     */
    public JsonInput(JsonReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    /**
     * Constructs a JsonInput object using a default {@code readerFactory}
     */
    public JsonInput() {
        this(Json.createReaderFactory(null));
    }

    /**
     * Reads the given path and parses the file contents as JSON.
     * @param path the path of the file
     * @return the parsed JSON data of the file
     * @exception IOException the file could not be read
     * @exception JsonException the JSON data could not be constructed from the file
     */
    public JsonValue readPath(Path path) throws IOException {
        try (JsonReader reader = readerFactory.createReader(Files.newBufferedReader(path))) {
            return reader.readValue();
        }
    }

    /**
     * Gets the value for the given key in the given object.
     * @param jo the json object
     * @param key the key to look up
     * @return the value, which must be non-null
     * @exception IOException if the value is missing or null
     */
    protected JsonValue valueFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = jo.get(key);
        if (value==null) {
            throw exception("Missing key: \""+key+"\"");
        }
        return value;
    }

    /**
     * Gets the int value for the given key in the given object.
     * If the value is a string that can be parsed as an int, the parsed int will be returned
     * @param jo the json object
     * @param key the key to look up
     * @return the int value for the given key in the given object
     * @exception IOException if the value is missing or not an int
     */
    protected int intFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        return toInt(value, key);
    }

    /**
     * Gets the Integer value for the given key in the given object.
     * If the value is a string that can be parsed as an int, the parsed int will be returned.
     * If there is no such value, or if it is null, this method will return null.
     * @param jo the json object
     * @param key the key to look up
     * @return the Integer value for the given key in the given object, or null if there is no such value
     * @exception IOException if the value is present but cannot be read as an integer
     */
    protected Integer integerFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = jo.get(key);
        if (value==null || value.getValueType()==ValueType.NULL) {
            return null;
        }
        return toInt(value, key);
    }

    private int toInt(JsonValue value, String key) throws IOException {
        if (value.getValueType()==ValueType.STRING) {
            try {
                return Integer.parseInt(((JsonString) value).getString());
            } catch (NumberFormatException e) {
                throw exception("Expected an integer for key "+key+" but got non-integer string.", e);
            }
        }
        if (value.getValueType()!=ValueType.NUMBER) {
            throw exception("Expected an integer from key "+key+" but got "+value.getValueType());
        }
        JsonNumber jnum = (JsonNumber) value;
        if (!jnum.isIntegral()) {
            throw exception("Expected an integer from key "+key+" but got "+jnum);
        }
        return jnum.intValue();
    }

    /**
     * Gets the string value for the given key in the given object.
     * The key must be present, and the value must be a string.
     * @param jo the json object
     * @param key the key to look up
     * @return the string associated with the given key
     * @exception IOException if the key is missing, or its value is not a string
     */
    protected String stringFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonString)) {
            throw exception("Expected string for key "+key+" but got "+value.getValueType());
        }
        return ((JsonString) value).getString();
    }

    /**
     * Gets JSON object value for the given key inside the given object.
     * The key must be present, and the value must be a JSON object
     * @param jo the json object
     * @param key the key to look up
     * @return the JSON object associated with the given key
     * @exception IOException if the key is missing, or its value is not a JSON object
     */
    protected JsonObject objectFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonObject)) {
            throw exception("Expected JSON object from key "+key+" but got "+value.getValueType());
        }
        return value.asJsonObject();
    }

    /**
     * Gets JSON array value for the given key inside the given object.
     * The key must be present, and the value must be an array
     * @param jo the json object
     * @param key the key to look up
     * @return the JSON array associated with the given key
     * @exception IOException if the key is missing, or its value is not a JSON array
     */
    protected JsonArray arrayFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonArray)) {
            throw exception("Expected JSON array from key "+key+" but got "+value.getValueType());
        }
        return value.asJsonArray();
    }

    /**
     * Stream the objects from an array in the given object.
     * Using the stream may produce {@link UncheckedIOException} if any element of the array is not a JSON object.
     * @param jsonObject the object containing the array
     * @param fieldName the key to look up
     * @return a stream of JSON objects inside the array
     * @exception IOException if the key is missing or if its value is not a JSON array
     */
    protected Stream<JsonObject> streamObjects(JsonObject jsonObject, final String fieldName) throws IOException {
        JsonArray jsonArray = arrayFrom(jsonObject, fieldName);
        return jsonArray.stream()
                .map(obj -> {
                    if (obj == null) {
                        throw unchecked(exception("null in " + fieldName));
                    }
                    if (obj.getValueType() != ValueType.OBJECT) {
                        throw unchecked(exception("expected object in " + fieldName + " but got " + obj.getValueType()));
                    }
                    return obj.asJsonObject();
                });
    }

    /**
     * Stream the strings from an array in the given object.
     * Using the stream may produce {@link UncheckedIOException} if any element of the array is not a string.
     * @param jsonObject the object containing the array
     * @param fieldName the key to look up
     * @return a stream of strings inside the array
     * @exception IOException if the key is missing or if its value is not a JSON array
     */
    protected Stream<String> streamStrings(JsonObject jsonObject, String fieldName) throws IOException {
        JsonArray jsonArray = arrayFrom(jsonObject, fieldName);
        try {
            return jsonArray.stream()
                    .map(obj -> {
                        if (obj==null) {
                            throw unchecked(exception("null in "+fieldName));
                        }
                        if (obj.getValueType()!=ValueType.STRING) {
                            throw unchecked(exception("expected string in "+fieldName+" but got "+obj.getValueType()));
                        }
                        return ((JsonString) obj).getString();
                    });
        } catch (UncheckedIOException ue) {
            throw ue.getCause();
        }
    }

    /**
     * Gets an iterable of the objects from an array in the given object.
     * Iterating may produce {@link UncheckedIOException} if any element of the array is not a JSON object.
     * @param jsonObject the object containing the array
     * @param fieldName the key to look up
     * @return an iterable of JSON objects inside the array
     * @exception IOException if the key is missing or if its value is not a JSON array
     */
    protected Iterable<JsonObject> iterObjects(JsonObject jsonObject, String fieldName) throws IOException {
        return asIter(streamObjects(jsonObject, fieldName));
    }

    /**
     * Gets an iterable of the strings from an array in the given object.
     * Iterating may produce {@link UncheckedIOException} if any element of the array is not a string.
     * @param jsonObject the object containing the array
     * @param fieldName the key to look up
     * @return an iterable of strings inside the array
     * @exception IOException if the key is missing or if its value is not a JSON array
     */
    protected Iterable<String> iterStrings(JsonObject jsonObject, String fieldName) throws IOException {
        return asIter(streamStrings(jsonObject, fieldName));
    }

    /** Interpret a stream as an {@code Iterable} */
    protected static <T> Iterable<T> asIter(Stream<T> stream) {
        return stream::iterator;
    }

    /** Constructs an {@code IOException} with the given message */
    protected IOException exception(String message) {
        return new IOException(message);
    }
    /** Constructs an {@code IOException} with the given message and cause */
    protected IOException exception(String message, Exception cause) {
        return new IOException(message, cause);
    }
    /** Wraps an {@code IOException} in an {@code UncheckedIOException}*/
    protected UncheckedIOException unchecked(IOException ex) {
        return new UncheckedIOException(ex);
    }
}
