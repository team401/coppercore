package coppercore.parameter_tools.test;

import static org.junit.jupiter.api.Assertions.*;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.path_provider.PathProvider;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class JSONHandlerHttpTests {

    public static class TestData {
        public String name = "test";
        public int value = 42;
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
}
