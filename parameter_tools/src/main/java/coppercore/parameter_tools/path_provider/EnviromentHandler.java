package coppercore.parameter_tools.path_provider;

import coppercore.parameter_tools.JSONSync;
import coppercore.parameter_tools.JSONSyncConfigBuilder;

public class EnviromentHandler {
    static JSONSync<EnviromentHandler> sync = new JSONSync<>(new EnviromentHandler(), "", new JSONSyncConfigBuilder().build());
    static EnviromentHandler instance;

    Enviroment[] enviroments;
    String defaults;
    String enviroment;

    public static EnviromentHandler getEnviromentHandler(){
        return instance;
    }

    //Path only used if not loaded
    public static EnviromentHandler getEnviromentHandler(String path){
        if (instance == null){
            reloadEnviromentHandler(path);
        }
        return instance;
    }

    static void reloadEnviromentHandler(String path){
        sync.setFile(path);
        instance = sync.getObject();
    }


    public EnviromentPathProvider getEnviromentPathProvider(){
        Enviroment env = null;
        for (int i = 0; i<enviroments.length; i++){
            if (enviroments[i].getName() == enviroment){
                env = enviroments[i];
            }
        }
        if (env == null){
            throw new RuntimeException("Enviroment not found");
        }
        return new EnviromentPathProvider(env, defaults);
    }

}
