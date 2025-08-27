package coppercore.parameter_tools.test;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
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

    /**
     * Prepares a new {@link JSONSync} instance with a predefined configuration and file path before
     * each test.
     */
    @BeforeEach
    public void TestPrep() {
        ExampleJsonSyncClass.synced =
                new JSONSync<>(
                        new ExampleJsonSyncClass(),
                        "ExampleJsonSyncClass.json",
                        new UnitTestingPathProvider().getDirectory("JSONSyncTests"),
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setUpPolymorphAdapter(ExampleJsonSyncClass.Action.class)
                                .build());
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
        Assertions.assertEquals(instance.actions.size(), 2);
        Assertions.assertEquals(
                instance.actions.get(0) instanceof ExampleJsonSyncClass.Start, true);
        Assertions.assertEquals(
                instance.actions.get(1) instanceof ExampleJsonSyncClass.Finish, true);
        Assertions.assertEquals(
                ((ExampleJsonSyncClass.Start) instance.actions.get(0)).isDebug(), true);
    }

    /**
     * Tests the {@link JSONSync#setFile} method to verify it properly updates the file path and
     * loads data from the newly specified file.
     */
    @Test
    public void JsonSyncSetFileTest() {
        ExampleJsonSyncClass.synced.setFile("SetFileTest.json");
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
        Integer saved_int = 106454;
        Integer random_int = 102130324;
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        instance.testingIntField = saved_int;
        ExampleJsonSyncClass.synced.setFile("SaveFileTest.json");
        ExampleJsonSyncClass.synced.saveData();
        instance.testingIntField = random_int;
        ExampleJsonSyncClass.synced.loadData();
        instance = ExampleJsonSyncClass.synced.getObject();
        Assertions.assertEquals(
                saved_int, instance.testingIntField, "testInt should be " + saved_int);
    }
}
