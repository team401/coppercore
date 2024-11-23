package coppercore.parameter_tools;

public abstract class JSONConstantLoader<clazz> implements JSONSyncronized<clazz> {
    public JSONSync<clazz> sync;

    private static JSONSync.JSONSyncConfig getDefaultConfig() {
        return new JSONSync.JSONSyncConfigBuilder().build();
    }

    public JSONConstantLoader(clazz obj, String filePath) {
        this(obj, filePath, getDefaultConfig());
    }

    public JSONConstantLoader(clazz obj, String filePath, JSONSync.JSONSyncConfig config) {
        sync = new JSONSync<>(obj, filePath, config);
    }

    public JSONSync getJSONSync() {
        return sync;
    }

    public clazz getObject() {
        return sync.getObject();
    }

    public void loadConstants() {
        sync.loadData();
    }
}
