package coppercore.parameter_tools.test;

import coppercore.parameter_tools.json.JSONName;
import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Per;

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

    public Angle angle = Units.Degree.of(340.0);

    public Per<AngleUnit, TimeUnit> test =
            Per.ofRelativeUnits(6.5, PerUnit.combine(Units.Degrees, Units.Seconds));

    public final BasicMotorDataHolder motorData = null;
    public final Pose2d pose = new Pose2d(new Translation2d(3.5, 3.2), new Rotation2d(0.47));

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
