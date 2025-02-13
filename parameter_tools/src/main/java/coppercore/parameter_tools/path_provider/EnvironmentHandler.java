package coppercore.parameter_tools.path_provider;

import java.io.File;

import coppercore.parameter_tools.json.JSONExclude;
import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;

public class EnvironmentHandler {
    private static final JSONSync<EnvironmentHandler> sync =
            new JSONSync<>(new EnvironmentHandler(), "", new JSONSyncConfigBuilder().build());


    @JSONExclude private String filepath;
    private Environment[] environments;
    private String defaults;
    private String environment;

    private EnvironmentHandler(){}

    public static EnvironmentHandler getEnvironmentHandler(String path) {
        sync.setFile(path);
        sync.loadData();
        EnvironmentHandler instance = sync.getObject();

        instance.filepath = new File(path).getParent();

        return instance;
    }

    public EnvironmentHandler reload(){
        return getEnvironmentHandler(filepath);
    }

    public EnvironmentHandler setEnvironment(String environment){
        this.environment = environment;
        return this;
    }

    public EnvironmentPathProvider getEnvironmentPathProvider() {
        Environment env = null;
        for (Environment environment1 : environments) {
            if (environment1.getName().compareTo(environment) == 0) {
                env = environment1;
            }
        }
        if (env == null) {
            throw new RuntimeException("Environment not found");
        }
        return new EnvironmentPathProvider(env, filepath, defaults);
    }
}
