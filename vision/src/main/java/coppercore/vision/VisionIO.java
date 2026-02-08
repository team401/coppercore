package coppercore.vision;

import coppercore.math.RunOnce;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.Optional;
import java.util.function.DoubleFunction;
import org.littletonrobotics.junction.AutoLog;

/**
 * Represents an IO wrapper for vision localizer to query. This class is responsible for updating
 * camera poses and data
 */
public interface VisionIO {
    @AutoLog
    public static class VisionIOInputs {
        /** True if camera is connected. */
        public boolean connected = false;

        /** Array of pose observations, see {@link coppercore.vision.VisionIO.PoseObservation} */
        public PoseObservation[] poseObservations = new PoseObservation[0];

        /**
         * Array of single tag observations, see {@link
         * coppercore.vision.VisionIO.SingleTagObservation}
         */
        public SingleTagObservation[] singleTagObservations = new SingleTagObservation[0];

        /** Array with all distinct fiducial ids */
        public int[] tagIds = new int[0];

        /**
         * Average distance to targets, see {@link
         * coppercore.vision.VisionLocalizer#getLatestVariance(VisionIO.PoseObservation,int)}
         */
        public double averageTagDistanceM = 0;

        /** True if PhotonVision detected multiple tags in a frame. */
        public boolean hasMultitagResult = false;
    }

    /**
     * Represents the angle to a single target, not used for pose estimation.
     *
     * @param tx yaw (left/right rotation about the x-axis) to the target
     * @param ty pitch (up/down rotation about the y-axis) to the target
     */
    public static record TargetObservation(Rotation2d tx, Rotation2d ty) {}

    /**
     * Represents a robot pose sample used for pose estimation.
     *
     * @param timestamp timestamp of when frame was taken
     * @param pose pose (of robot relative to field coordinate system)
     * @param ambiguity ambiguity of pose estimate
     * @param tagCount number of fiducial ids used for pose estimate
     * @param averageTagDistance from camera to targets (if the pose estimate is based on one
     *     target, the distance to the target; if the pose estimate is based on multiple targets,
     *     their average.)
     */
    public static record PoseObservation(
            double timestamp,
            Pose3d pose,
            double ambiguity,
            int tagCount,
            double averageTagDistance) {}

    /**
     * An observation of a single AprilTag.
     *
     * @param tagId the fiducial id of the april tag
     * @param timestamp timestamp of when frame was taken
     * @param distance3D distance from best camera to target, equals norm(cameraToTarget)
     * @param cameraToTarget best camera to target transform
     * @param tx yaw (left/right rotation about the x-axis) to the target
     * @param ty pitch (up/down rotation about the y-axis) to the target
     */
    public static record SingleTagObservation(
            int tagId,
            double timestamp,
            double distance3D,
            Transform3d cameraToTarget,
            Rotation2d tx,
            Rotation2d ty) {}
    ;

    /**
     * Updates the inputs of the camera with a new robotToCamera transform; used for moving cameras.
     *
     * @param inputs the information received from the camera either in sim or the real camera
     * @param robotToCamera the transform from the robot to the camera. This is a DoubleFunction
     *     that must return the robotToCamera transform at the given timestamp in seconds.
     * @param doOnce A {@link coppercore.math.RunOnce RunOnce} instance created for each periodic
     *     cycle which Sim IOs will use to update the sim once per cycle.
     */
    public default void updateInputs(
            VisionIOInputs inputs,
            DoubleFunction<Optional<Transform3d>> robotToCamera,
            RunOnce doOnce) {}

    /**
     * Initializes the camera with the april tag layout and robot to camera transform. This is
     * called once when the camera is created.
     *
     * @param tagLayout the april tag field layout
     * @param robotToCameraAt the function to get the robot to camera transform at a given time
     */
    public default void initializeCamera(
            AprilTagFieldLayout tagLayout, DoubleFunction<Optional<Transform3d>> robotToCameraAt) {}
}
