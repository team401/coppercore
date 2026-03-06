package coppercore.parameter_tools.test;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.json.JSONSyncConfig;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.path_provider.PathProvider;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class JSONHandlerHttpTests {

    public static class TestData {
        public String name = "test";
        public int value = 42;
    }

    public static class DataWithUnits {
        public Distance distance = Units.Meters.of(1.5);
        public LinearVelocity velocity = Units.MetersPerSecond.of(2.5);
        public String label = "sensor";
    }

    public static class DataWithArray {
        public String name = "items";
        public List<Integer> values = new ArrayList<>(List.of(1, 2, 3));
        public List<String> tags = new ArrayList<>(List.of("a", "b", "c"));
    }

    public static class DataWithUnitArray {
        public String name = "angles";
        public Angle[] angles =
                new Angle[] {Units.Degrees.of(0), Units.Degrees.of(90), Units.Degrees.of(180)};
        public Distance[] distances = new Distance[] {Units.Meters.of(1.0), Units.Meters.of(2.0)};
    }

    public static class ThreadTrackedValue {
        public String value = "initial";
    }

    public static class ThreadTrackedData {
        public ThreadTrackedValue tracked = new ThreadTrackedValue();
    }

    private static class ThreadTrackedValueAdapter extends TypeAdapter<ThreadTrackedValue> {
        private final AtomicReference<Thread> writeThread = new AtomicReference<>();
        private final AtomicReference<Thread> readThread = new AtomicReference<>();

        @Override
        public void write(JsonWriter out, ThreadTrackedValue value) throws IOException {
            writeThread.set(Thread.currentThread());
            out.beginObject();
            out.name("value").value(value.value);
            out.endObject();
        }

        @Override
        public ThreadTrackedValue read(JsonReader in) throws IOException {
            readThread.set(Thread.currentThread());
            ThreadTrackedValue value = new ThreadTrackedValue();
            in.beginObject();
            while (in.hasNext()) {
                if ("value".equals(in.nextName())) {
                    value.value = in.nextString();
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return value;
        }
    }

    private static class HttpResult {
        final int statusCode;
        final String contentType;
        final String body;

        HttpResult(int statusCode, String contentType, String body) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
        }
    }

    private CompletableFuture<HttpResult> startRequest(String method, String path) {
        return startRequest(method, path, null);
    }

    private CompletableFuture<HttpResult> startRequest(
            String method, String path, String jsonBody) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        URL url = new URL("http://localhost:8088" + path);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod(method);
                        conn.setConnectTimeout(1000);
                        conn.setReadTimeout(5000);
                        if (jsonBody != null) {
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json");
                            try (OutputStream os = conn.getOutputStream()) {
                                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        int statusCode = conn.getResponseCode();
                        String contentType = conn.getContentType();
                        byte[] bodyBytes =
                                conn.getErrorStream() != null
                                        ? conn.getErrorStream().readAllBytes()
                                        : conn.getInputStream().readAllBytes();
                        return new HttpResult(
                                statusCode,
                                contentType,
                                new String(bodyBytes, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private HttpResult awaitRequest(JSONHandler handler, CompletableFuture<HttpResult> future)
            throws InterruptedException, ExecutionException, TimeoutException {
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!future.isDone() && System.nanoTime() < deadlineNanos) {
            int drained = handler.drainQueuedHttpActions();
            if (drained == 0) {
                Thread.sleep(10);
            }
        }
        assertTrue(
                future.isDone(), "Request should complete after the owner drains queued actions");
        return future.get(1, TimeUnit.SECONDS);
    }

    private HttpResult awaitDirectRequest(CompletableFuture<HttpResult> future)
            throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(5, TimeUnit.SECONDS);
    }

    @Test
    void addRoute_servesJsonAtDefaultEnvironment()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "hello";
        data.value = 123;

        handler.addRoute("/testroute", data);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/default/testroute"));

        assertEquals(200, response.statusCode);
        assertEquals("application/json", response.contentType);
        assertTrue(response.body.contains("\"name\""));
        assertTrue(response.body.contains("\"hello\""));
        assertTrue(response.body.contains("\"value\""));
        assertTrue(response.body.contains("123"));
    }

    @Test
    void addRoute_withPathProvider_usesEnvironmentName()
            throws InterruptedException, ExecutionException, TimeoutException {
        PathProvider testProvider =
                new PathProvider() {
                    @Override
                    public String resolvePath(String file) {
                        return file;
                    }

                    @Override
                    public String getEnvironmentName() {
                        return "testenv";
                    }
                };

        JSONHandler handler = new JSONHandler(testProvider);
        TestData data = new TestData();

        handler.addRoute("/envroute", data);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/testenv/envroute"));

        assertEquals(200, response.statusCode);
    }

    @Test
    void addRoute_multipleRoutes_allAccessible()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();

        TestData data1 = new TestData();
        data1.name = "first";

        TestData data2 = new TestData();
        data2.name = "second";

        handler.addRoute("/route1", data1);
        handler.addRoute("/route2", data2);

        HttpResult response1 = awaitRequest(handler, startRequest("GET", "/default/route1"));
        HttpResult response2 = awaitRequest(handler, startRequest("GET", "/default/route2"));

        assertTrue(response1.body.contains("\"first\""));
        assertTrue(response2.body.contains("\"second\""));
    }

    @Test
    void addRoute_isIdempotent_canBeCalledMultipleTimes()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();

        TestData data1 = new TestData();
        data1.name = "original";
        data1.value = 100;

        TestData data2 = new TestData();
        data2.name = "updated";
        data2.value = 200;

        handler.addRoute("/idempotent", data1);
        handler.addRoute("/idempotent", data2);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/default/idempotent"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"updated\""));
        assertTrue(response.body.contains("200"));
    }

    @Test
    void putRequest_updatesObject()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "original";
        data.value = 100;

        handler.addRoute("/puttest", data);

        HttpResult response =
                awaitRequest(
                        handler,
                        startRequest(
                                "PUT", "/default/puttest", "{\"name\":\"updated\",\"value\":999}"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"updated\""));
        assertTrue(response.body.contains("999"));
        assertEquals("updated", data.name);
        assertEquals(999, data.value);
    }

    @Test
    void putRequest_partialUpdate()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "original";
        data.value = 100;

        handler.addRoute("/partialput", data);

        HttpResult response =
                awaitRequest(
                        handler,
                        startRequest("PUT", "/default/partialput", "{\"name\":\"newname\"}"));

        assertEquals(200, response.statusCode);
        assertEquals("newname", data.name);
    }

    @Test
    void postRequest_invokesCallback()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "test";
        data.value = 42;

        AtomicBoolean callbackInvoked = new AtomicBoolean(false);
        AtomicReference<String> capturedName = new AtomicReference<>();

        handler.addRoute("/posttest", data);
        handler.registerPostCallback(
                "/posttest",
                (TestData obj) -> {
                    callbackInvoked.set(true);
                    capturedName.set(obj.name);
                    return true;
                });

        HttpResult response = awaitRequest(handler, startRequest("POST", "/default/posttest"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"success\":true"));
        assertTrue(response.body.contains("\"data\""));
        assertTrue(callbackInvoked.get());
        assertEquals("test", capturedName.get());
    }

    @Test
    void postRequest_withoutCallback_returnsError()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();

        handler.addRoute("/nopostcallback", data);

        HttpResult response =
                awaitRequest(handler, startRequest("POST", "/default/nopostcallback"));

        assertEquals(400, response.statusCode);
    }

    @Test
    void putAndPostWorkTogether()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "initial";
        data.value = 0;

        AtomicReference<Integer> capturedValue = new AtomicReference<>();

        handler.addRoute("/combined", data);
        handler.registerPostCallback(
                "/combined",
                (TestData obj) -> {
                    capturedValue.set(obj.value);
                    return true;
                });

        HttpResult putResponse =
                awaitRequest(
                        handler,
                        startRequest(
                                "PUT",
                                "/default/combined",
                                "{\"name\":\"modified\",\"value\":555}"));
        assertEquals(200, putResponse.statusCode);

        HttpResult postResponse = awaitRequest(handler, startRequest("POST", "/default/combined"));
        assertEquals(200, postResponse.statusCode);
        assertEquals(555, capturedValue.get());
    }

    @Test
    void dataWithUnits_getRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnits data = new DataWithUnits();
        data.distance = Units.Meters.of(3.14);
        data.velocity = Units.MetersPerSecond.of(9.8);
        data.label = "measurement";

        handler.addRoute("/units", data);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/default/units"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"label\""));
        assertTrue(response.body.contains("\"measurement\""));
        assertTrue(response.body.contains("distance"));
        assertTrue(response.body.contains("velocity"));
    }

    @Test
    void dataWithUnits_putRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnits data = new DataWithUnits();
        data.distance = Units.Meters.of(1.0);
        data.velocity = Units.MetersPerSecond.of(1.0);
        data.label = "original";

        handler.addRoute("/unitsput", data);

        HttpResult response =
                awaitRequest(
                        handler,
                        startRequest(
                                "PUT",
                                "/default/unitsput",
                                "{\"label\":\"updated\",\"distance\":{\"value\":5.5,\"unit\":\"Meters\"},"
                                    + "\"velocity\":{\"value\":10.0,\"unit\":\"MetersPerSecond\"}}"));

        assertEquals(200, response.statusCode);
        assertEquals("updated", data.label);
    }

    @Test
    void dataWithUnits_postCallback()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnits data = new DataWithUnits();
        data.distance = Units.Meters.of(2.5);
        data.velocity = Units.MetersPerSecond.of(5.0);
        data.label = "sensor1";

        AtomicReference<Double> capturedDistance = new AtomicReference<>();

        handler.addRoute("/unitspost", data);
        handler.registerPostCallback(
                "/unitspost",
                (DataWithUnits obj) -> {
                    capturedDistance.set(obj.distance.in(Units.Meters));
                    return true;
                });

        HttpResult response = awaitRequest(handler, startRequest("POST", "/default/unitspost"));

        assertEquals(200, response.statusCode);
        assertEquals(2.5, capturedDistance.get(), 0.001);
    }

    @Test
    void dataWithArray_getRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithArray data = new DataWithArray();
        data.name = "arraytest";
        data.values = new ArrayList<>(List.of(10, 20, 30, 40));
        data.tags = new ArrayList<>(List.of("x", "y", "z"));

        handler.addRoute("/array", data);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/default/array"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"arraytest\""));
        assertTrue(response.body.contains("10"));
        assertTrue(response.body.contains("20"));
        assertTrue(response.body.contains("30"));
        assertTrue(response.body.contains("40"));
        assertTrue(response.body.contains("\"x\""));
        assertTrue(response.body.contains("\"y\""));
        assertTrue(response.body.contains("\"z\""));
    }

    @Test
    void dataWithArray_putRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithArray data = new DataWithArray();
        data.name = "original";
        data.values = new ArrayList<>(List.of(1, 2, 3));
        data.tags = new ArrayList<>(List.of("a", "b"));

        handler.addRoute("/arrayput", data);

        HttpResult response =
                awaitRequest(
                        handler,
                        startRequest(
                                "PUT",
                                "/default/arrayput",
                                "{\"name\":\"modified\",\"values\":[100,200,300,400,500],"
                                        + "\"tags\":[\"tag1\",\"tag2\",\"tag3\"]}"));

        assertEquals(200, response.statusCode);
        assertEquals("modified", data.name);
        assertEquals(5, data.values.size());
        assertEquals(100, data.values.get(0));
        assertEquals(500, data.values.get(4));
        assertEquals(3, data.tags.size());
        assertEquals("tag1", data.tags.get(0));
    }

    @Test
    void dataWithArray_postCallback()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithArray data = new DataWithArray();
        data.name = "arraytest";
        data.values = new ArrayList<>(List.of(5, 10, 15));
        data.tags = new ArrayList<>(List.of("one", "two"));

        AtomicReference<Integer> capturedSum = new AtomicReference<>();
        AtomicReference<Integer> capturedTagCount = new AtomicReference<>();

        handler.addRoute("/arraypost", data);
        handler.registerPostCallback(
                "/arraypost",
                (DataWithArray obj) -> {
                    capturedSum.set(obj.values.stream().mapToInt(Integer::intValue).sum());
                    capturedTagCount.set(obj.tags.size());
                    return true;
                });

        HttpResult response = awaitRequest(handler, startRequest("POST", "/default/arraypost"));

        assertEquals(200, response.statusCode);
        assertEquals(30, capturedSum.get());
        assertEquals(2, capturedTagCount.get());
    }

    @Test
    void unsupportedMethod_returns405()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();

        handler.addRoute("/methodtest", data);

        HttpResult response = awaitDirectRequest(startRequest("DELETE", "/default/methodtest"));

        assertEquals(405, response.statusCode);
    }

    @Test
    void registerPostCallback_withoutRoute_throwsException() {
        JSONHandler handler = new JSONHandler();

        assertThrows(
                IllegalArgumentException.class,
                () -> handler.registerPostCallback("/nonexistent", (TestData obj) -> true));
    }

    @Test
    void postRequest_returnsFalse_whenCallbackReturnsFalse()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "test";
        data.value = 42;

        handler.addRoute("/postfalse", data);
        handler.registerPostCallback("/postfalse", (TestData obj) -> false);

        HttpResult response = awaitRequest(handler, startRequest("POST", "/default/postfalse"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"success\":false"));
        assertTrue(response.body.contains("\"data\""));
    }

    @Test
    void dataWithUnitArray_getRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnitArray data = new DataWithUnitArray();
        data.name = "test_angles";
        data.angles =
                new Angle[] {
                    Units.Degrees.of(45),
                    Units.Degrees.of(90),
                    Units.Degrees.of(135),
                    Units.Degrees.of(180)
                };
        data.distances =
                new Distance[] {Units.Meters.of(1.5), Units.Meters.of(3.0), Units.Meters.of(4.5)};

        handler.addRoute("/unitarray", data);

        HttpResult response = awaitRequest(handler, startRequest("GET", "/default/unitarray"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"test_angles\""));
        assertTrue(response.body.contains("\"angles\""));
        assertTrue(response.body.contains("\"distances\""));
        assertTrue(response.body.contains("\"value\""));
        assertTrue(response.body.contains("\"unit\""));
    }

    @Test
    void dataWithUnitArray_putRequest()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnitArray data = new DataWithUnitArray();
        data.name = "original";
        data.angles = new Angle[] {Units.Degrees.of(0)};
        data.distances = new Distance[] {Units.Meters.of(1.0)};

        handler.addRoute("/unitarrayput", data);

        HttpResult response =
                awaitRequest(
                        handler,
                        startRequest(
                                "PUT",
                                "/default/unitarrayput",
                                "{\"name\":\"updated\","
                                        + "\"angles\":["
                                        + "{\"value\":30.0,\"unit\":\"Degrees\"},"
                                        + "{\"value\":60.0,\"unit\":\"Degrees\"},"
                                        + "{\"value\":90.0,\"unit\":\"Degrees\"}],"
                                        + "\"distances\":["
                                        + "{\"value\":5.0,\"unit\":\"Meters\"},"
                                        + "{\"value\":10.0,\"unit\":\"Meters\"}]}"));

        assertEquals(200, response.statusCode);
        assertEquals("updated", data.name);
        assertEquals(3, data.angles.length);
        assertEquals(30.0, data.angles[0].in(Units.Degrees), 0.001);
        assertEquals(60.0, data.angles[1].in(Units.Degrees), 0.001);
        assertEquals(90.0, data.angles[2].in(Units.Degrees), 0.001);
        assertEquals(2, data.distances.length);
        assertEquals(5.0, data.distances[0].in(Units.Meters), 0.001);
        assertEquals(10.0, data.distances[1].in(Units.Meters), 0.001);
    }

    @Test
    void dataWithUnitArray_postCallback()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        DataWithUnitArray data = new DataWithUnitArray();
        data.name = "callback_test";
        data.angles =
                new Angle[] {Units.Degrees.of(10), Units.Degrees.of(20), Units.Degrees.of(30)};
        data.distances = new Distance[] {Units.Meters.of(2.0), Units.Meters.of(4.0)};

        AtomicReference<Double> capturedAngleSum = new AtomicReference<>();
        AtomicReference<Double> capturedDistanceSum = new AtomicReference<>();
        AtomicReference<Integer> capturedAngleCount = new AtomicReference<>();

        handler.addRoute("/unitarraypost", data);
        handler.registerPostCallback(
                "/unitarraypost",
                (DataWithUnitArray obj) -> {
                    double angleSum = 0;
                    for (Angle angle : obj.angles) {
                        angleSum += angle.in(Units.Degrees);
                    }
                    capturedAngleSum.set(angleSum);

                    double distanceSum = 0;
                    for (Distance distance : obj.distances) {
                        distanceSum += distance.in(Units.Meters);
                    }
                    capturedDistanceSum.set(distanceSum);
                    capturedAngleCount.set(obj.angles.length);
                    return true;
                });

        HttpResult response = awaitRequest(handler, startRequest("POST", "/default/unitarraypost"));

        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"success\":true"));
        assertEquals(60.0, capturedAngleSum.get(), 0.001);
        assertEquals(6.0, capturedDistanceSum.get(), 0.001);
        assertEquals(3, capturedAngleCount.get());
    }

    @Test
    void getRequest_runsSerializationOnOwnerThread()
            throws InterruptedException, ExecutionException, TimeoutException {
        ThreadTrackedValueAdapter adapter = new ThreadTrackedValueAdapter();
        JSONSyncConfig config =
                new JSONSyncConfigBuilder()
                        .addJsonTypeAdapter(ThreadTrackedValue.class, adapter)
                        .build();
        JSONHandler handler = new JSONHandler(config);
        ThreadTrackedData data = new ThreadTrackedData();
        data.tracked.value = "queued";

        handler.addRoute("/threadedget", data);

        CompletableFuture<HttpResult> future = startRequest("GET", "/default/threadedget");
        Thread.sleep(100);

        assertFalse(future.isDone());
        assertNull(adapter.writeThread.get());

        HttpResult response = awaitRequest(handler, future);

        assertEquals(200, response.statusCode);
        assertEquals(Thread.currentThread(), adapter.writeThread.get());
        assertTrue(response.body.contains("\"queued\""));
    }

    @Test
    void putRequest_runsDeserializationAndMutationOnOwnerThread()
            throws InterruptedException, ExecutionException, TimeoutException {
        ThreadTrackedValueAdapter adapter = new ThreadTrackedValueAdapter();
        JSONSyncConfig config =
                new JSONSyncConfigBuilder()
                        .addJsonTypeAdapter(ThreadTrackedValue.class, adapter)
                        .build();
        JSONHandler handler = new JSONHandler(config);
        ThreadTrackedData data = new ThreadTrackedData();
        data.tracked.value = "before";

        handler.addRoute("/threadedput", data);

        CompletableFuture<HttpResult> future =
                startRequest("PUT", "/default/threadedput", "{\"tracked\":{\"value\":\"after\"}}");
        Thread.sleep(100);

        assertFalse(future.isDone());
        assertEquals("before", data.tracked.value);
        assertNull(adapter.readThread.get());

        HttpResult response = awaitRequest(handler, future);

        assertEquals(200, response.statusCode);
        assertEquals(Thread.currentThread(), adapter.readThread.get());
        assertEquals("after", data.tracked.value);
    }

    @Test
    void postRequest_runsCallbackOnOwnerThread()
            throws InterruptedException, ExecutionException, TimeoutException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();

        handler.addRoute("/threadedpost", data);
        handler.registerPostCallback(
                "/threadedpost",
                (TestData obj) -> {
                    callbackThread.set(Thread.currentThread());
                    return true;
                });

        CompletableFuture<HttpResult> future = startRequest("POST", "/default/threadedpost");
        Thread.sleep(100);

        assertFalse(future.isDone());
        assertNull(callbackThread.get());

        HttpResult response = awaitRequest(handler, future);

        assertEquals(200, response.statusCode);
        assertEquals(Thread.currentThread(), callbackThread.get());
        assertTrue(response.body.contains("\"success\":true"));
    }
}
