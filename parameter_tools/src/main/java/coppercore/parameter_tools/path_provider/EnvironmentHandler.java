package coppercore.parameter_tools.path_provider;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONExclude;
import java.io.File;

/**
 * Handles the loading and management of environments from a JSON file. Provides methods to retrieve
 * the current environment and its associated path.
 *
 * <p>warning To avoid dsync inside application, the same environment handler should be used across
 * the application. Unless if it is desired to have different environments for different parts of
 * the application.
 */
public class EnvironmentHandler {
    // TODO: Switch to using JSONHandler
    // Singleton instance for JSON synchronization
    private static final JSONSync<EnvironmentHandler> sync =
            new JSONSync<>(new EnvironmentHandler(), "", new JSONSyncConfigBuilder().build());

    @JSONExclude private String filepath;
    private Environment[] environments;
    private String defaults;
    private String environment;

    /**
     * Private constructor to prevent instantiation. Use the static method getEnvironmentHandler to
     * obtain an instance.
     */
    private EnvironmentHandler() {}

    /**
     * Retrieves an instance of EnvironmentHandler, loading data from the specified JSON file.
     *
     * @param path the path to the JSON file containing environment data
     * @return an instance of EnvironmentHandler with loaded environments
     */
    public static EnvironmentHandler getEnvironmentHandler(String path) {
        sync.setFile(path);
        sync.loadData();
        EnvironmentHandler instance = sync.getObject();

        instance.filepath = new File(path).getParent();

        return instance;
    }

    /**
     * Retrieves the current instance of EnvironmentHandler.
     *
     * @return the current instance of EnvironmentHandler
     */
    public EnvironmentHandler reload() {
        return getEnvironmentHandler(filepath);
    }

    /**
     * Sets the current environment.
     *
     * @param environment the name of the environment to set
     * @return the current instance of EnvironmentHandler for method chaining
     */
    public EnvironmentHandler setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Retrieves a EnvironmentPathProvider for the specified environment. If no matching environment
     * is found, a RuntimeException is thrown.
     *
     * @return an instance of EnvironmentPathProvider for the matching environment and file path
     * @throws RuntimeException if the specified environment is not found in the list of
     *     environments.
     */
    public EnvironmentPathProvider getEnvironmentPathProvider() {
        Environment env = null;
        for (Environment environment1 : environments) {
            if (environment1.getName().compareTo(environment) == 0) {
                env = environment1;
            }
        }
        if (env == null) {
            throw new RuntimeException("Environment not found");
        }
        return new EnvironmentPathProvider(env, filepath, defaults);
    }
}
