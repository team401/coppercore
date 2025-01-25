package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.LinkedList;
import java.util.List;
import org.littletonrobotics.junction.Logger;

/**
 * Localizes the robot using camera measurements. Periodically updates camera data and allows for
 * custom handling of new measurements.
 */
public class VisionLocalizer extends SubsystemBase {
    private final VisionIO[] io;
    private final VisionIOInputsAutoLogged[] inputs;
    private final Alert[] disconnectedAlerts;
    // avoid NullPointerExceptions by setting a default no-op
    private VisionConsumer consumer;
    public AprilTagFieldLayout aprilTagLayout;
    private double[] cameraStdDevFactors;

    /**
     * Constructs a new VisionLocalizer instance
     *
     * @param consumer functional interface responsible for adding vision measurements to drive pose
     * @param aprilTagLayout the field layout for current year
     * @param cameraStdDevFactors factors to multiply standard deviation. matches camera index
     *     (camera 0 -> index 0 in factors)
     * @param io of each camera, using photon vision or sim
     */
    public VisionLocalizer(
            VisionConsumer consumer,
            AprilTagFieldLayout aprilTagLayout,
            double[] cameraStdDevFactors,
            VisionIO... io) {
        this.consumer = consumer;
        this.io = io;
        this.aprilTagLayout = aprilTagLayout;
        this.cameraStdDevFactors = cameraStdDevFactors;

        for (int i = 0; i < io.length; i++) {
            io[i].setAprilTagLayout(aprilTagLayout);
        }

        // Initialize inputs
        this.inputs = new VisionIOInputsAutoLogged[io.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new VisionIOInputsAutoLogged();
        }

        // Initialize disconnected alerts
        this.disconnectedAlerts = new Alert[io.length];
        for (int i = 0; i < inputs.length; i++) {
            disconnectedAlerts[i] =
                    new Alert("Vision camera " + i + " is disconnected.", AlertType.kWarning);
        }
    }

    /** Periodically updates the camera data and processes new measurements. */
    @Override
    public void periodic() {
        for (int i = 0; i < io.length; i++) {
            io[i].updateInputs(inputs[i]);
            Logger.processInputs("Vision/Camera" + i, inputs[i]);
        }

        // Initialize logging values
        List<Pose3d> allRobotPoses = new LinkedList<>();
        List<Pose3d> allRobotPosesAccepted = new LinkedList<>();
        List<Pose3d> allRobotPosesRejected = new LinkedList<>();

        // Loop over cameras
        for (int cameraIndex = 0; cameraIndex < io.length; cameraIndex++) {
            // Update disconnected alert
            disconnectedAlerts[cameraIndex].set(!inputs[cameraIndex].connected);

            // Initialize logging values
            List<Pose3d> robotPoses = new LinkedList<>();
            List<Pose3d> robotPosesAccepted = new LinkedList<>();
            List<Pose3d> robotPosesRejected = new LinkedList<>();

            for (VisionIO.PoseObservation observation : inputs[cameraIndex].poseObservations) {
                robotPoses.add(observation.pose());
                if (shouldRejectPose(observation)) {
                    robotPosesRejected.add(observation.pose());
                    continue;
                }

                robotPosesAccepted.add(observation.pose());

                consumer.accept(
                        observation.pose().toPose2d(),
                        observation.timestamp(),
                        getLatestVariance(observation, cameraIndex));
            }
            logCameraData(cameraIndex, robotPoses, robotPosesAccepted, robotPosesRejected);

            allRobotPoses.addAll(robotPoses);
            allRobotPosesAccepted.addAll(robotPosesAccepted);
            allRobotPosesRejected.addAll(robotPosesRejected);
        }

        logSummaryData(allRobotPoses, allRobotPosesAccepted, allRobotPosesRejected);
    }

    /** sets a VisionConsumer for the vision to send estimates to */
    public void setVisionConsumer(VisionConsumer consumer) {
        this.consumer = consumer;
    }

