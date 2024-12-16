package coppercore.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.littletonrobotics.junction.AutoLog;

/**
 * Interface representing the input/output (IO) functionality for a camera in the vision system.
 * This interface provides a method to update camera inputs and includes an inner class for
 * encapsulating camera-related data.
 */
public interface CameraIO {

    /**
     * Class representing the inputs from a camera, including pose, tag information, and connection
     * status.
     */
    @AutoLog
    public static class CameraIOInputs {
        /** The most recent pose of the robot relative to the field, as calculated by the camera. */
        public Pose3d latestFieldToRobot = new Pose3d();

        /** The average distance to detected tags in meters. */
        public double averageTagDistanceM = 0.0;

        /** The average yaw (rotation) of detected tags, represented as a {@link Rotation2d}. */
        public Rotation2d averageTagYaw = new Rotation2d();

        /** The number of tags currently detected by the camera. */
        public int nTags = 0;

        /** The timestamp of the latest measurement in seconds. */
        public double latestTimestampSeconds = 0.0;

        /** Whether the camera is currently connected. */
        public boolean connected = false;

        /** Indicates if a new measurement has been received from the camera. */
        public boolean isNewMeasurement = false;

        /**
         * Indicates whether the new camera measurement was accepted by the initial filters. This is
         * always {@code false} if {@code isNewMeasurement} is {@code false}.
         */
        public boolean wasAccepted = false;
    }

    /**
     * Updates the provided {@link CameraIOInputs} object with the latest camera data. This default
     * implementation does nothing and should be overridden by implementing classes.
     *
     * @param inputs The {@link CameraIOInputs} object to update with the latest camera data.
     */
    public default void updateInputs(CameraIOInputs inputs) {}
}
