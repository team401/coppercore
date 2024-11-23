package coppercore.parameter_tools;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONSync<T> {

    private final Gson gson;

    private T instance;

    private String file;

    private final JSONSyncConfig config;

    public T getObject() {
        return instance;
    }

    private FileReader getFileReader(String path) {
        try {
            return new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found " + path, e);
        }
    }

    private FileWriter getFileWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            throw new RuntimeException("IOException " + path, e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        instance = gson.fromJson(getFileReader(file), (Class<T>) instance.getClass());
    }

    public void saveData() {
        String json = gson.toJson(instance);
        // System.out.println(json);
        FileWriter writer = getFileWriter(file);
        try {
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        }
    }

    public void setFile(String newFilePath) {
        file = newFilePath;
    }

    private Gson generateGson() {
        ExclusionStrategy strategy =
                new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        return (field.getAnnotation(JSONExclude.class) != null);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                };

        GsonBuilder builder = new GsonBuilder();
        if (this.config.serializeNulls) builder.serializeNulls();
        if (this.config.prettyPrinting) builder.setPrettyPrinting();
        if (this.config.excludeFieldsWithoutExposeAnnotation)
            builder.excludeFieldsWithoutExposeAnnotation();
        builder.setFieldNamingPolicy(this.config.namingPolicy);
        builder.setLongSerializationPolicy(this.config.longSerializationPolicy);
        builder.addDeserializationExclusionStrategy(strategy);
        return builder.create();
    }

    public JSONSync(T instance, String file, JSONSyncConfig config) {
        this.instance = instance;
        this.config = (config == null) ? new JSONSyncConfigBuilder().build() : config;
        this.gson = generateGson();
        this.file = file;
    }

    private static record JSONSyncConfig(
            boolean serializeNulls,
            boolean prettyPrinting,
            boolean excludeFieldsWithoutExposeAnnotation,
            FieldNamingPolicy namingPolicy,
            LongSerializationPolicy longSerializationPolicy) {
        public JSONSyncConfig(JSONSyncConfigBuilder builder) {
            this(
                    builder.serializeNulls,
                    builder.prettyPrinting,
                    builder.excludeFieldsWithoutExposeAnnotation,
                    builder.namingPolicy,
                    builder.longSerializationPolicy);
        }
    }

    public static class JSONSyncConfigBuilder {
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
}
