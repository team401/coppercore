package coppercore.parameter_tools.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import coppercore.parameter_tools.path_provider.PathProvider;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A utility class for synchronizing Java objects with JSON files. Provides functionality for
 * loading and saving data to a JSON file using the Gson library.
 */
public class JSONSync<T> {

    private static final JSONSyncConfig defaultConfig = new JSONSyncConfigBuilder().build();
    private final Gson gson; // Gson instance for serialization and deserialization
    private T instance; // The object being synchronized
    private String file; // File path for the JSON file
    private final JSONSyncConfig config; // Configuration for Gson
    private final PathProvider pathProvider;

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
        String path = file;
        if (pathProvider != null) {
            path = pathProvider.resolveReadPath(file);
        }
        instance = gson.fromJson(getFileReader(path), (Class<T>) instance.getClass());
    }

    /**
     * Saves the current state of the object to the JSON file.
     *
     * @throws RuntimeException if the JSON file cannot be written.
     */
    public void saveData() {
        String path = file;
        if (pathProvider != null) {
            path = pathProvider.resolveReadPath(file);
        }
        String json = gson.toJson(instance);
        FileWriter writer = getFileWriter(path);
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

    public JSONSync(T instance, String file) {
        this(instance, file, null, defaultConfig);
    }

    public JSONSync(T instance, String file, PathProvider provider) {
        this(instance, file, provider, defaultConfig);
    }

    /**
     * Constructs a new JSONSync instance.
     *
     * @param instance The object to synchronize with the JSON file.
     * @param file The file path for the JSON file.
     * @param config Configuration for the Gson instance; uses defaults if null.
     */
    public JSONSync(T instance, String file, JSONSyncConfig config) {
        this(instance, file, null, config);
    }

    public JSONSync(T instance, String file, PathProvider provider, JSONSyncConfig config) {
        this.instance = instance;
        this.config = (config == null) ? new JSONSyncConfigBuilder().build() : config;
        this.gson = generateGson();
        this.file = file;
        this.pathProvider = provider;
    }

    /**
     * Generates a Gson instance with the current configuration.
     *
     * @return A configured Gson instance.
     */
    private Gson generateGson() {
        ExclusionStrategy jsonExcludeStrategy = new JSONExcludeExclusionStrategy();
        FieldNamingStrategy jsonNameStrategy = new JSONNamingStrategy(this.config.namingPolicy());
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new JSONTypeAdapterFactory());
        if (this.config.serializeNulls()) builder.serializeNulls();
        if (this.config.prettyPrinting()) builder.setPrettyPrinting();
        if (this.config.excludeFieldsWithoutExposeAnnotation())
            builder.excludeFieldsWithoutExposeAnnotation();
        builder.setFieldNamingStrategy(jsonNameStrategy)
                .setLongSerializationPolicy(this.config.longSerializationPolicy())
                .addDeserializationExclusionStrategy(jsonExcludeStrategy)
                .addSerializationExclusionStrategy(jsonExcludeStrategy);
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
}
