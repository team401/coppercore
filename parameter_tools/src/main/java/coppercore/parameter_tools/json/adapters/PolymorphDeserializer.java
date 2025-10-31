package coppercore.parameter_tools.json.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A custom deserializer for handling polymorphic JSON deserialization. This deserializer uses a
 * {@link JsonType} annotation on the target class to determine the property that specifies the
 * subtype and the mapping of property values to specific subtypes.
 *
 * @param <T> the base type of the object to deserialize
 */
public class PolymorphDeserializer<T> implements JsonDeserializer<T> {

    /**
     * Deserializes a JSON element into an object of type T, determining the specific subtype based
     * on a property defined in the JsonType annotation.
     *
     * @param json the JSON element to deserialize
     * @param type the type of the object to deserialize to
     * @param context the deserialization context
     * @return the deserialized object of type T
     * @throws JsonParseException if deserialization fails
     */
    @Override
    public T deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            Class<?> typeClass = Class.forName(type.getTypeName());
            JsonType jsonType = typeClass.getDeclaredAnnotation(JsonType.class);
            String property = json.getAsJsonObject().get(jsonType.property()).getAsString();
            JsonSubtype[] subtypes = jsonType.subtypes();
            Type subType =
                    Arrays.stream(subtypes)
                            .filter(subtype -> subtype.name().equals(property))
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new)
                            .clazz();
            return context.deserialize(json, subType);
        } catch (JsonParseException | ClassNotFoundException e) {
            throw new JsonParseException("Failed deserialize json", e);
        }
    }
}
