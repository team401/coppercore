package coppercore.parameter_tools.path_provider;

import java.io.File;

public class EnviromentPathProvider implements PathProvider{
    Enviroment enviroment;
    String defaults;
    String filepath;

    public EnviromentPathProvider(Enviroment enviroment, String filepath){
        this(enviroment, filepath, null);
    }

    public EnviromentPathProvider(Enviroment enviroment, String filepath, String defaultsPath){
        this.enviroment = enviroment;
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
        if (enviroment.hasFile(file)){
            return filepath + File.pathSeparator + enviroment.getPath() + File.pathSeparator + file;
        }
        if (defaults != null){
            return filepath + File.pathSeparator + defaults + File.pathSeparator + file;
        }
        throw new RuntimeException("Could not find "+file+" in the "+enviroment.getName()+" enviroment");
    }
}
