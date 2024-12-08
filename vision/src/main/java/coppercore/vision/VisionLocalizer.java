package coppercore.vision;

import edu.wpi.first.math.Matrix;
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

    public VisionLocalizer(VisionConsumer consumer, VisionIO... io) {
        this.consumer = consumer;
        this.io = io;

        // Initialize inputs
        this.inputs = new VisionIOInputsAutoLogged[io.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new VisionIOInputsAutoLogged();
        }

        // Initialize disconnected alerts
        this.disconnectedAlerts = new Alert[io.length];
        for (int i = 0; i < inputs.length; i++) {
            disconnectedAlerts[i] =
                    new Alert(
                            "Vision camera " + io[cameraIndex].name + " is disconnected.",
                            AlertType.kWarning);
        }
    }

    /** Periodically updates the camera data and processes new measurements. */
    @Override
    public void periodic() {
        for (int i = 0; i < io.length; i++) {
            io[i].updateInputs(inputs[i]);
            Logger.processInputs("Vision/Camera" + io[cameraIndex].name, inputs[i]);
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

            for (PoseObservation observation : inputs[cameraIndex].poseObservations) {
                if (rejectPose(observation)) {
                    robotPosesRejected.add(observation.pose());
                    continue;
                }

                robotPosesAccepted.add(observation.pose());

                consumer.accept(
                        observation.pose().toPose2d(),
                        observation.timestamp(),
                        getLatestVariance(observation, cameraIndex));
            }

            // Log camera datadata
            Logger.recordOutput(
                    "Vision/Camera" + io[cameraIndex].name + "/RobotPoses",
                    robotPoses.toArray(new Pose3d[robotPoses.size()]));
            Logger.recordOutput(
                    "Vision/Camera" + io[cameraIndex].name + "/RobotPosesAccepted",
                    robotPosesAccepted.toArray(new Pose3d[robotPosesAccepted.size()]));
            Logger.recordOutput(
                    "Vision/Camera" + io[cameraIndex].name + "/RobotPosesRejected",
                    robotPosesRejected.toArray(new Pose3d[robotPosesRejected.size()]));
            allRobotPoses.addAll(robotPoses);
            allRobotPosesAccepted.addAll(robotPosesAccepted);
            allRobotPosesRejected.addAll(robotPosesRejected);
        }
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

    public void setVisionConsumer(VisionConsumer consumer) {
        this.consumer = consumer;
    }

    private boolean rejectPose(VisionIO.PoseObservation observation) {
        return observation.tagCount() == 0 // Must have at least one tag
                || (observation.tagCount() == 1
                        && observation.averageTagDistance()
                                > CoreVisionConstants
                                        .singleTagDistanceCutoff) // Cannot be high ambiguity
                || Math.abs(observation.pose().getZ())
                        > CoreVisionConstants.maxZError // Must have realistic Z coordinate
                || observation.averageTagDistance() > CoreVisionConstants.maxAcceptedDistanceMeters
                // Must be within the field boundaries
                || observation.pose().getX() < 0.0
                || observation.pose().getX() > aprilTagLayout.getFieldLength()
                || observation.pose().getY() < 0.0
                || observation.pose().getY() > aprilTagLayout.getFieldWidth();
    }

    private Matrix<N3, N1> getLatestVariance(
            VisionIO.PoseObservation observation, int cameraIndex) {
        Matrix<N3, N1> stdDev = CoreVisionConstants.singleTagStdDev;
        double avgDistanceFromTarget = observation.averageTagDistance();
        int numTags = observation.tagCount();
        if (numTags > 1) {
            stdDev = CoreVisionConstants.multiTagStdDev;
        }
        // distance based variance
        stdDev =
                stdDev.times(
                        1
                                + (Math.pow(avgDistanceFromTarget, 2)
                                        / CoreVisionConstants.distanceFactor));

        // adjustment based on position of camera
        if (cameraIndex < CoreVisionConstants.cameraStdDevFactors.length) {
            stdDev = stdDev.times(CoreVisionConstants.cameraStdDevFactors[cameraIndex]);
        }

        return stdDev;
    }

    @FunctionalInterface
    public static interface VisionConsumer {
        public void accept(
                Pose2d visionRobotPoseMeters,
                double timestampSeconds,
                Matrix<N3, N1> visionMeasurementStdDevs);
    }
}
