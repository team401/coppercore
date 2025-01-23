package coppercore.parameter_tools.path_provider;

import coppercore.parameter_tools.JSONSync;
import coppercore.parameter_tools.JSONSyncConfigBuilder;

public class EnvironmentHandler {
    static JSONSync<EnvironmentHandler> sync = new JSONSync<>(new EnvironmentHandler(), "", new JSONSyncConfigBuilder().build());
    static EnvironmentHandler instance;

    Environment[] environments;
    String defaults;
    String environment;

    public static EnvironmentHandler getEnviromentHandler(){
        return instance;
    }

    //Path only used if not loaded
    public static EnvironmentHandler getEnviromentHandler(String path){
        if (instance == null){
            reloadEnviromentHandler(path);
        }
        return instance;
    }

    static void reloadEnviromentHandler(String path){
        sync.setFile(path);
        instance = sync.getObject();
    }


    public EnvironmentPathProvider getEnviromentPathProvider(){
        Environment env = null;
        for (int i = 0; i<environments.length; i++){
            if (environments[i].getName() == environment){
                env = environments[i];
            }
        }
        if (env == null){
            throw new RuntimeException("Enviroment not found");
        }
        return new EnvironmentPathProvider(env, defaults);
    }

}
