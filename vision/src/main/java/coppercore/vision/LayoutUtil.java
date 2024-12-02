package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.IOException;
import java.util.Collections;

public class LayoutUtil {
    public static AprilTagFieldLayout initLayout(String name) {
        AprilTagFieldLayout layout;
        // AprilTagFieldLayout's constructor throws an IOException, so we must catch it
        // in order to initialize our layout as a static constant
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
