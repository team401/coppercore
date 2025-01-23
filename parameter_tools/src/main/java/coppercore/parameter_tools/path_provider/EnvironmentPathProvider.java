package coppercore.parameter_tools.path_provider;

import java.io.File;

public class EnvironmentPathProvider implements PathProvider{
    Environment environment;
    String defaults;
    String filepath;

    public EnvironmentPathProvider(Environment environment, String filepath){
        this(environment, filepath, null);
    }

    public EnvironmentPathProvider(Environment enviroment, String filepath, String defaultsPath){
        this.environment = enviroment;
        this.defaults = defaultsPath;
        this.filepath = filepath;
    }

    @Override
    public String resolveReadPath(String file){
        String path = resolvePath(file);
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) { 
            return path;
        }else{
            throw new RuntimeException(file+" does not exist at "+path);
        }
    }

    @Override
    public String resolvePath(String file) {
        if (environment.hasFile(file)){
            return filepath + File.pathSeparator + environment.getPath() + File.pathSeparator + file;
        }
        if (defaults != null){
            return filepath + File.pathSeparator + defaults + File.pathSeparator + file;
        }
        throw new RuntimeException("Could not find "+file+" in the "+environment.getName()+" enviroment");
    }
}
