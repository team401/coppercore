package coppercore.vision;

import coppercore.vision.VisionIO.SingleTagObservation;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
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

    private final VisionGainConstants gainConstants;

    /**
     * Constructs a new VisionLocalizer instance
     *
     * @param consumer functional interface responsible for adding vision measurements to drive pose
     * @param aprilTagLayout the field layout for current year
     * @param gainConstants a VisionGainConstants object containing the standard deviation factors
     *     and pose rejection parameters for this VisionLocalizer.
     * @param cameraStdDevFactors factors to multiply standard deviation. matches camera index
     *     (camera 0 -> index 0 in factors)
     * @param io of each camera, using photon vision or sim
     */
    public VisionLocalizer(
            VisionConsumer consumer,
            AprilTagFieldLayout aprilTagLayout,
            VisionGainConstants gainConstants,
            double[] cameraStdDevFactors,
            VisionIO... io) {
        this.consumer = consumer;
        this.io = io;
        this.aprilTagLayout = aprilTagLayout;
        this.gainConstants = gainConstants;
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

    /**
     * Returns the X angle to the best target, which can be used for simple servoing with vision.
     *
     * @param cameraIndex The index of the camera to use.
     */
    public Rotation2d getTargetX(int cameraIndex) {
        return inputs[cameraIndex].latestTargetObservation.tx();
    }

    public boolean hasMultitagResult() {
        boolean hasResult = false;
        for (VisionIOInputsAutoLogged input : inputs) {
            if (input.hasMultitagResult) {
                hasResult = true;
                break;
            }
        }

        return hasResult;
    }

    /**
     * boolean that checks whether or not a coprocessor is connected like the BeeLink
     *
     * @return camera inputs are connected
     */
    public boolean coprocessorConnected() {
        return inputs[0].connected;
    }

    /**
     * calculates the strafing and forward / reverse required for drive to be in line with a
     * specific tag + offset
     *
     * @param tagId desired tag to align to
     * @param desiredCameraIndex camera to use for measurements
     * @param crossTrackOffsetMeters how much to offset horizontal distance by
     * @param alongTrackOffsetMeters how much to offset along track distance by (if camera is pushed
     *     into robot, not aligned with bumper)
     * @return a distance to tag with validity
     */
    public DistanceToTag getDistanceErrorToTag(
            int tagId,
            int desiredCameraIndex,
            double crossTrackOffsetMeters,
            double alongTrackOffsetMeters) {
        // camera not in vision
        if (desiredCameraIndex >= inputs.length) {
            return new DistanceToTag(0, 0, false);
        }

        SingleTagObservation tagObserved = null;

        if (inputs[desiredCameraIndex].hasMultitagResult) {
            for (SingleTagObservation obs : inputs[desiredCameraIndex].singleTagObservations) {
                if (obs.tagId() == tagId) {
                    tagObserved = obs;
                    break;
                }
            }
        } else if (inputs[desiredCameraIndex].singleTagObservations.length != 0
                && inputs[desiredCameraIndex].singleTagObservations[0].tagId() == tagId) {
            tagObserved = inputs[desiredCameraIndex].singleTagObservations[0];
        } else {
            return new DistanceToTag(0, 0, false);
        }

        // get part of 3d distance lying on xy plane
        double distanceXYPlane = tagObserved.distance3D() * Math.cos(tagObserved.ty().getRadians());

        // calculate strafe and forward distances required to get to tag
        double crossTrackDistance =
                distanceXYPlane * Math.sin(tagObserved.tx().minus(new Rotation2d()).getRadians())
                        - crossTrackOffsetMeters;
        double alongTrackDistance =
                distanceXYPlane * Math.cos(tagObserved.tx().minus(new Rotation2d()).getRadians())
                        - alongTrackOffsetMeters;

        return new DistanceToTag(crossTrackDistance, alongTrackDistance, true);
    }

    /** Periodically updates the camera data and processes new measurements. */
    @Override
    public void periodic() {
        for (int i = 0; i < io.length; i++) {
            io[i].updateInputs(inputs[i]);
            Logger.processInputs("Vision/Camera" + i, inputs[i]);
        }

        // Initialize logging values
        List<Pose3d> allRobotPoses = new ArrayList<>();
        List<Pose3d> allRobotPosesAccepted = new ArrayList<>();
        List<Pose3d> allRobotPosesRejected = new ArrayList<>();

        // Loop over cameras
        for (int cameraIndex = 0; cameraIndex < io.length; cameraIndex++) {
            // Update disconnected alert
            disconnectedAlerts[cameraIndex].set(!inputs[cameraIndex].connected);

            // Initialize logging values
            List<Pose3d> robotPoses = new ArrayList<>();
            List<Pose3d> robotPosesAccepted = new ArrayList<>();
            List<Pose3d> robotPosesRejected = new ArrayList<>();

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
                // Cannot be high ambiguity if single tag
                || (observation.tagCount() == 1
                        && observation.ambiguity() > gainConstants.maxSingleTagAmbiguity)
                // Multi-tag criteria:
                || Math.abs(observation.pose().getZ())
                        > gainConstants.maxZCutoff // Must have realistic Z coordinate
                || observation.averageTagDistance() > gainConstants.maxAcceptedDistanceMeters
                || observation.ambiguity() > gainConstants.maxAmbiguity
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
                gainConstants.linearStdDevFactor * Math.pow(avgDistanceFromTarget, 2) / numTags;
        double angularStdDev =
                gainConstants.angularStdDevFactor * Math.pow(avgDistanceFromTarget, 2) / numTags;

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
     * @param cameraIndex index of camera to log
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

    public static record DistanceToTag(
            double crossTrackDistance, double alongTrackDistance, boolean isValid) {}
    ;
}
