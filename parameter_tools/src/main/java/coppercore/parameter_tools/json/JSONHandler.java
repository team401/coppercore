package coppercore.parameter_tools.json;

import coppercore.parameter_tools.path_provider.PathProvider;

/** Handler to create JSONSync objects with given config and path provider */
public class JSONHandler {

    private final JSONSyncConfig config;
    private final PathProvider path_provider;

    /** Creates a JSONHandler with default config and no path provider */
    public JSONHandler() {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    /**
     * Creates a JSONHandler with default config and given path provider
     *
     * @param path_provider path provider
     */
    public JSONHandler(PathProvider path_provider) {
        this(new JSONSyncConfigBuilder().build(), path_provider);
    }

    /**
     * Creates a JSONHandler with given config and no path provider
     *
     * @param config JSONSync config
     */
    public JSONHandler(JSONSyncConfig config) {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    /**
     * Creates a JSONHandler with given config and path provider
     *
     * @param config JSONSync config
     * @param path_provider path provider
     */
    public JSONHandler(JSONSyncConfig config, PathProvider path_provider) {
        this.config = config;
        this.path_provider = path_provider;
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param pathProvider pathProvider to get path to file
     * @param config JSONSync config
     * @return JSONSync Object for the target type
     */
    public <T> JSONSync<T> getJsonSync(
            T blankObject, String filename, PathProvider pathProvider, JSONSyncConfig config) {
        if (pathProvider == null) {
            return new JSONSync<>(blankObject, filename, config);
        }
        return new JSONSync<>(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param pathProvider pathProvider to get path to file
     * @return JSONSync Object for the target type
     */
    public <T> JSONSync<T> getJsonSync(T blankObject, String filename, PathProvider pathProvider) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param config JSONSync config
     * @return JSONSync Object for the target type
     */
    public <T> JSONSync<T> getJsonSync(T blankObject, String filename, JSONSyncConfig config) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @return JSONSync Object for the target type
     */
    public <T> JSONSync<T> getJsonSync(T blankObject, String filename) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Loads a object from a file using a JSONSync Object
     *
     * @param <T> type of the object
     * @param blankObject a blank version of the object (To make java/gson happy)
     * @param filename The name of the file to load the object from.
     * @return The loaded object
     */
    public <T> T getObject(T blankObject, String filename) {
        JSONSync<T> sync = getJsonSync(blankObject, filename);
        sync.loadData();
        return sync.getObject();
    }

    /**
     * Save the object to a json file
     *
     * @param <T> Type of the object
     * @param object the object to save
     * @param filename the name of the file
     */
    public <T> void saveObject(T object, String filename) {
        JSONSync<T> sync = getJsonSync(object, filename);
        sync.saveData();
    }
}
