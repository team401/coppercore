package coppercore.parameter_tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.LongSerializationPolicy;


public class JSONSyncConfigBuilder {
    public boolean serializeNulls = false;
    public boolean prettyPrinting = false;
    public boolean excludeFieldsWithoutExposeAnnotation = false;
    public FieldNamingPolicy namingPolicy = FieldNamingPolicy.IDENTITY;
    public LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;

    public JSONSyncConfigBuilder setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
        return this;
    }

    public JSONSyncConfigBuilder setPrettyPrinting(boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
        return this;
    }

    public JSONSyncConfigBuilder setExcludeFieldsWithoutExposeAnnotation(
            boolean excludeFieldsWithoutExposeAnnotation) {
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
        return this;
    }

    public JSONSyncConfigBuilder setNamingPolicy(FieldNamingPolicy namingPolicy) {
        this.namingPolicy = namingPolicy;
        return this;
    }

    public JSONSyncConfigBuilder setLongSerializationPolicy(
            LongSerializationPolicy longSerializationPolicy) {
        this.longSerializationPolicy = longSerializationPolicy;
        return this;
    }

    public JSONSyncConfig build() {
        return new JSONSyncConfig(this);
    }
}