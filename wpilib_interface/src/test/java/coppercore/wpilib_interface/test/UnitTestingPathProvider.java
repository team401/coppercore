package coppercore.wpilib_interface.test;

import coppercore.parameter_tools.path_provider.PathProvider;
import java.io.File;

public class UnitTestingPathProvider implements PathProvider {

    public static final String DIRECTORY =
            new File("").getAbsolutePath()
                    + File.separator
                    + "build"
                    + File.separator
                    + "resources"
                    + File.separator
                    + "test";
    private String path;

    public UnitTestingPathProvider() {
        this("");
    }

    private UnitTestingPathProvider(String path) {
        this.path = path;
    }

    @Override
    public String resolvePath(String file) {
        if (!"".equals(path)) {
            return DIRECTORY + File.separator + path + File.separator + file;
        } else {
            return DIRECTORY + File.separator + file;
        }
    }

    public String getPath() {
        return path;
    }

    public String getFullPath() {
        return DIRECTORY + File.separator + path;
    }

    public UnitTestingPathProvider getDirectory(String path) {
        if (!"".equals(this.path)) {
            return new UnitTestingPathProvider(this.path + File.separator + path);
        } else {
            return new UnitTestingPathProvider(path);
        }
    }
}
