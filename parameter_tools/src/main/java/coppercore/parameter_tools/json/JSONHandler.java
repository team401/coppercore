package coppercore.parameter_tools.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.sun.net.httpserver.HttpServer;
import coppercore.parameter_tools.json.strategies.JSONExcludeExclusionStrategy;
import coppercore.parameter_tools.json.strategies.JSONNamingStrategy;
import coppercore.parameter_tools.json.strategies.JSONPrimitiveCheckStrategy;
import coppercore.parameter_tools.path_provider.PathProvider;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.Pair;
import edu.wpi.first.wpilibj.RobotController;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/** Handler to create JSONSync objects with given config and path provider */
public final class JSONHandler {

    private static final int PORT = 8088;
    private static HttpServer server;
    private static final Object serverLock = new Object();
    private static final Set<String> registeredPaths = new HashSet<>();

    private final JSONSyncConfig config;
    private final PathProvider path_provider;

    /** Creates a JSONHandler with default config and no path provider */
    public JSONHandler() {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    /**
     * Creates a JSONHandler with default config and given path provider
     *
     * @param path_provider path provider
     */
    public JSONHandler(PathProvider path_provider) {
        this(new JSONSyncConfigBuilder().build(), path_provider);
    }

    /**
     * Creates a JSONHandler with given config and no path provider
     *
     * @param config JSONSync config
     */
    public JSONHandler(JSONSyncConfig config) {
        this(new JSONSyncConfigBuilder().build(), null);
    }

    /**
     * Creates a JSONHandler with given config and path provider
     *
     * @param config JSONSync config
     * @param path_provider path provider
     */
    public JSONHandler(JSONSyncConfig config, PathProvider path_provider) {
        this.config = config;
        this.path_provider = path_provider;
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param pathProvider pathProvider to get path to file
     * @param config JSONSync config
     * @return JSONSync Object for the target type
     */
    private <T> JSONSync<T> getJsonSync(
            T blankObject, String filename, PathProvider pathProvider, JSONSyncConfig config) {
        if (pathProvider == null) {
            return new JSONSync<>(blankObject, filename, config);
        }
        return new JSONSync<>(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param pathProvider pathProvider to get path to file
     * @return JSONSync Object for the target type
     */
    private <T> JSONSync<T> getJsonSync(T blankObject, String filename, PathProvider pathProvider) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @param config JSONSync config
     * @return JSONSync Object for the target type
     */
    private <T> JSONSync<T> getJsonSync(T blankObject, String filename, JSONSyncConfig config) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Gets JSONSync Object
     *
     * @param <T> type of object JSONSync is built for.
     * @param blankObject blank version of the target type
     * @param filename file name to load the object from.
     * @return JSONSync Object for the target type
     */
    private <T> JSONSync<T> getJsonSync(T blankObject, String filename) {
        return getJsonSync(blankObject, filename, path_provider, config);
    }

    /**
     * Loads a object from a file using a JSONSync Object
     *
     * @param <T> type of the object
     * @param blankObject a blank version of the object (To make java/gson happy)
     * @param filename The name of the file to load the object from.
     * @return The loaded object
     */
    public <T> T getObject(T blankObject, String filename) {
        JSONSync<T> sync = getJsonSync(blankObject, filename);
        sync.loadData();
        return sync.getObject();
    }

    /**
     * Save the object to a json file
     *
     * @param <T> Type of the object
     * @param object the object to save
     * @param filename the name of the file
     */
    public <T> void saveObject(T object, String filename) {
        JSONSync<T> sync = getJsonSync(object, filename);
        sync.saveData();
    }

    /**
     * Adds an HTTP route that serves the given object as JSON. The route will be available at
     * /{environment}/{path} where environment is determined by the path provider.
     *
     * @param <T> Type of the object
     * @param path the route path (should start with /)
     * @param instance of the object to serialize and serve
     */
    public <T> void addRoute(String path, T instance) {
        ensureServerStarted();
        String environment = getEnvironmentName();
        String fullPath = "/" + environment + path;

        synchronized (serverLock) {
            if (registeredPaths.contains(fullPath)) {
                server.removeContext(fullPath);
            }
            registeredPaths.add(fullPath);
        }

        server.createContext(
                fullPath,
                exchange -> {
                    String json = buildGson().toJson(instance);
                    byte[] response = json.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                });

        String host = getHostAddress();
        String url = "http://" + host + ":" + PORT + fullPath;
        System.out.println(
                "added route for "
                        + environment
                        + path
                        + "; use\ncurl "
                        + url
                        + "\nto retrieve the data");
    }

    /**
     * Gets the host address for constructing URLs. On the roboRIO, computes the IP address from the
     * team number using the FRC TE.AM notation (10.TE.AM.2). In simulation or if the team number
     * cannot be determined, falls back to the local host address.
     *
     * @return the host address
     */
    private String getHostAddress() {
        try {
            HAL.initialize(500, 0);
            int team = RobotController.getTeamNumber();
            if (team > 0) {
                int te = team / 100;
                int am = team % 100;
                return "10." + te + "." + am + ".2";
            }
        } catch (Throwable t) {
            // Fall through to local host lookup (catches UnsatisfiedLinkError when HAL not loaded)
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Gets the environment name from the path provider, or "default" if no path provider is set.
     *
     * @return the environment name
     */
    private String getEnvironmentName() {
        if (path_provider == null) {
            return "default";
        }
        return path_provider.getEnvironmentName();
    }

    /** Ensures the HTTP server is started. Lazily initializes the server on first call. */
    private void ensureServerStarted() {
        synchronized (serverLock) {
            if (server == null) {
                try {
                    server = HttpServer.create(new InetSocketAddress(PORT), 0);
                    server.setExecutor(null);
                    server.start();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to start HTTP server on port " + PORT, e);
                }
            }
        }
    }

    /**
     * Builds a Gson instance with the current configuration.
     *
     * @return a configured Gson instance
     */
    private Gson buildGson() {
        ExclusionStrategy jsonExcludeStrategy = new JSONExcludeExclusionStrategy();
        FieldNamingStrategy jsonNameStrategy =
                new JSONNamingStrategy(this.config.namingPolicy(), this.config);
        jsonNameStrategy =
                JSONPrimitiveCheckStrategy.checkForPrimitives(jsonNameStrategy, this.config);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new JSONTypeAdapterFactory(this.config));
        if (this.config.serializeNulls()) {
            builder.serializeNulls();
        }
        if (this.config.prettyPrinting()) {
            builder.setPrettyPrinting();
        }
        if (this.config.excludeFieldsWithoutExposeAnnotation()) {
            builder.excludeFieldsWithoutExposeAnnotation();
        }
        builder.setFieldNamingStrategy(jsonNameStrategy)
                .setLongSerializationPolicy(this.config.longSerializationPolicy())
                .addDeserializationExclusionStrategy(jsonExcludeStrategy)
                .addSerializationExclusionStrategy(jsonExcludeStrategy);
        for (TypeAdapterFactory factory : this.config.typeAdapterFactories()) {
            builder.registerTypeAdapterFactory(factory);
        }
        for (Pair<Class, Object> pair : this.config.typeAdapters()) {
            builder.registerTypeAdapter(pair.getFirst(), pair.getSecond());
        }
        return builder.create();
    }
}
