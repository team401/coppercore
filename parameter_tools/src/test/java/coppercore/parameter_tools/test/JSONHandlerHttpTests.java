package coppercore.parameter_tools.test;

import static org.junit.jupiter.api.Assertions.*;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.path_provider.PathProvider;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void addRoute_servesJsonAtDefaultEnvironment() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "hello";
        data.value = 123;

        handler.addRoute("/testroute", data);

        URL url = new URL("http://localhost:8088/default/testroute");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        assertEquals("application/json", conn.getContentType());

        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"name\""), "Response should contain name field: " + response);
        assertTrue(
                response.contains("\"hello\""), "Response should contain hello value: " + response);
        assertTrue(
                response.contains("\"value\""), "Response should contain value field: " + response);
        assertTrue(response.contains("123"), "Response should contain 123 value: " + response);
    }

    @Test
    void addRoute_withPathProvider_usesEnvironmentName() throws IOException {
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

        URL url = new URL("http://localhost:8088/testenv/envroute");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
    }

    @Test
    void addRoute_multipleRoutes_allAccessible() throws IOException {
        JSONHandler handler = new JSONHandler();

        TestData data1 = new TestData();
        data1.name = "first";

        TestData data2 = new TestData();
        data2.name = "second";

        handler.addRoute("/route1", data1);
        handler.addRoute("/route2", data2);

        URL url1 = new URL("http://localhost:8088/default/route1");
        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        String response1 =
                new String(conn1.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(response1.contains("\"first\""), "Response1 should contain first: " + response1);

        URL url2 = new URL("http://localhost:8088/default/route2");
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        String response2 =
                new String(conn2.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response2.contains("\"second\""), "Response2 should contain second: " + response2);
    }

    @Test
    void addRoute_isIdempotent_canBeCalledMultipleTimes() throws IOException {
        JSONHandler handler = new JSONHandler();

        TestData data1 = new TestData();
        data1.name = "original";
        data1.value = 100;

        TestData data2 = new TestData();
        data2.name = "updated";
        data2.value = 200;

        // Add route twice with same path - should not throw
        handler.addRoute("/idempotent", data1);
        handler.addRoute("/idempotent", data2);

        // Verify route is still accessible and serves the latest data
        URL url = new URL("http://localhost:8088/default/idempotent");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"updated\""),
                "Response should contain updated value: " + response);
        assertTrue(response.contains("200"), "Response should contain 200 value: " + response);
    }

    @Test
    void putRequest_updatesObject() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "original";
        data.value = 100;

        handler.addRoute("/puttest", data);

        // Send PUT request to update the object
        URL url = new URL("http://localhost:8088/default/puttest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody = "{\"name\":\"updated\",\"value\":999}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"updated\""),
                "Response should contain updated name: " + response);
        assertTrue(response.contains("999"), "Response should contain updated value: " + response);

        // Verify the original object was actually updated
        assertEquals("updated", data.name);
        assertEquals(999, data.value);
    }

    @Test
    void putRequest_partialUpdate() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "original";
        data.value = 100;

        handler.addRoute("/partialput", data);

        // Send PUT request with only one field
        URL url = new URL("http://localhost:8088/default/partialput");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody = "{\"name\":\"newname\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        assertEquals("newname", data.name);
        // Value should remain unchanged (default for int is 0 from deserialization,
        // but we only copy non-null values)
    }

    @Test
    void postRequest_invokesCallback() throws IOException {
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

        // Send POST request
        URL url = new URL("http://localhost:8088/default/posttest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"success\":true"),
                "Response should contain success:true: " + response);
        assertTrue(
                response.contains("\"data\""), "Response should contain data field: " + response);
        assertTrue(callbackInvoked.get(), "Callback should have been invoked");
        assertEquals("test", capturedName.get(), "Callback should receive the object");
    }

    @Test
    void postRequest_withoutCallback_returnsError() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();

        handler.addRoute("/nopostcallback", data);

        // Send POST request without registering callback
        URL url = new URL("http://localhost:8088/default/nopostcallback");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(400, conn.getResponseCode());
    }

    @Test
    void putAndPostWorkTogether() throws IOException {
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

        // First, PUT to update the object
        URL putUrl = new URL("http://localhost:8088/default/combined");
        HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setDoOutput(true);
        putConn.setRequestProperty("Content-Type", "application/json");

        String jsonBody = "{\"name\":\"modified\",\"value\":555}";
        try (OutputStream os = putConn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(200, putConn.getResponseCode());
        putConn.getInputStream().close();

        // Then, POST to trigger callback with updated data
        URL postUrl = new URL("http://localhost:8088/default/combined");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setDoOutput(true);

        assertEquals(200, postConn.getResponseCode());
        assertEquals(555, capturedValue.get(), "Callback should receive updated value");
    }

    @Test
    void dataWithUnits_getRequest() throws IOException {
        JSONHandler handler = new JSONHandler();
        DataWithUnits data = new DataWithUnits();
        data.distance = Units.Meters.of(3.14);
        data.velocity = Units.MetersPerSecond.of(9.8);
        data.label = "measurement";

        handler.addRoute("/units", data);

        URL url = new URL("http://localhost:8088/default/units");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"label\""), "Response should contain label field: " + response);
        assertTrue(
                response.contains("\"measurement\""),
                "Response should contain measurement value: " + response);
        assertTrue(
                response.contains("distance"),
                "Response should contain distance field: " + response);
        assertTrue(
                response.contains("velocity"),
                "Response should contain velocity field: " + response);
    }

    @Test
    void dataWithUnits_putRequest() throws IOException {
        JSONHandler handler = new JSONHandler();
        DataWithUnits data = new DataWithUnits();
        data.distance = Units.Meters.of(1.0);
        data.velocity = Units.MetersPerSecond.of(1.0);
        data.label = "original";

        handler.addRoute("/unitsput", data);

        // Update just the label via PUT
        URL url = new URL("http://localhost:8088/default/unitsput");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody =
                "{\"label\":\"updated\",\"distance\":{\"value\":5.5,\"unit\":\"Meters\"},\"velocity\":{\"value\":10.0,\"unit\":\"MetersPerSecond\"}}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        assertEquals("updated", data.label);
    }

    @Test
    void dataWithUnits_postCallback() throws IOException {
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

        URL url = new URL("http://localhost:8088/default/unitspost");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(200, conn.getResponseCode());
        assertEquals(2.5, capturedDistance.get(), 0.001, "Callback should receive distance value");
    }

    @Test
    void dataWithArray_getRequest() throws IOException {
        JSONHandler handler = new JSONHandler();
        DataWithArray data = new DataWithArray();
        data.name = "arraytest";
        data.values = new ArrayList<>(List.of(10, 20, 30, 40));
        data.tags = new ArrayList<>(List.of("x", "y", "z"));

        handler.addRoute("/array", data);

        URL url = new URL("http://localhost:8088/default/array");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(response.contains("\"arraytest\""), "Response should contain name: " + response);
        assertTrue(response.contains("10"), "Response should contain array value 10: " + response);
        assertTrue(response.contains("20"), "Response should contain array value 20: " + response);
        assertTrue(response.contains("30"), "Response should contain array value 30: " + response);
        assertTrue(response.contains("40"), "Response should contain array value 40: " + response);
        assertTrue(response.contains("\"x\""), "Response should contain tag x: " + response);
        assertTrue(response.contains("\"y\""), "Response should contain tag y: " + response);
        assertTrue(response.contains("\"z\""), "Response should contain tag z: " + response);
    }

    @Test
    void dataWithArray_putRequest() throws IOException {
        JSONHandler handler = new JSONHandler();
        DataWithArray data = new DataWithArray();
        data.name = "original";
        data.values = new ArrayList<>(List.of(1, 2, 3));
        data.tags = new ArrayList<>(List.of("a", "b"));

        handler.addRoute("/arrayput", data);

        URL url = new URL("http://localhost:8088/default/arrayput");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody =
                "{\"name\":\"modified\",\"values\":[100,200,300,400,500],\"tags\":[\"tag1\",\"tag2\",\"tag3\"]}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        assertEquals("modified", data.name);
        assertEquals(5, data.values.size());
        assertEquals(100, data.values.get(0));
        assertEquals(500, data.values.get(4));
        assertEquals(3, data.tags.size());
        assertEquals("tag1", data.tags.get(0));
    }

    @Test
    void dataWithArray_postCallback() throws IOException {
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

        URL url = new URL("http://localhost:8088/default/arraypost");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(200, conn.getResponseCode());
        assertEquals(30, capturedSum.get(), "Callback should receive sum of values");
        assertEquals(2, capturedTagCount.get(), "Callback should receive tag count");
    }

    @Test
    void unsupportedMethod_returns405() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();

        handler.addRoute("/methodtest", data);

        URL url = new URL("http://localhost:8088/default/methodtest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        assertEquals(405, conn.getResponseCode());
    }

    @Test
    void registerPostCallback_withoutRoute_throwsException() {
        JSONHandler handler = new JSONHandler();

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    handler.registerPostCallback("/nonexistent", (TestData obj) -> true);
                });
    }

    @Test
    void postRequest_returnsFalse_whenCallbackReturnsFalse() throws IOException {
        JSONHandler handler = new JSONHandler();
        TestData data = new TestData();
        data.name = "test";
        data.value = 42;

        handler.addRoute("/postfalse", data);
        handler.registerPostCallback(
                "/postfalse",
                (TestData obj) -> {
                    return false;
                });

        URL url = new URL("http://localhost:8088/default/postfalse");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"success\":false"),
                "Response should contain success:false: " + response);
        assertTrue(
                response.contains("\"data\""), "Response should contain data field: " + response);
    }

    @Test
    void dataWithUnitArray_getRequest() throws IOException {
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

        URL url = new URL("http://localhost:8088/default/unitarray");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"test_angles\""), "Response should contain name: " + response);
        assertTrue(
                response.contains("\"angles\""),
                "Response should contain angles array: " + response);
        assertTrue(
                response.contains("\"distances\""),
                "Response should contain distances array: " + response);
        // Verify array contains unit data (value and unit fields)
        assertTrue(
                response.contains("\"value\""),
                "Response should contain value fields for units: " + response);
        assertTrue(
                response.contains("\"unit\""),
                "Response should contain unit fields for units: " + response);
    }

    @Test
    void dataWithUnitArray_putRequest() throws IOException {
        JSONHandler handler = new JSONHandler();
        DataWithUnitArray data = new DataWithUnitArray();
        data.name = "original";
        data.angles = new Angle[] {Units.Degrees.of(0)};
        data.distances = new Distance[] {Units.Meters.of(1.0)};

        handler.addRoute("/unitarrayput", data);

        URL url = new URL("http://localhost:8088/default/unitarrayput");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody =
                "{\"name\":\"updated\","
                        + "\"angles\":["
                        + "{\"value\":30.0,\"unit\":\"Degrees\"},"
                        + "{\"value\":60.0,\"unit\":\"Degrees\"},"
                        + "{\"value\":90.0,\"unit\":\"Degrees\"}"
                        + "],"
                        + "\"distances\":["
                        + "{\"value\":5.0,\"unit\":\"Meters\"},"
                        + "{\"value\":10.0,\"unit\":\"Meters\"}"
                        + "]}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
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
    void dataWithUnitArray_postCallback() throws IOException {
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

        URL url = new URL("http://localhost:8088/default/unitarraypost");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        assertEquals(200, conn.getResponseCode());
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(
                response.contains("\"success\":true"),
                "Response should contain success:true: " + response);
        assertEquals(60.0, capturedAngleSum.get(), 0.001, "Callback should receive sum of angles");
        assertEquals(
                6.0, capturedDistanceSum.get(), 0.001, "Callback should receive sum of distances");
        assertEquals(3, capturedAngleCount.get(), "Callback should receive angle array length");
    }
}
