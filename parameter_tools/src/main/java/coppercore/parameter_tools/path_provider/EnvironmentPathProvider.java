package coppercore.parameter_tools.path_provider;

import java.io.File;

public class EnvironmentPathProvider implements PathProvider {
    Environment environment;
    String defaults;
    String filepath;

    public EnvironmentPathProvider(Environment environment, String filepath, String defaultsPath) {
        this.environment = environment;
        this.defaults = defaultsPath;
        this.filepath = filepath;
    }

    @Override
    public String resolveWritePath(String file) {
        return resolvePath(file, environment.getDefaultToEnvironment());
    }

    /**
     * Gets path to file
     *
     * @exception RuntimException This will throw an error if the file does not exist
     */
    @Override
    public String resolveReadPath(String file) {
        String path = resolvePath(file, environment.getDefaultToEnvironment());
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return path;
        } else {
            throw new RuntimeException(file + " does not exist at " + path);
        }
    }

    @Override
    public String resolvePath(String file) {
        return resolvePath(file, false);
    }

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
}
