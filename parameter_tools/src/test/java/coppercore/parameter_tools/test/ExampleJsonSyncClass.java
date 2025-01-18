package coppercore.parameter_tools.test;

import coppercore.parameter_tools.JSONName;
import coppercore.parameter_tools.JSONSync;
import coppercore.parameter_tools.JSONSyncConfigBuilder;

/**
 * Example class to demonstrate the usage of the {@link JSONSync} utility for saving and loading
 * data in JSON format.
 *
 * <p>This class showcases how fields can be serialized and deserialized, including nested objects,
 * using the {@link JSONSync} functionality.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Defines several fields of various types to demonstrate JSON serialization.
 *   <li>Includes a nested class {@code BasicMotorDataHolder} to demonstrate handling nested data
 *       structures.
 *   <li>Utilizes {@link JSONSync} to manage serialization and deserialization with the associated
 *       JSON file.
 * </ul>
 */
public class ExampleJsonSyncClass {

    /**
     * Static {@link JSONSync} instance for managing the serialization and deserialization of this
     * class.
     */
    public static JSONSync<ExampleJsonSyncClass> synced =
            new JSONSync<>(
                    new ExampleJsonSyncClass(), "filePath", new JSONSyncConfigBuilder().build());

    public final String testText = "";

    public final Double testDouble = 0.0;

    @JSONName("testInt")
    public Integer testingIntField = 0;

    public final BasicMotorDataHolder motorData = null;

    /** Nested class to represent motor-related data. */
    public class BasicMotorDataHolder {

        public final Double maxVoltage = 0.0;

        public final Double minVoltage = 0.0;

        public Double currentVoltage = 0.0;

        @Override
        public String toString() {
            return "minVoltage: "
                    + minVoltage
                    + "\nmaxVoltage: "
                    + maxVoltage
                    + "\ncurrentVoltage: "
                    + currentVoltage;
        }
    }

    @Override
    public String toString() {
        return "testText: "
                + testText
                + "\ntestInt: "
                + testingIntField
                + "\ntestDouble: "
                + testDouble
                + "\nmotorData: "
                + motorData;
    }
}
