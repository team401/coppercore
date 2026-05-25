package coppercore.parameter_tools.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.adapters.OptionalTypeAdapterFactory;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionalTypeAdapterFactoryTests {
    private static final Type OPTIONAL_STRING_TYPE = new TypeToken<Optional<String>>() {}.getType();
    private static final Type OPTIONAL_INTEGER_LIST_TYPE =
            new TypeToken<Optional<List<Integer>>>() {}.getType();

    private final Gson gson =
            new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                    .create();

    private static class OptionalValues {
        public Optional<String> present = Optional.of("value");
        public Optional<String> empty = Optional.empty();
        public Optional<List<Integer>> numbers = Optional.of(List.of(1, 2, 3));
    }

    @Test
    public void OptionalOfSerializesAsInnerValue() {
        String json = gson.toJson(Optional.of("present"), OPTIONAL_STRING_TYPE);

        Assertions.assertEquals("\"present\"", json);
    }

    @Test
    public void OptionalEmptySerializesAsNull() {
        String json = gson.toJson(Optional.empty(), OPTIONAL_STRING_TYPE);

        Assertions.assertEquals("null", json);
    }

    @Test
    public void NullOptionalReferenceSerializesAsNull() {
        String json = gson.toJson(null, OPTIONAL_STRING_TYPE);

        Assertions.assertEquals("null", json);
    }

    @Test
    public void InnerJsonValueDeserializesAsPresentOptional() {
        Optional<String> value = gson.fromJson("\"present\"", OPTIONAL_STRING_TYPE);

        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals("present", value.get());
    }

    @Test
    public void JsonNullDeserializesAsEmptyOptional() {
        Optional<String> value = gson.fromJson("null", OPTIONAL_STRING_TYPE);

        Assertions.assertTrue(value.isEmpty());
    }

    @Test
    public void OptionalFieldValuesRoundTripInsideObject() {
        OptionalValues value =
                gson.fromJson(gson.toJson(new OptionalValues()), OptionalValues.class);

        Assertions.assertEquals(Optional.of("value"), value.present);
        Assertions.assertTrue(value.empty.isEmpty());
        Assertions.assertEquals(Optional.of(List.of(1, 2, 3)), value.numbers);
    }

    @Test
    public void ParameterizedInnerTypeRoundTrips() {
        Optional<List<Integer>> value =
                gson.fromJson(
                        gson.toJson(Optional.of(List.of(4, 5)), OPTIONAL_INTEGER_LIST_TYPE),
                        OPTIONAL_INTEGER_LIST_TYPE);

        Assertions.assertEquals(Optional.of(List.of(4, 5)), value);
    }

    @Test
    public void JsonSyncDefaultConfigRegistersOptionalAdapter() {
        JSONSync<OptionalValues> sync = new JSONSync<>(new OptionalValues(), "OptionalValues.json");

        OptionalValues value = sync.deserialize("{\"present\":\"updated\",\"empty\":null}");

        Assertions.assertEquals(Optional.of("updated"), value.present);
        Assertions.assertTrue(value.empty.isEmpty());
    }
}
