package coppercore.parameter_tools.test;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link JSONSync} class to validate its functionality, including data loading,
 * file setting, and data saving.
 *
 * <p>This test class uses JUnit 5 to ensure that {@link JSONSync} correctly serializes and
 * deserializes objects to and from JSON files. It also verifies that {@link JSONSync} updates files
 * when data changes.
 */
public class JSONSyncTests {

    /** Directory paths for storing and accessing test JSON files. */
    public static final String DIRECTORY = new File("").getAbsolutePath();

    public static final String BUILD_DIRECTORY = DIRECTORY + "/build";
    public static final String RESOURCE_DIRECTORY = BUILD_DIRECTORY + "/resources/test";

    /**
     * Prepares a new {@link JSONSync} instance with a predefined configuration and file path before
     * each test.
     */
    @BeforeEach
    public void TestPrep() {
        ExampleJsonSyncClass.synced =
                new JSONSync<>(
                        new ExampleJsonSyncClass(),
                        RESOURCE_DIRECTORY + "/ExampleJsonSyncClass.json",
                        new JSONSyncConfigBuilder().setPrettyPrinting(true).build());
    }

    /**
     * Tests the {@link JSONSync#loadData} method to ensure data is correctly loaded from a JSON
     * file.
     */
    @Test
    public void JsonSyncLoadDataTest() {
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        Assertions.assertEquals(10.0, instance.testDouble, "testDouble should be 10.0");
        Assertions.assertEquals(2, instance.testingIntField, "testInt should be 2");
        Assertions.assertEquals(
                0.47, instance.pose.getRotation().getRadians(), "rotation radians should be 0.47");
        Assertions.assertNull(instance.motorData, "motorData should be null");
    }

    /**
     * Tests the {@link JSONSync#setFile} method to verify it properly updates the file path and
     * loads data from the newly specified file.
     */
    @Test
    public void JsonSyncSetFileTest() {
        ExampleJsonSyncClass.synced.setFile(RESOURCE_DIRECTORY + "/SetFileTest.json");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        Assertions.assertEquals(10.0, instance.testDouble, "testDouble should be 10.0");
        Assertions.assertEquals(2, instance.testingIntField, "testInt should be 2");
        Assertions.assertNotNull(instance.motorData, "motorData should not be null");
        Assertions.assertEquals(-12.3, instance.motorData.minVoltage, "minVoltage should be -12.3");
        Assertions.assertEquals(16.4, instance.motorData.maxVoltage, "maxVoltage should be 16.4");
        Assertions.assertEquals(
                0.0, instance.motorData.currentVoltage, "currentVoltage should be 0.0");
    }

    /**
     * Tests the {@link JSONSync#saveData} method to ensure changes to an object are serialized and
     * saved to a JSON file correctly.
     */
    @Test
    public void JsonSyncSaveFileTest() {
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        instance.testingIntField = 10;
        ExampleJsonSyncClass.synced.setFile(RESOURCE_DIRECTORY + "/SaveFileTest.json");
        ExampleJsonSyncClass.synced.saveData();
    }
}
