package coppercore.parameter_tools.path_provider;

public interface PathProvider {

    default String resolveReadPath(String file) {
        return resolvePath(file);
    }

    default String resolveWritePath(String file) {
        return resolvePath(file);
    }

    String resolvePath(String file);

    default String resolvePath(String file, boolean write) {
        if (write) {
            return resolveWritePath(file);
        } else {
            return resolveReadPath(file);
        }
    }
}