    /***
     * checks if a pose measurement should be consumed
     *
     * @param observation a single observation from a camera
     * @return true if pose should be rejected due to low tags, high distance, or out of field
     */
    private boolean shouldRejectPose(VisionIO.PoseObservation observation) {
        return observation.tagCount() == 0 // Must have at least one tag
                || (observation.tagCount() == 1
                        && observation.ambiguity()
                                > CoreVisionConstants
                                        .maxSingleTagAmbiguity) // Cannot be high ambiguity if
                // single tag
                || Math.abs(observation.pose().getZ())
                        > CoreVisionConstants.maxZCutoff // Must have realistic Z coordinate
                || observation.averageTagDistance() > CoreVisionConstants.maxAcceptedDistanceMeters
                || observation.ambiguity() > 0.3
                // Must be within the field boundaries
                || observation.pose().getX() < 0.0
                || observation.pose().getX() > aprilTagLayout.getFieldLength()
                || observation.pose().getY() < 0.0
                || observation.pose().getY() > aprilTagLayout.getFieldWidth();
    }

    /**
     * calculates how much we should rely on this pose when sending it to vision consumer
     *
     * @param observation a pose estimate from a camera
     * @param cameraIndex the index of camera providing observation
     * @return a matrix representing the standard deviation factors
     */
    private Matrix<N3, N1> getLatestVariance(
            VisionIO.PoseObservation observation, int cameraIndex) {
        double avgDistanceFromTarget = observation.averageTagDistance();
        int numTags = observation.tagCount();
        double linearStdDev =
                CoreVisionConstants.linearStdDevFactor
                        * Math.pow(avgDistanceFromTarget, 2)
                        / numTags;
        double angularStdDev =
                CoreVisionConstants.angularStdDevFactor
                        * Math.pow(avgDistanceFromTarget, 2)
                        / numTags;

        // adjustment based on position of camera
        if (cameraIndex < this.cameraStdDevFactors.length) {
            linearStdDev *= this.cameraStdDevFactors[cameraIndex];
            angularStdDev *= this.cameraStdDevFactors[cameraIndex];
        }

        return VecBuilder.fill(linearStdDev, linearStdDev, angularStdDev);
    }

    /**
     * logs individual camera data to advantage kit realOutputs under Vision/camera/index
     *
     * @param cameraIndex index of camera to liog
     * @param robotPoses list of all poses found by camera
     * @param robotPosesAccepted list of poses NOT REJECTED by shouldRejectPose
     * @param robotPosesRejected list of poses REJECTED by shouldRejectPose
     */
    private void logCameraData(
            int cameraIndex,
            List<Pose3d> robotPoses,
            List<Pose3d> robotPosesAccepted,
            List<Pose3d> robotPosesRejected) {
        // Log camera datadata
        Logger.recordOutput(
                "Vision/Camera" + cameraIndex + "/RobotPoses",
                robotPoses.toArray(new Pose3d[robotPoses.size()]));
        Logger.recordOutput(
                "Vision/Camera" + cameraIndex + "/RobotPosesAccepted",
                robotPosesAccepted.toArray(new Pose3d[robotPosesAccepted.size()]));
        Logger.recordOutput(
                "Vision/Camera" + cameraIndex + "/RobotPosesRejected",
                robotPosesRejected.toArray(new Pose3d[robotPosesRejected.size()]));
    }

    /**
     * logs summary data to realOutputs via Vision/Summary/
     *
     * @param allRobotPoses list of all poses found by all cameras
     * @param allRobotPosesAccepted list of poses NOT REJECTED by shouldRejectPose
     * @param allRobotPosesRejected list of poses REJECTED by shouldRejectPose
     */
    private void logSummaryData(
            List<Pose3d> allRobotPoses,
            List<Pose3d> allRobotPosesAccepted,
            List<Pose3d> allRobotPosesRejected) {
        Logger.recordOutput(
                "Vision/Summary/RobotPoses",
                allRobotPoses.toArray(new Pose3d[allRobotPoses.size()]));
        Logger.recordOutput(
                "Vision/Summary/RobotPosesAccepted",
                allRobotPosesAccepted.toArray(new Pose3d[allRobotPosesAccepted.size()]));
        Logger.recordOutput(
                "Vision/Summary/RobotPosesRejected",
                allRobotPosesRejected.toArray(new Pose3d[allRobotPosesRejected.size()]));
    }

    @FunctionalInterface
    public static interface VisionConsumer {
        public void accept(
                Pose2d visionRobotPoseMeters,
                double timestampSeconds,
                Matrix<N3, N1> visionMeasurementStdDevs);
    }
}
