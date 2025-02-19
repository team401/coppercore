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
    public String resolveReadPath(String file) {
        String path = resolvePath(file);
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return path;
        } else {
            throw new RuntimeException(file + " does not exist at " + path);
        }
    }

    @Override
    public String resolvePath(String file) {
        String pathAttempt =
                filepath + File.separator + environment.getPath() + File.separator + file;

        if (environment.hasFile(file)) {
            return pathAttempt;
        }


        if (defaults != null) {
            pathAttempt = filepath + File.separator + defaults + File.separator + file;
            return pathAttempt;
        }

        throw new RuntimeException(
                "Could not find "
                        + file
                        + " in the "
                        + environment.getName()
                        + " environment at path "
                        + pathAttempt);
    }
}
