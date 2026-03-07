package coppercore.parameter_tools.json;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import coppercore.parameter_tools.path_provider.PathProvider;
import edu.wpi.first.hal.HAL;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/** Handler to create JSONSync objects with given config and path provider */
public final class JSONHandler {

    private static final int PORT = 8088;
    private static final int DEFAULT_QUEUE_CAPACITY = 256;
    private static HttpServer server;
    private static final Object serverLock = new Object();
    private static final Set<String> registeredPaths = new HashSet<>();
    private static final Map<String, RouteInfo<?>> routeInfoMap = new HashMap<>();

    /** Holds information about a registered route including the instance and optional callback. */
    private static class RouteInfo<T> {
        final T instance;
        final JSONSync<T> sync;
        volatile Function<T, Boolean> postCallback;

        RouteInfo(T instance, JSONSync<T> sync) {
            this.instance = instance;
            this.sync = sync;
            this.postCallback = null;
        }
    }

    /** Work submitted by the HTTP server thread for the owner thread to execute. */
    private static class QueuedHttpAction<T> {
        private final Callable<T> action;
        private final CompletableFuture<T> future = new CompletableFuture<>();

        QueuedHttpAction(Callable<T> action) {
            this.action = action;
        }

        void run() {
            try {
                future.complete(action.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    /** Response payload produced by an owner-thread action and written by the HTTP thread. */
    private static class HttpResponse {
        final int statusCode;
        final String contentType;
        final byte[] body;

        HttpResponse(int statusCode, String contentType, byte[] body) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
        }
    }

    private final JSONSyncConfig config;
    private final PathProvider path_provider;
    private final ArrayBlockingQueue<QueuedHttpAction<?>> queuedHttpActions;
    private final QueuedHttpAction<?>[] queuedHttpActionBuffer;

    /** Creates a JSONHandler with default config and no path provider */
    public JSONHandler() {
        this(new JSONSyncConfigBuilder().build(), null, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a JSONHandler with default config and given path provider
     *
     * @param path_provider path provider
     */
    public JSONHandler(PathProvider path_provider) {
        this(new JSONSyncConfigBuilder().build(), path_provider, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a JSONHandler with given config and no path provider
     *
     * @param config JSONSync config
     */
    public JSONHandler(JSONSyncConfig config) {
        this(config, null, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a JSONHandler with given config and path provider
     *
     * @param config JSONSync config
     * @param path_provider path provider
     */
    public JSONHandler(JSONSyncConfig config, PathProvider path_provider) {
        this(config, path_provider, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a JSONHandler with the given config, path provider, and queue capacity for threaded
     * HTTP route actions.
     *
     * @param config JSONSync config
     * @param path_provider path provider
     * @param queueCapacity capacity for queued HTTP actions
     */
    public JSONHandler(JSONSyncConfig config, PathProvider path_provider, int queueCapacity) {
        this.config = config;
        this.path_provider = path_provider;
        this.queuedHttpActions = new ArrayBlockingQueue<>(queueCapacity);
        this.queuedHttpActionBuffer = new QueuedHttpAction<?>[queueCapacity];
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

        RouteInfo<T> routeInfo = new RouteInfo<>(instance, getJsonSync(instance, ""));

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
     * Runs a single queued HTTP action on the calling thread.
     *
     * <p>This is intended to be called by the thread that owns the route objects. Until this method
     * (or {@link #drainQueuedHttpActions()}) runs the queued work, the corresponding HTTP request
     * remains blocked waiting for a response.
     *
     * @return true if an action was run, false if the queue was empty
     */
    public boolean runNextQueuedHttpAction() {
        QueuedHttpAction<?> action = queuedHttpActions.poll();
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    /**
     * Drains and runs all queued HTTP actions on the calling thread.
     *
     * <p>This is the convenient periodic pump for the thread that owns the routed objects. If the
     * owner thread is delayed, incoming GET/PUT/POST requests will continue waiting. If the owner
     * thread never drains the queue, those HTTP requests will hang until the client times out.
     *
     * @return the number of actions that were run
     */
    public int drainQueuedHttpActions() {
        int drainedCount = 0;
        while (drainedCount < queuedHttpActionBuffer.length) {
            QueuedHttpAction<?> action = queuedHttpActions.poll();
            if (action == null) {
                break;
            }
            queuedHttpActionBuffer[drainedCount] = action;
            drainedCount++;
        }

        for (int i = 0; i < drainedCount; i++) {
            queuedHttpActionBuffer[i].run();
            queuedHttpActionBuffer[i] = null;
        }
        return drainedCount;
    }

    /**
     * Handles a GET request by returning the object as JSON.
     *
     * @param <T> Type of the object
     * @param exchange the HTTP exchange
     * @param routeInfo the route information
     */
    private <T> void handleGet(HttpExchange exchange, RouteInfo<T> routeInfo) throws IOException {
        HttpResponse response =
                executeQueuedHttpAction(
                        () -> {
                            String json = routeInfo.sync.serialize();
                            return jsonResponse(json);
                        });
        writeResponse(exchange, response);
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

        HttpResponse response =
                executeQueuedHttpAction(
                        () -> {
                            T updatedObject = routeInfo.sync.deserialize(requestBody);
                            copyFields(updatedObject, routeInfo.instance);
                            return jsonResponse(routeInfo.sync.serialize());
                        });
        writeResponse(exchange, response);
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
        HttpResponse response =
                executeQueuedHttpAction(
                        () -> {
                            if (routeInfo.postCallback == null) {
                                return textResponse(
                                        400, "text/plain", "No callback registered for this route");
                            }

                            Boolean result = routeInfo.postCallback.apply(routeInfo.instance);
                            String objectJson = routeInfo.sync.serialize();
                            String json =
                                    "{\"success\":" + result + ",\"data\":" + objectJson + "}";
                            return jsonResponse(json);
                        });
        writeResponse(exchange, response);
    }

    /**
     * Queues work for the owner thread and waits synchronously for it to finish.
     *
     * <p>This preserves request/response semantics for HTTP clients while ensuring the object is
     * only touched on the owner thread. The tradeoff is that the HTTP worker blocks here until the
     * owner thread drains the queue. A full queue fails fast with 503; otherwise, delays are
     * surfaced to clients as a slow or hanging request unless they time out.
     */
    private HttpResponse executeQueuedHttpAction(Callable<HttpResponse> action) {
        QueuedHttpAction<HttpResponse> queuedAction = new QueuedHttpAction<>(action);
        if (!queuedHttpActions.offer(queuedAction)) {
            return textResponse(503, "text/plain", "HTTP action queue is full");
        }

        try {
            return queuedAction.future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return textResponse(
                    500, "text/plain", "Interrupted while waiting for queued HTTP action");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? "Unknown error" : cause.getMessage();
            return textResponse(500, "text/plain", "Internal Server Error: " + message);
        }
    }

    private HttpResponse jsonResponse(String json) {
        return new HttpResponse(200, "application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    private HttpResponse textResponse(int statusCode, String contentType, String message) {
        return new HttpResponse(statusCode, contentType, message.getBytes(StandardCharsets.UTF_8));
    }

    private void writeResponse(HttpExchange exchange, HttpResponse response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", response.contentType);
        exchange.sendResponseHeaders(response.statusCode, response.body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.body);
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
}
