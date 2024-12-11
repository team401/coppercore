package coppercore.parameter_tools.test;

import coppercore.parameter_tools.JSONSync;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import coppercore.parameter_tools.JSONSyncConfigBuilder;

public class JSONSyncTests {

    public static final String DIRECTORY = new File("").getAbsolutePath();
    public static final String BUILD_DIRECTORY = DIRECTORY + "/build";
    public static final String RESOURCE_DIRECTORY = BUILD_DIRECTORY + "/resources/test";

    @BeforeEach
    public void TestPrep() {
        ExampleJsonSyncClass.synced =
                new JSONSync<ExampleJsonSyncClass>(
                        new ExampleJsonSyncClass(),
                        RESOURCE_DIRECTORY + "/ExampleJsonSyncClass.json",
                        new JSONSyncConfigBuilder().setPrettyPrinting(true).build());
    }

    @Test
    public void JsonSyncLoadDataTest() {
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testingIntField);
        Assertions.assertNull(instance.motorData);
    }

    @Test
    public void JsonSyncSetFileTest() {
        ExampleJsonSyncClass.synced.setFile(RESOURCE_DIRECTORY + "/SetFileTest.json");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testingIntField);
        Assertions.assertNotNull(instance.motorData);
        Assertions.assertEquals(-12.3, instance.motorData.minVoltage);
        Assertions.assertEquals(16.4, instance.motorData.maxVoltage);
        Assertions.assertEquals(0.0, instance.motorData.currentVoltage);
    }

    @Test
    public void JsonSyncSaveFileTest() {
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        instance.testingIntField = 10;
        ExampleJsonSyncClass.synced.setFile(RESOURCE_DIRECTORY + "/SaveFileTest.json");
        ExampleJsonSyncClass.synced.saveData();
    }
}
