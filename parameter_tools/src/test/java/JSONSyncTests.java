package coppercore.paremeter_tools.test;

import coppercore.parameter_tools.JSONSync;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

public class JSONSyncTests {

    public void main(String[] args) {
        JsonSyncLoadDataTest();
    }

    @BeforeEach
    public void TestPrep() {
        ExampleJsonSyncClass.synced =
                new JSONSync<ExampleJsonSyncClass>(
                        new ExampleJsonSyncClass(),
                        "filePath",
                        new JSONSync.JSONSyncConfigBuilder().build());
    }

    // @Test
    public void JsonSyncLoadDataTest() {
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testInt);
        Assertions.assertNull(instance.motorData);
    }

    // @Test
    public void JsonSyncSetFileTest() {

        ExampleJsonSyncClass.synced.setFile("filePath");
        ExampleJsonSyncClass.synced.loadData();
        ExampleJsonSyncClass instance = ExampleJsonSyncClass.synced.getObject();

        Assertions.assertEquals(10.0, instance.testDouble);
        Assertions.assertEquals(2, instance.testInt);
        Assertions.assertNotNull(instance.motorData);
        Assertions.assertEquals(-12.3, instance.motorData.minVoltage);
        Assertions.assertEquals(16.4, instance.motorData.maxVoltage);
        Assertions.assertEquals(0.0, instance.motorData.currentVoltage);
    }
}
