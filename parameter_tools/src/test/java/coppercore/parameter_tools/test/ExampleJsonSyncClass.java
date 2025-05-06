package coppercore.parameter_tools.test;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONName;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import java.util.List;

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

    public final BasicMotorDataHolder motorData = null;
    public final Pose2d pose = new Pose2d(new Translation2d(3.5, 3.2), new Rotation2d(0.47));
    public final List<Action> actions =
            List.of(
                    new Start(false, false, "test"),
                    new Wait(true, "testText", 0),
                    new None(false, "Text"),
                    new Wait(true, "", 324),
                    new Finish(true, "", "Random Reason"));

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

    @JsonType(
            property = "type",
            subtypes = {
                @JsonSubtype(clazz = Start.class, name = "start"),
                @JsonSubtype(clazz = Wait.class, name = "wait"),
                @JsonSubtype(clazz = Finish.class, name = "finish"),
                @JsonSubtype(clazz = None.class, name = "none"),
            })
    public static class Action {
        public Boolean printMessage = false;
        public String message = "";
        public String type = "none";

        public Action(String type, boolean printMessage, String message) {
            this.type = type;
            this.printMessage = printMessage;
            this.message = message;
        }

        public boolean doPrint() {
            return printMessage;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }
    }

    public class Start extends Action {
        public Boolean debug = false;

        public Start(boolean debug, boolean printMessage, String message) {
            super("start", printMessage, message);
            this.debug = debug;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    public class None extends Action {

        public None(boolean printMessage, String message) {
            super("none", printMessage, message);
        }
    }

    public class Wait extends Action {
        public Integer time = 0;

        public Wait(boolean printMessage, String message, int time) {
            super("wait", printMessage, message);
            this.time = time;
        }

        public int getTime() {
            return time;
        }
    }

    public class Finish extends Action {
        public String reason = "";

        public Finish(boolean printMessage, String message, String reason) {
            super("finish", printMessage, message);
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}
