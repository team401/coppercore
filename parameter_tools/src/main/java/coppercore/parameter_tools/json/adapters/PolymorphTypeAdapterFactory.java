package coppercore.parameter_tools.json.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A Gson factory for classes annotated with {@link JsonType}.
 *
 * <p>This is registered as a {@link TypeAdapterFactory} so polymorphic fields nested inside maps,
 * lists, arrays, or other objects are dispatched before Gson tries to instantiate an abstract base
 * class reflectively.
 */
public class PolymorphTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        JsonType typeAnnotation = rawType.getAnnotation(JsonType.class);
        if (typeAnnotation == null) {
            return null;
        }

        return new TypeAdapter<>() {
            @Override
            public T read(JsonReader reader) throws IOException {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement == null || jsonElement.isJsonNull()) {
                    return null;
                }
                if (!jsonElement.isJsonObject()) {
                    throw new JsonParseException(
                            "Expected JSON object for polymorphic type " + rawType.getName());
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement discriminator = jsonObject.get(typeAnnotation.property());
                if (discriminator == null || discriminator.isJsonNull()) {
                    throw new JsonParseException(
                            "Missing polymorphic discriminator "
                                    + typeAnnotation.property()
                                    + " for "
                                    + rawType.getName());
                }

                String discriminatorValue = discriminator.getAsString();
                Type targetType =
                        Arrays.stream(typeAnnotation.subtypes())
                                .filter(subtype -> subtype.name().equals(discriminatorValue))
                                .findFirst()
                                .map(JsonSubtype::clazz)
                                .orElseThrow(
                                        () ->
                                                new JsonParseException(
                                                        "Unknown subtype "
                                                                + discriminatorValue
                                                                + " for "
                                                                + rawType.getName()));

                return gson.fromJson(jsonElement, targetType);
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                gson.toJson(value, value.getClass(), out);
            }
        };
    }
}
