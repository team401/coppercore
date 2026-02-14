package coppercore.parameter_tools.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import coppercore.parameter_tools.json.strategies.JSONExcludeExclusionStrategy;
import coppercore.parameter_tools.json.strategies.JSONNamingStrategy;
import coppercore.parameter_tools.json.strategies.JSONPrimitiveCheckStrategy;
import coppercore.parameter_tools.path_provider.PathProvider;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.Pair;
import edu.wpi.first.wpilibj.RobotController;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Handler to create JSONSync objects with given config and path provider */
public final class JSONHandler {

    private static final int PORT = 8088;
    private static HttpServer server;
    private static final Object serverLock = new Object();
    private static final Set<String> registeredPaths = new HashSet<>();
    private static final Map<String, RouteInfo<?>> routeInfoMap = new HashMap<>();

    /** Holds information about a registered route including the instance and optional callback. */
    private static class RouteInfo<T> {
        final T instance;
        final Class<T> type;
        Function<T, Boolean> postCallback;

        @SuppressWarnings("unchecked")
        RouteInfo(T instance) {
            this.instance = instance;
            this.type = (Class<T>) instance.getClass();
            this.postCallback = null;
        }
    }

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
     * <p>Supported HTTP methods:
     *
     * <ul>
     *   <li>GET - Returns the current state of the object as JSON
     *   <li>PUT - Updates the object's fields from the JSON in the request body
     *   <li>POST - Calls the registered callback (if any) with the object
     * </ul>
     *
     * @param <T> Type of the object
     * @param path the route path (should start with /)
     * @param instance of the object to serialize and serve
     */
    public <T> void addRoute(String path, T instance) {
        ensureServerStarted();
        String environment = getEnvironmentName();
        String fullPath = "/" + environment + path;

        RouteInfo<T> routeInfo = new RouteInfo<>(instance);

        synchronized (serverLock) {
            if (registeredPaths.contains(fullPath)) {
                server.removeContext(fullPath);
            }
            registeredPaths.add(fullPath);
            routeInfoMap.put(fullPath, routeInfo);
        }

        server.createContext(fullPath, exchange -> handleRequest(exchange, fullPath, routeInfo));

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
     * Registers a callback to be invoked when a POST request is made to the specified route. The
     * callback receives the object associated with the route and returns a boolean indicating
     * success or failure, which is sent back to the client.
     *
     * @param <T> Type of the object
     * @param path the route path (should start with /)
     * @param callback the callback to invoke on POST requests, returns true for success
     * @throws IllegalArgumentException if no route exists for the given path
     */
    @SuppressWarnings("unchecked")
    public <T> void registerPostCallback(String path, Function<T, Boolean> callback) {
        String environment = getEnvironmentName();
        String fullPath = "/" + environment + path;

        synchronized (serverLock) {
            RouteInfo<?> routeInfo = routeInfoMap.get(fullPath);
            if (routeInfo == null) {
                throw new IllegalArgumentException("No route registered for path: " + fullPath);
            }
            ((RouteInfo<T>) routeInfo).postCallback = callback;
        }
    }

    /**
     * Handles an HTTP request for a route.
     *
     * @param <T> Type of the object
     * @param exchange the HTTP exchange
     * @param fullPath the full path of the route
     * @param routeInfo the route information
     */
    private <T> void handleRequest(HttpExchange exchange, String fullPath, RouteInfo<T> routeInfo)
            throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, routeInfo);
                    break;
                case "PUT":
                    handlePut(exchange, routeInfo);
                    break;
                case "POST":
                    handlePost(exchange, routeInfo);
                    break;
                default:
                    sendError(exchange, 405, "Method Not Allowed");
                    break;
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Handles a GET request by returning the object as JSON.
     *
     * @param <T> Type of the object
     * @param exchange the HTTP exchange
     * @param routeInfo the route information
     */
    private <T> void handleGet(HttpExchange exchange, RouteInfo<T> routeInfo) throws IOException {
        String json = buildGson().toJson(routeInfo.instance);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    /**
     * Handles a PUT request by updating the object's fields from the JSON in the request body.
     *
     * @param <T> Type of the object
     * @param exchange the HTTP exchange
     * @param routeInfo the route information
     */
    private <T> void handlePut(HttpExchange exchange, RouteInfo<T> routeInfo) throws IOException {
        String requestBody;
        try (InputStream is = exchange.getRequestBody()) {
            requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        Gson gson = buildGson();
        T updatedObject = gson.fromJson(requestBody, routeInfo.type);

        // Copy fields from updated object to the instance
        copyFields(updatedObject, routeInfo.instance);

        // Return the updated object
        String json = gson.toJson(routeInfo.instance);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    /**
     * Handles a POST request by invoking the registered callback with the object. The callback's
     * boolean return value is included in the response.
     *
     * @param <T> Type of the object
     * @param exchange the HTTP exchange
     * @param routeInfo the route information
     */
    private <T> void handlePost(HttpExchange exchange, RouteInfo<T> routeInfo) throws IOException {
        if (routeInfo.postCallback == null) {
            sendError(exchange, 400, "No callback registered for this route");
            return;
        }

        Boolean result = routeInfo.postCallback.apply(routeInfo.instance);

        // Return the result along with the current state of the object
        Gson gson = buildGson();
        String objectJson = gson.toJson(routeInfo.instance);
        String json = "{\"success\":" + result + ",\"data\":" + objectJson + "}";
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    /**
     * Copies all fields from the source object to the target object.
     *
     * @param <T> Type of the objects
     * @param source the source object
     * @param target the target object
     */
    private <T> void copyFields(T source, T target) {
        Class<?> clazz = source.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(source);
                    if (value != null) {
                        field.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    // Skip fields that can't be accessed
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Sends an error response.
     *
     * @param exchange the HTTP exchange
     * @param code the HTTP status code
     * @param message the error message
     */
    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
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
