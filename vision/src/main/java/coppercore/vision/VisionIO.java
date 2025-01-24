package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.littletonrobotics.junction.AutoLog;

/**
 * Represents an IO wrapper for vision localizer to query. This class is responsible for updating
 * camera poses and data
 */
public interface VisionIO {
    @AutoLog
    public static class VisionIOInputs {
        public boolean connected = false;
        public TargetObservation latestTargetObservation =
                new TargetObservation(new Rotation2d(), new Rotation2d());
        public PoseObservation[] poseObservations = new PoseObservation[0];
        public SingleTagObservation latestSingleTagObservation =
                new SingleTagObservation(0, 0, 0, new Rotation2d(), new Rotation2d());
        public int[] tagIds = new int[0];
        public double averageTagDistanceM = 0;
    }

    /** Represents the angle to a simple target, not used for pose estimation. */
    public static record TargetObservation(Rotation2d tx, Rotation2d ty) {}

    /** Represents a robot pose sample used for pose estimation. */
    public static record PoseObservation(
            double timestamp,
            Pose3d pose,
            double ambiguity,
            int tagCount,
            double averageTagDistance) {}

    public static record SingleTagObservation(
            int tagId, double timestamp, double distance3D, Rotation2d tx, Rotation2d ty) {}
    ;

    public default void updateInputs(VisionIOInputs inputs) {}

    public default void setAprilTagLayout(AprilTagFieldLayout tagLayout) {}
}
