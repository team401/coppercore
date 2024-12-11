package coppercore.parameter_tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONSync<T> {

    private final JSONSyncConfig defaultConfig = new JSONSyncConfigBuilder().build();

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
        ExclusionStrategy jsonExcludeStrategy = new JSONExcludeExclusionStrategy();
        FieldNamingStrategy jsonNameStrategy = new JSONNameNamingStrategy(this.config.namingPolicy());
        GsonBuilder builder = new GsonBuilder();
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

    public JSONSync(T instance, String file, JSONSyncConfig config) {
        this.instance = instance;
        this.config = (config == null) ? new JSONSyncConfigBuilder().build() : config;
        this.gson = generateGson();
        this.file = file;
    }

    public JSONSync(T instance, String file) {

    }

}
