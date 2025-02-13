package coppercore.parameter_tools.json;

import coppercore.parameter_tools.path_provider.PathProvider;

public class JSONHandler {
    
    private final JSONSyncConfig config;
    private final PathProvider path_provider;

    public JSONHandler(){
        this(new JSONSyncConfigBuilder().build(), null);
    }

    public JSONHandler(PathProvider path_provider){
        this(new JSONSyncConfigBuilder().build(), path_provider);
    }

    public JSONHandler(JSONSyncConfig config){
        this(new JSONSyncConfigBuilder().build(), null);
    }

    public JSONHandler(JSONSyncConfig config, PathProvider path_provider){
        this.config = config;
        this.path_provider = path_provider;
    }

    public <T> T getObject(T blankObject, String filename){
        JSONSync<T> sync;
        if (path_provider != null){
            sync = new JSONSync<>(blankObject, filename, path_provider, config);
        }else{
            sync = new JSONSync<>(blankObject, filename, config);
        }
        sync.loadData();
        return sync.getObject();
    }

    public <T> void saveObject(T object, String filename){
        JSONSync<T> sync;
        if (path_provider != null){
            sync = new JSONSync<>(object, filename, path_provider, config);
        }else{
            sync = new JSONSync<>(object, filename, config);
        }
        sync.saveData();
    }

}
