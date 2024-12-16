package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

/**
 * An implementation of {@link CameraIO} that integrates with PhotonVision cameras for real-time
 * pose estimation using AprilTags. This class handles camera updates, data simulation, and
 * field-to-robot transformations.
 *
 * <p>This class supports both real-world and simulated cameras and provides utility methods for
 * processing tag data such as distances and yaw angles.
 */
public class CameraIOPhoton implements CameraIO {

    private final PhotonCamera camera;
    private final PhotonPoseEstimator poseEstimator;
    private double latestTimestampSeconds = 0.0;

    /**
     * Creates a new {@code CameraIOPhoton} instance using a camera name, field layout, and the
     * robot-to-camera transform.
     *
     * @param name The name of the camera.
     * @param layout The {@link AprilTagFieldLayout} describing the field's AprilTag configuration.
     * @param robotToCamera The {@link Transform3d} representing the transformation from the robot
     *     to the camera.
     */
    public CameraIOPhoton(String name, AprilTagFieldLayout layout, Transform3d robotToCamera) {
        this(new PhotonCamera(name), layout, robotToCamera);
    }

    /**
     * Creates a new {@code CameraIOPhoton} instance using an existing PhotonCamera object, field
     * layout, and the robot-to-camera transform.
     *
     * @param camera The {@link PhotonCamera} instance to use.
     * @param layout The {@link AprilTagFieldLayout} describing the field's AprilTag configuration.
     * @param robotToCamera The {@link Transform3d} representing the transformation from the robot
     *     to the camera.
     */
    public CameraIOPhoton(
            PhotonCamera camera, AprilTagFieldLayout layout, Transform3d robotToCamera) {
        this.camera = camera;

        poseEstimator =
                new PhotonPoseEstimator(
                        layout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, robotToCamera);
        poseEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
    }

    /**
     * Factory method for creating a {@code CameraIOPhoton} instance for real cameras.
     *
     * @param params The {@link CameraParams} object containing camera parameters.
     * @param layout The {@link AprilTagFieldLayout} describing the field's AprilTag configuration.
     * @return A {@code CameraIOPhoton} instance configured for real cameras.
     */
    public static CameraIOPhoton fromRealCameraParams(
            CameraParams params, AprilTagFieldLayout layout) {
        return new CameraIOPhoton(params.name(), layout, params.robotToCamera());
    }

    /**
     * Factory method for creating a {@code CameraIOPhoton} instance for simulated cameras.
     *
     * @param params The {@link CameraParams} object containing camera parameters.
     * @param layout The {@link AprilTagFieldLayout} describing the field's AprilTag configuration.
     * @param sim The {@link VisionSystemSim} instance managing the simulation.
     * @param stream Whether to enable raw and processed streams for the simulation.
     * @return A {@code CameraIOPhoton} instance configured for simulated cameras.
     */
    public static CameraIOPhoton fromSimCameraParams(
            CameraParams params, AprilTagFieldLayout layout, VisionSystemSim sim, boolean stream) {
        PhotonCamera camera = new PhotonCamera(params.name());

        SimCameraProperties props = new SimCameraProperties();
        props.setCalibration(params.xResolution(), params.yResolution(), params.fov());
        props.setFPS(params.fps());
        props.setCalibError(0.25, 0.08);

        PhotonCameraSim cameraSim = new PhotonCameraSim(camera, props);
        sim.addCamera(cameraSim, params.robotToCamera());

        cameraSim.enableRawStream(stream);
        cameraSim.enableProcessedStream(stream);

        return new CameraIOPhoton(camera, layout, params.robotToCamera());
    }

    /**
     * Updates the {@link CameraIOInputs} object with the latest data from the PhotonVision camera.
     *
     * @param inputs The {@link CameraIOInputs} object to populate with the latest camera data.
     */
    @Override
    public void updateInputs(CameraIOInputs inputs) {
        inputs.connected = camera.isConnected();

        PhotonPipelineResult result = camera.getLatestResult();
        if (result.getTimestampSeconds() <= latestTimestampSeconds) {
            inputs.isNewMeasurement = false;
            inputs.wasAccepted = false;
            return;
        }
        inputs.isNewMeasurement = true;
        latestTimestampSeconds = result.getTimestampSeconds();
        Optional<EstimatedRobotPose> photonPose = poseEstimator.update(result);

        photonPose.ifPresentOrElse(
                (pose) -> {
                    calculateAverageTagDistance(pose, inputs);
                    inputs.latestFieldToRobot = pose.estimatedPose;

                    inputs.latestTimestampSeconds = this.latestTimestampSeconds;
                    inputs.averageTagYaw = calculateAverageTagYaw(pose);

                    inputs.wasAccepted = true;
                },
                () -> {
                    inputs.wasAccepted = false;
                });
    }

    /**
     * Calculates the average distance from the robot to detected AprilTags.
     *
     * @param pose The {@link EstimatedRobotPose} containing the detected tags.
     * @param inputs The {@link CameraIOInputs} object to populate with the calculated distance and
     *     tag count.
     */
    private void calculateAverageTagDistance(EstimatedRobotPose pose, CameraIOInputs inputs) {
        double distance = 0.0;
        int numTags = 0;
        for (PhotonTrackedTarget target : pose.targetsUsed) {
            var tagPose = poseEstimator.getFieldTags().getTagPose(target.getFiducialId());
            if (tagPose.isEmpty()) {
                continue;
            }
            numTags += 1;
            distance +=
                    tagPose.get()
                            .toPose2d()
                            .getTranslation()
                            .getDistance(pose.estimatedPose.toPose2d().getTranslation());
        }
        distance /= pose.targetsUsed.size();

        inputs.nTags = numTags;
        inputs.averageTagDistanceM = distance;
    }

    /**
     * Calculates the average yaw of detected AprilTags.
     *
     * @param pose The {@link EstimatedRobotPose} containing the detected tags.
     * @return The average yaw as a {@link Rotation2d}.
     */
    private static Rotation2d calculateAverageTagYaw(EstimatedRobotPose pose) {
        double yawRad = 0.0;
        for (PhotonTrackedTarget target : pose.targetsUsed) {
            yawRad += target.getBestCameraToTarget().getRotation().getZ();
        }
        yawRad /= pose.targetsUsed.size();
        yawRad -= Math.PI * Math.signum(yawRad);

        return Rotation2d.fromRadians(yawRad);
    }
}
