package coppercore.parameter_tools.path_provider;

import java.io.File;

/**
 * Represents an environment with a name and a file path. Provides methods to retrieve the name,
 * path, and check for the existence of a file.
 */
public class Environment {
    private String name;
    private String filepath;

    /** If is isolated only uses files in its directory */
    private final Boolean isolated = false;

    /**
     * Returns the name of the environment.
     *
     * @return the name of the environment
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the file path of the environment.
     *
     * @return the file path, or the name if not set
     */
    public String getPath() {
        return (filepath != null) ? filepath : name;
    }

    /**
     * Checks if it contains file
     *
     * @param basePath path to file inside environment
     * @param file file to environment
     * @return if it has the file
     */
    public boolean hasFile(String basePath, String file) {
        File f = new File(basePath + File.separator + getPath() + File.separator + file);
        return f.exists() && !f.isDirectory();
    }

    /**
     * Return if the environment is isolated
     *
     * @return environment is isolated
     */
    public Boolean isIsolated() {
        return isolated;
    }
}
