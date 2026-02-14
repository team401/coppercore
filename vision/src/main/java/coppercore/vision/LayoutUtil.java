package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.IOException;
import java.util.Collections;

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
     * @return The initialized {@link AprilTagFieldLayout}.
     */
    public static AprilTagFieldLayout initLayout(String name) {
        AprilTagFieldLayout layout;
        // AprilTagFieldLayout's constructor throws an IOException, so we must catch it
        try {
            layout =
                    new AprilTagFieldLayout(
                            Filesystem.getDeployDirectory().getAbsolutePath()
                                    + "/taglayout/"
                                    + name
                                    + ".json");
        } catch (IOException ioe) {
            DriverStation.reportWarning(
                    "Failed to load AprilTag Layout: " + ioe.getLocalizedMessage(), false);
            layout = new AprilTagFieldLayout(Collections.emptyList(), 0.0, 0.0);
        }
        return layout;
    }
}
