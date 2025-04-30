package coppercore.parameter_tools.json;

import coppercore.parameter_tools.path_provider.PathProvider;

public class JSONHandler {

    private final JSONSyncConfig config;
    private final PathProvider path_provider;

    public JSONHandler() {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    public JSONHandler(PathProvider path_provider) {
        this(new JSONSyncConfigBuilder().build(), path_provider);
    }

    public JSONHandler(JSONSyncConfig config) {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    public JSONHandler(JSONSyncConfig config, PathProvider path_provider) {
        this.config = config;
        this.path_provider = path_provider;
    }

    public <T> JSONSync<T> getJsonSync(T blankObject, String filename, PathProvider pathProvider, JSONSyncConfig config){
        if (pathProvider == null){
            return new JSONSync<>(blankObject, filename, config);
        }
        return new JSONSync<>(blankObject, filename, path_provider, config);
    }  

    public <T> JSONSync<T> getJsonSync(T blankObject, String filename, PathProvider pathProvider){
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    public <T> JSONSync<T> getJsonSync(T blankObject, String filename, JSONSyncConfig config){
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    public <T> JSONSync<T> getJsonSync(T blankObject, String filename){
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    public <T> T getObject(T blankObject, String filename) {
        JSONSync<T> sync = getJsonSync(blankObject, filename);
        sync.loadData();
        return sync.getObject();
    }

    public <T> void saveObject(T object, String filename) {
        JSONSync<T> sync = getJsonSync(object, filename);
        sync.saveData();
    }
}
