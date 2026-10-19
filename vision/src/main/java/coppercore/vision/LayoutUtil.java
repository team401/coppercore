package coppercore.vision;

import java.io.IOException;
import java.nio.file.Path;
import org.wpilib.driverstation.DriverStationErrors;
import org.wpilib.fields.Field;
import org.wpilib.system.Filesystem;

/* This is not currently used in the 2026 robot code, it uses
 * frc.robot.constants.AprilTagConstants.getTagLayout() instead.
 * TODO: refactor
 */
/** Utility class for loading an AprilTag field layout from a JSON file. */
public class LayoutUtil {
    /**
     * Initializes an AprilTag field layout from a JSON file located in the deploy directory. If the
     * layout fails to load, an empty layout is returned and a warning is logged.
     *
     * @param name The name of the layout file (without the .json extension).
     * @return The initialized {@link Field}.
     */
    public static Field initLayout(String name) {
        Field layout;
        try {
            layout =
                    Field.loadFromFile(
                            Path.of(
                                    Filesystem.getDeployDirectory().getAbsolutePath(),
                                    "taglayout",
                                    name + ".json"));
        } catch (IOException ioe) {
            DriverStationErrors.reportWarning(
                    "Failed to load AprilTag Layout: " + ioe.getLocalizedMessage(), false);
            layout = new Field();
        }
        return layout;
    }
}
