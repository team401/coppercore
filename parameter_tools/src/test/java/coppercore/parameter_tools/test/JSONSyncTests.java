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
        System.out.println("Test Prep");
        ExampleJsonSyncClass.synced =
                new JSONSync<>(
                        new ExampleJsonSyncClass(),
                        RESOURCE_DIRECTORY + "/ExampleJsonSyncClass.json",
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
		Assertions.assertEquals(10.0, instance.testPer.magnitude(), 0.1, "testPer magnitude should be 10.0");
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

    private static class PrimitiveClass {
        final int testInt;

        PrimitiveClass(int x) {
            testInt = x;
        }
    }

    /** Written with help from CoPilot */
    @Test
    public void JsonSyncPrimitiveCrashTest() {
        Exception e =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> {
                            JSONSync<PrimitiveClass> sync =
                                    new JSONSync<>(
                                            new PrimitiveClass(0),
                                            RESOURCE_DIRECTORY + "/PrimitiveClass.json",
                                            new JSONSyncConfigBuilder()
                                                    .setPrettyPrinting(true)
                                                    .build());
                            sync.loadData();
                        });
        System.out.println(e);
    }

    /** Class to test the primitive check crash feature. */
    private static class PrimitiveCheckCrash {
        public final int primitiveField = 10;
    }

    /** Tests the {@link JSONSync#saveData} method to ensure it crashes when a primitive is used. */
    @Test
    public void JsonSyncPrimitiveCheckCrash() {
        JSONSync<PrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new PrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/PrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

    /** Class to test the primitive check crash feature with a private field. */
    private static class PrivatePrimitiveCheckCrash {
        private final int primitiveField = 10;
    }

    /**
     * Tests the {@link JSONSync#saveData} method to ensure it crashes when a private primitive is
     * used.
     */
    @Test
    public void JsonSyncPrivatePrimitiveCheckCrash() {
        JSONSync<PrivatePrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new PrivatePrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/PrivatePrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

    /** Class to test the primitive check crash feature with a double. */
    private static class DoublePrimitiveCheckCrash {
        public final double primitiveField = 10.0;
    }

    /** Tests the {@link JSONSync#saveData} method to ensure it crashes when a double is used. */
    @Test
    public void JsonSyncDoublePrimitiveCheckCrash() {
        JSONSync<DoublePrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new DoublePrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/DoublePrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

    /** Class to test the primitive check crash feature with a boolean. */
    private static class BooleanPrimitiveCheckCrash {
        public final boolean primitiveField = true;
    }

    /** Tests the {@link JSONSync#saveData} method to ensure it crashes when a boolean is used. */
    @Test
    public void JsonSyncBooleanPrimitiveCheckCrash() {
        JSONSync<BooleanPrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new BooleanPrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/BooleanPrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

    /** Class to test the primitive check crash feature with a byte. */
    private static class BytePrimitiveCheckCrash {
        public final byte primitiveField = 1;
    }

    /** Tests the {@link JSONSync#saveData} method to ensure it crashes when a byte is used. */
    @Test
    public void JsonSyncBytePrimitiveCheckCrash() {
        JSONSync<BytePrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new BytePrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/BytePrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

    /** Class to test the primitive check crash feature with a short. */
    private static class ShortPrimitiveCheckCrash {
        public final short primitiveField = 2;
    }

    /** Tests the {@link JSONSync#saveData} method to ensure it crashes when a short is used. */
    @Test
    public void JsonSyncShortPrimitiveCheckCrash() {
        JSONSync<ShortPrimitiveCheckCrash> synced =
                new JSONSync<>(
                        new ShortPrimitiveCheckCrash(),
                        RESOURCE_DIRECTORY + "/ShortPrimitiveCheckCrash.json",
                        new JSONSyncConfigBuilder()
                                .setPrettyPrinting(true)
                                .setPrimitiveCheckCrash(true)
                                .build());
        Assertions.assertThrows(RuntimeException.class, synced::saveData);
    }

}
