package coppercore.parameter_tools.path_provider;

import java.io.File;

public class Environment {
    private String name;
    private String filepath;

    @SuppressWarnings("FieldMayBeFinal")
    private Boolean defaultToEnvironment = null;

    public String getName() {
        return name;
    }

    public String getPath() {
        return (filepath != null) ? filepath : name;
    }

    public boolean hasFile(String basePath, String file) {
        File f = new File(basePath + File.separator + getPath() + File.separator + file);
        return f.exists() && !f.isDirectory();
    }

    public Boolean getDefaultToEnvironment() {
        return defaultToEnvironment;
    }
}
