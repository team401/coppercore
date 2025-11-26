package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
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

        /**
         * Return observation to "best" target, where best target is determined as per
         * PhotonTargetSortMode - which could be "largest". I was unable to find where
         * PhotonTargetSortMode is set. It is not clear why just picking the best is useful; this is
         * used in a method VisionLocalizer.getTargetX, which however isn't used anywhere in
         * coppercore or the 2025-Robot-Code.
         */
        public TargetObservation latestTargetObservation =
                new TargetObservation(new Rotation2d(), new Rotation2d());

        /** Array of pose observations, see {@link coppercore.vision.VisionIO.PoseObservation} */
        public PoseObservation[] poseObservations = new PoseObservation[0];

        /**
         * Array of single tag observations, see {@link
         * coppercore.vision.VisionIO.SingleTagObservation}
         */
        public SingleTagObservation[] singleTagObservations = new SingleTagObservation[0];

        /** Array with all distinct fiducial ids */
        public int[] tagIds = new int[0];

        /** Average distance to targets (unclear what this is for) */
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
     *     target, the distance to the target; if the pose estimage is based on multiple, their
     *     average - which doesn't appear very useful; it's also not used anywhere other than being
     *     logged.)
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

    public default void updateInputs(VisionIOInputs inputs) {}

    public default void setAprilTagLayout(AprilTagFieldLayout tagLayout) {}
}
