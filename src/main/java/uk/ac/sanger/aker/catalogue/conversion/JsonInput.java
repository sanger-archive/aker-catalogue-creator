package uk.ac.sanger.aker.catalogue.conversion;

import javax.json.*;
import javax.json.JsonValue.ValueType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author dr6
 */
public class JsonInput {
    private JsonReaderFactory readerFactory;

    public JsonInput(JsonReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    public JsonInput() {
        this(Json.createReaderFactory(null));
    }

    public JsonValue readPath(Path path) throws IOException {
        try (JsonReader reader = readerFactory.createReader(Files.newBufferedReader(path))) {
            return reader.readValue();
        }
    }

    protected JsonValue valueFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = jo.get(key);
        if (value==null) {
            throw exception("Missing key: \""+key+"\"");
        }
        return value;
    }

    protected int intFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        return toInt(value, key);
    }

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

    protected String stringFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonString)) {
            throw exception("Expected string for key "+key+" but got "+value.getValueType());
        }
        return ((JsonString) value).getString();
    }

    protected JsonObject objectFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonObject)) {
            throw exception("Expected JSON object from key "+key+" but got "+value.getValueType());
        }
        return value.asJsonObject();
    }

    protected JsonArray arrayFrom(JsonObject jo, String key) throws IOException {
        JsonValue value = valueFrom(jo, key);
        if (!(value instanceof JsonArray)) {
            throw exception("Expected JSON array from key "+key+" but got "+value.getValueType());
        }
        return value.asJsonArray();
    }

    protected Stream<JsonObject> streamObjects(JsonObject jsonObject, final String fieldName) throws IOException {
        JsonArray jsonArray = arrayFrom(jsonObject, fieldName);
        try {
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
        } catch (UncheckedIOException ue) {
            throw ue.getCause();
        }
    }

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

    protected Iterable<JsonObject> iterObjects(JsonObject jsonObject, String fieldName) throws IOException {
        return asIter(streamObjects(jsonObject, fieldName));
    }

    protected Iterable<String> iterStrings(JsonObject jsonObject, String fieldName) throws IOException {
        return asIter(streamStrings(jsonObject, fieldName));
    }

    protected static <T> Iterable<T> asIter(Stream<T> stream) {
        return stream::iterator;
    }

    protected IOException exception(String message) {
        return new IOException(message);
    }
    protected IOException exception(String message, Exception cause) {
        return new IOException(message, cause);
    }
    protected UncheckedIOException unchecked(IOException ex) {
        return new UncheckedIOException(ex);
    }
}
