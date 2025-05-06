package coppercore.parameter_tools.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import coppercore.parameter_tools.json.adapters.PolymorphDeserializer;
import edu.wpi.first.math.Pair;
import java.util.ArrayList;
import java.util.List;

/** Builder class for creating a JSONSyncConfig instance. */
public class JSONSyncConfigBuilder {
    public boolean serializeNulls = false;
    public boolean prettyPrinting = true;
    public boolean excludeFieldsWithoutExposeAnnotation = false;
    public boolean primitiveChecking = true;
    public boolean primitiveCheckPrintAlert = false;
    public boolean primitiveCheckCrash = true;
    public FieldNamingPolicy namingPolicy = FieldNamingPolicy.IDENTITY;
    public LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;
    private List<Pair<Class, Object>> typeAdapters = new ArrayList<>();
    private List<TypeAdapterFactory> typeAdapterFactories = new ArrayList<>();

    /**
     * Sets whether null fields should be serialized.
     *
     * @param serializeNulls True to serialize null fields, false otherwise.
     * @return The builder instance.
     */
    public JSONSyncConfigBuilder setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
        return this;
    }

    /**
     * Sets whether the JSON output should use pretty printing.
     *
     * @param prettyPrinting True to enable pretty printing, false otherwise.
     * @return The builder instance.
     */
    public JSONSyncConfigBuilder setPrettyPrinting(boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
        return this;
    }

    /**
     * Sets whether fields without @Expose annotations should be excluded.
     *
     * @param excludeFieldsWithoutExposeAnnotation True to exclude fields, false otherwise.
     * @return The builder instance.
     */
    public JSONSyncConfigBuilder setExcludeFieldsWithoutExposeAnnotation(
            boolean excludeFieldsWithoutExposeAnnotation) {
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
        return this;
    }

    /**
     * Sets the naming policy for fields in the JSON output.
     *
     * @param namingPolicy The field naming policy.
     * @return The builder instance.
     */
    public JSONSyncConfigBuilder setNamingPolicy(FieldNamingPolicy namingPolicy) {
        this.namingPolicy = namingPolicy;
        return this;
    }

    /**
     * Sets the serialization policy for long values.
     *
     * @param longSerializationPolicy The serialization policy for long values.
     * @return The builder instance.
     */
    public JSONSyncConfigBuilder setLongSerializationPolicy(
            LongSerializationPolicy longSerializationPolicy) {
        this.longSerializationPolicy = longSerializationPolicy;
        return this;
    }

    public <T> JSONSyncConfigBuilder addJsonDeserializer(
            Class<T> clazz, JsonDeserializer<T> adapter) {
        typeAdapters.add(new Pair<>(clazz, adapter));
        return this;
    }

    public <T> JSONSyncConfigBuilder addJsonSerializer(Class<T> clazz, JsonSerializer<T> adapter) {
        typeAdapters.add(new Pair<>(clazz, adapter));
        return this;
    }

    public <T> JSONSyncConfigBuilder addJsonTypeAdapter(Class<T> clazz, TypeAdapter<T> adapter) {
        typeAdapters.add(new Pair<>(clazz, adapter));
        return this;
    }

    public <T> JSONSyncConfigBuilder setUpPolymorphAdapter(Class<T> clazz) {
        return addJsonDeserializer(clazz, new PolymorphDeserializer<>());
    }

    /**
     * Builds the configuration object.
     *
     * @return A JSONSyncConfig instance.
     */
    public JSONSyncConfig build() {
        return new JSONSyncConfig(
                serializeNulls,
                prettyPrinting,
                excludeFieldsWithoutExposeAnnotation,
                namingPolicy,
                longSerializationPolicy,
                primitiveChecking,
                primitiveCheckPrintAlert,
                primitiveCheckCrash,
                typeAdapters,
                typeAdapterFactories);
    }

    public void setPrimitiveChecking(boolean primitiveChecking) {
        this.primitiveChecking = primitiveChecking;
    }

    public void setPrimitiveCheckPrintAlert(boolean primitiveCheckPrintAlert) {
        this.primitiveCheckPrintAlert = primitiveCheckPrintAlert;
    }

    public void setPrimitiveCheckCrash(boolean primitiveCheckCrash) {
        this.primitiveCheckCrash = primitiveCheckCrash;
    }
}
