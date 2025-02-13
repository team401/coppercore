package coppercore.parameter_tools.test;

import java.io.File;

import coppercore.parameter_tools.path_provider.PathProvider;

public class UnitTestingPathProvider implements PathProvider {

    public static final String DIRECTORY = new File("").getAbsolutePath() + File.pathSeparator + "build" + File.pathSeparator+ "resources" + File.pathSeparator + "test";
    private String path;

    public UnitTestingPathProvider(){
        path = "";
    }

    private UnitTestingPathProvider(String path){
        this.path = path;
    }

    @Override
    public String resolvePath(String file) {
        if (!"".equals(path)){
            return DIRECTORY + File.pathSeparator + path + File.pathSeparator + file;
        }else{
            return DIRECTORY + File.pathSeparator + file;
        }
    }

    public UnitTestingPathProvider getDirectory(String path){
        if (!"".equals(path)){
            return new UnitTestingPathProvider(this.path + File.pathSeparator + path);
        }else{
            return new UnitTestingPathProvider(path);
        }
    }
   
}