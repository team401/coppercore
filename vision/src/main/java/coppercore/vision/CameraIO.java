package coppercore.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.littletonrobotics.junction.AutoLog;

public interface CameraIO {

    @AutoLog
    public static class CameraIOInputs {
        public Pose3d latestFieldToRobot = new Pose3d();
        public double averageTagDistanceM = 0.0;
        public Rotation2d averageTagYaw = new Rotation2d();
        public int nTags = 0;

        public double latestTimestampSeconds = 0.0;
        public boolean connected = false;
        public boolean isNewMeasurement = false;

        /**
         * Whether the new camera measurement was accepted by the initial filters. Always false if
         * `isNewMeasurement` is false.
         */
        public boolean wasAccepted = false;
    }

    public default void updateInputs(CameraIOInputs inputs) {}
}
