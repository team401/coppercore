package coppercore.standalone;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfig;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Serializes a {@link Transform3d} and a {@link Transform2d} to JSON using coppercore's JSON
 * framework and prints the result to standard output.
 *
 * <p>Serialization is done with {@link JSONSync#serialize()} — the same call {@code JSONHandler}
 * makes internally when it serves an object over its GET route — using the default {@link
 * JSONSyncConfig} and whatever type converters the framework currently has registered. No
 * Transform-specific converters are added here; the output reflects the framework's behavior as-is.
 *
 * <pre>
 *   ./gradlew :standalone:run -PmainClass=coppercore.standalone.TransformJsonDump
 * </pre>
 */
public final class TransformJsonDump {
    private TransformJsonDump() {}

    public static void main(String[] args) {
        Transform3d transform3d =
                new Transform3d(
                        new Translation3d(1.0, 2.0, 3.0),
                        new Rotation3d(
                                Math.toRadians(10.0), Math.toRadians(20.0), Math.toRadians(30.0)));

        Transform2d transform2d =
                new Transform2d(new Translation2d(4.0, 5.0), Rotation2d.fromDegrees(45.0));

        JSONSyncConfig config = new JSONSyncConfigBuilder().build();

        System.out.println("Transform3d:");
        System.out.println(new JSONSync<>(transform3d, "", config).serialize());

        System.out.println();

        System.out.println("Transform2d:");
        System.out.println(new JSONSync<>(transform2d, "", config).serialize());
    }
}
