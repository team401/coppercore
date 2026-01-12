package coppercore.parameter_tools.path_provider;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONExclude;
import java.io.File;
import java.util.Map;

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
    private final Map<String, Environment> environments = new java.util.HashMap<>();
    private String defaultEnvironmentName;
    private String selectedEnvironmentName;

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
     * @param environmentName the name of the environment to set
     * @return the current instance of EnvironmentHandler for method chaining
     */
    public EnvironmentHandler setEnvironment(String environmentName) {
        this.selectedEnvironmentName = environmentName;
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
        if (selectedEnvironmentName == null) {
            throw new RuntimeException("No environment selected");
        }
        Environment selectedEnv = environments.getOrDefault(this.selectedEnvironmentName, null);
        if (selectedEnv == null) {
            throw new RuntimeException("Environment not found");
        }
        // Give the environment its name for path purposes
        selectedEnv.setEnvironmentName(this.selectedEnvironmentName);

        Environment defaultEnv = environments.getOrDefault(this.defaultEnvironmentName, null);
        String defaultFilepath = null;
        if (defaultEnv != null) {
            // Give the default environment its name for path purposes
            defaultEnv.setEnvironmentName(this.defaultEnvironmentName);
            defaultFilepath = defaultEnv.getPath();
        }
        return new EnvironmentPathProvider(selectedEnv, filepath, defaultFilepath);
    }
}
