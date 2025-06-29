package coppercore.parameter_tools.path_provider;

/**
 * Interface for providing paths for reading and writing files.
 * Implementations should define how to resolve the file paths.
 */
public interface PathProvider {

    /**
     * Resolves the path for reading a file.
     * @param file the name of the file to read
     * @return the resolved path for reading the file
     */
    default String resolveReadPath(String file) {
        return resolvePath(file);
    }
    /**
     * Resolves the path for writing a file.
     * @param file the name of the file to write
     * @return the resolved path for writing the file
     */
    default String resolveWritePath(String file) {
        return resolvePath(file);
    }

    /**
     * Resolves the path for a file.
     * @param file the name of the file to resolve
     * @return the resolved path for the file
     */
    String resolvePath(String file);
}
