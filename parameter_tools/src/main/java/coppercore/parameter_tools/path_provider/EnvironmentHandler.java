package coppercore.parameter_tools.path_provider;

import coppercore.parameter_tools.JSONExclude;
import coppercore.parameter_tools.JSONSync;
import coppercore.parameter_tools.JSONSyncConfigBuilder;
import java.io.File;

public class EnvironmentHandler {
    static JSONSync<EnvironmentHandler> sync =
            new JSONSync<>(new EnvironmentHandler(), "", new JSONSyncConfigBuilder().build());
    static EnvironmentHandler instance;

    Environment[] environments;
    String defaults;
    String environment;

    @JSONExclude static String filepath;

    public static EnvironmentHandler getEnvironmentHandler() {
        return instance;
    }

    // Path only used if not loaded
    public static EnvironmentHandler getEnvironmentHandler(String path) {
        if (instance == null) {
            reloadEnvironmentHandler(path);
        }
        return instance;
    }

    static void reloadEnvironmentHandler(String path) {
        sync.setFile(path);
        sync.loadData();
        instance = sync.getObject();

        filepath = new File(path).getParent();
    }

    public EnvironmentPathProvider getEnvironmentPathProvider() {
        Environment env = null;
        for (int i = 0; i < environments.length; i++) {
            if (environments[i].getName().compareTo(environment) == 0) {
                env = environments[i];
            }
        }
        if (env == null) {
            throw new RuntimeException("Environment not found");
        }
        return new EnvironmentPathProvider(env, filepath, defaults);
    }
}
