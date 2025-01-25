package coppercore.parameter_tools.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.LongSerializationPolicy;

/** Builder class for creating a JSONSyncConfig instance. */
public class JSONSyncConfigBuilder {
    public boolean serializeNulls = false;
    public boolean prettyPrinting = false;
    public boolean excludeFieldsWithoutExposeAnnotation = false;
    public FieldNamingPolicy namingPolicy = FieldNamingPolicy.IDENTITY;
    public LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;

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

    /**
     * Builds the configuration object.
     *
     * @return A JSONSyncConfig instance.
     */
    public JSONSyncConfig build() {
        return new JSONSyncConfig(this);
    }
}
