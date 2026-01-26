package coppercore.parameter_tools.path_provider;

import java.io.File;

/** Provides file paths based on the current environment. It checks if the file exists in the */
public class EnvironmentPathProvider implements PathProvider {
    Environment environment;
    String defaults;
    String filepath;

    /**
     * Constructor for EnvironmentPathProvider.
     *
     * @param environment the environment to use for path resolution
     * @param filepath the base file path where the environment files are located
     * @param defaultsPath the default path to use if the file does not exist in the environment
     */
    public EnvironmentPathProvider(Environment environment, String filepath, String defaultsPath) {
        this.environment = environment;
        this.defaults = defaultsPath;
        this.filepath = filepath;
    }

    /**
     * Find path for file for the current environment. If File does not exist then it returns the
     * desired location for the file to be made in.
     */
    @Override
    public String resolveWritePath(String file) {
        return resolvePath(file, true);
    }

    /** Gets path to file This will throw an error if the file does not exist */
    @Override
    public String resolveReadPath(String file) {
        String path = resolvePath(file, environment.isIsolated());
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return path;
        } else {
            throw new RuntimeException(file + " does not exist at " + path);
        }
    }

    /**
     * Find path for file for the current environment. If File does not exist then it returns the
     * desired location for the file to be made in.
     *
     * @param file file to find
     * @return resolved path
     */
    @Override
    public String resolvePath(String file) {
        return resolvePath(file, false);
    }

    /**
     * Find path for file for the current environment. If File does not exist then it returns the
     * desired location for the file to be made in.
     *
     * @param file file to find
     * @param defaultToEnvironment if true it will not use the default path if the file does not
     *     exist
     * @return resolved path
     */
    public String resolvePath(String file, boolean defaultToEnvironment) {
        String path = filepath + File.separator + environment.getPath() + File.separator + file;

        if (!environment.hasFile(filepath, file) && defaults != null) {
            path = filepath + File.separator + defaults + File.separator + file;
            if (defaultToEnvironment) {
                File f = new File(path);
                if (!f.exists()) {
                    path =
                            filepath
                                    + File.separator
                                    + environment.getPath()
                                    + File.separator
                                    + file;
                }
            }
        }

        return path;
    }

    /**
     * Returns the environment name from the environment.
     *
     * @return the environment path/name
     */
    @Override
    public String getEnvironmentName() {
        return environment.getPath();
    }
}
