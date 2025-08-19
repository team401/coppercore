package coppercore.math;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

public final class LinearDriveMath {
    private LinearDriveMath() {} // Prevent class from being instantiated

    public static double getAngleBetweenVectors(Translation2d u, Translation2d v) {
        double uNorm = u.getNorm();
        double vNorm = v.getNorm();
        double dotProduct = u.toVector().dot(v.toVector());

        // Avoid cases that break acos() and return 0.0 in these cases.
        if (uNorm * vNorm < 1e-10 || dotProduct > (uNorm * vNorm)) {
            return 0.0;
        }
        return Math.acos(dotProduct / (uNorm * vNorm));
    }

    public static Pose2d findPhase1Pose(Pose2d phase2Pose, double kDriveToPointPhase2Distance) {
        // Project the goal pose backwards to find the phase 1 goal pose.
        double phase1OffsetX =
                kDriveToPointPhase2Distance
                        * Math.cos(phase2Pose.getRotation().getRadians() + Math.PI);
        double phase1OffsetY =
                kDriveToPointPhase2Distance
                        * Math.sin(phase2Pose.getRotation().getRadians() + Math.PI);
        return new Pose2d(
                phase2Pose.getX() + phase1OffsetX,
                phase2Pose.getY() + phase1OffsetY,
                phase2Pose.getRotation());
    }
}
