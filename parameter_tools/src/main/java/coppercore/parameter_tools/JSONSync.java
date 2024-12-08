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

/**
 * A utility class for synchronizing Java objects with JSON files. Provides functionality for
 * loading and saving data to a JSON file using the Gson library.
 */
public class JSONSync<T> {

    private final Gson gson; // Gson instance for serialization and deserialization
    private T instance; // The object being synchronized
    private String file; // File path for the JSON file
    private final JSONSyncConfig config; // Configuration for Gson

    /**
     * Retrieves the object synchronized with the JSON file.
     *
     * @return The synchronized object.
     */
    public T getObject() {
        return instance;
    }

    /**
     * Loads the data from the JSON file into the object.
     *
     * @throws RuntimeException if the JSON file cannot be read or parsed.
     */
    @SuppressWarnings("unchecked")
    public void loadData() {
        instance = gson.fromJson(getFileReader(file), (Class<T>) instance.getClass());
    }

    /**
     * Saves the current state of the object to the JSON file.
     *
     * @throws RuntimeException if the JSON file cannot be written.
     */
    public void saveData() {
        String json = gson.toJson(instance);
        FileWriter writer = getFileWriter(file);
        try {
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        }
    }

    /**
     * Sets the file path for the JSON file.
     *
     * @param newFilePath The new file path for the JSON file.
     */
    public void setFile(String newFilePath) {
        file = newFilePath;
    }

    /**
     * Constructs a new JSONSync instance.
     *
     * @param instance The object to synchronize with the JSON file.
     * @param file The file path for the JSON file.
     * @param config Configuration for the Gson instance; uses defaults if null.
     */
    public JSONSync(T instance, String file, JSONSyncConfig config) {
        this.instance = instance;
        this.config = (config == null) ? new JSONSyncConfigBuilder().build() : config;
        this.gson = generateGson();
        this.file = file;
    }

    /**
     * Generates a Gson instance with the current configuration.
     *
     * @return A configured Gson instance.
     */
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
        builder.setFieldNamingPolicy(this.config.namingPolicy)
                .setLongSerializationPolicy(this.config.longSerializationPolicy)
                .addDeserializationExclusionStrategy(strategy)
                .addSerializationExclusionStrategy(strategy);
        return builder.create();
    }

    /**
     * Provides a FileReader for the given file path.
     *
     * @param path The file path.
     * @return A FileReader for the specified file.
     * @throws RuntimeException if the file cannot be found.
     */
    private FileReader getFileReader(String path) {
        try {
            return new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found " + path, e);
        }
    }

    /**
     * Provides a FileWriter for the given file path.
     *
     * @param path The file path.
     * @return A FileWriter for the specified file.
     * @throws RuntimeException if the file cannot be created or written to.
     */
    private FileWriter getFileWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            throw new RuntimeException("IOException " + path, e);
        }
    }

    /** Configuration class for customizing Gson behavior. */
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

    /** Builder class for creating a JSONSyncConfig instance. */
    public static class JSONSyncConfigBuilder {
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
}
