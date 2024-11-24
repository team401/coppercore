package coppercore.parameter_tools.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import coppercore.parameter_tools.JSONSync;

public class JSONSyncTests {

    @BeforeEach
    public void TestPrep() {
        ExampleJsonSyncClass.synced =
                new JSONSync<ExampleJsonSyncClass>(
                        new ExampleJsonSyncClass(),
                        "filePath",
                        new JSONSync.JSONSyncConfigBuilder().setPrettyPrinting(true).build());
    }

    //@Test
    public void JsonSyncLoadDataTest() {
        System.out.println("LoadTest");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testInt);
        Assertions.assertNull(instance.motorData);
        System.out.println(instance);
        System.out.println("");
    }

    //@Test
    public void JsonSyncSetFileTest() {
        System.out.println("SetTest");
        ExampleJsonSyncClass.synced.setFile(
                "filePath");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testInt);
        Assertions.assertNotNull(instance.motorData);
        Assertions.assertEquals(-12.3, instance.motorData.minVoltage);
        Assertions.assertEquals(16.4, instance.motorData.maxVoltage);
        Assertions.assertEquals(0.0, instance.motorData.currentVoltage);
        System.out.println(instance);
        System.out.println("");
    }

    //@Test
    public void JsonSyncSaveFileTest() {
        System.out.println("SaveTest");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();
        System.out.println(instance);
        System.out.println("\nSaving File\n");
        instance.testInt = 10;
        ExampleJsonSyncClass.synced.setFile(
                "filePath");
        ExampleJsonSyncClass.synced.saveData();
        System.out.println(instance);
        System.out.println("");
    }
}
