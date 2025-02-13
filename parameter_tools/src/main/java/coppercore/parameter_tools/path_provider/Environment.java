package coppercore.parameter_tools.path_provider;

import java.io.File;

public class Environment {
    String name;
    String filepath;

    public String getName() {
        return name;
    }

    public String getPath() {
        return (filepath != null)? filepath : name;
    }

    public boolean hasFile(String basePath, String file) {
        File f = new File(basePath + File.separator + filepath + File.separator + file);
        return f.exists() && !f.isDirectory();
    }
}
