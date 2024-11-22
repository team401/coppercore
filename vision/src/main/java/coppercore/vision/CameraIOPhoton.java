package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
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

public class CameraIOPhoton implements CameraIO {
    private final PhotonCamera camera;
    private final PhotonPoseEstimator poseEstimator;

    private double latestTimestampSeconds = 0.0;

    public CameraIOPhoton(String name, AprilTagFieldLayout layout, Transform3d robotToCamera) {
        this(new PhotonCamera(name), layout, robotToCamera);
    }

    public CameraIOPhoton(
            PhotonCamera camera, AprilTagFieldLayout layout, Transform3d robotToCamera) {
        this.camera = camera;

        poseEstimator =
                new PhotonPoseEstimator(
                        layout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, robotToCamera);
    }

    public static CameraIOPhoton fromRealCameraParams(
            CameraParams params, AprilTagFieldLayout layout) {
        return new CameraIOPhoton(params.name(), layout, params.robotToCamera());
    }

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

    @Override
    public void updateInputs(CameraIOInputs inputs) {
        inputs.connected = camera.isConnected();

        PhotonPipelineResult result = camera.getLatestResult();
        if (result.getTimestampSeconds() == latestTimestampSeconds) {
            inputs.isNewMeasurement = false;
            inputs.wasAccepted = false;
            return;
        }
        inputs.isNewMeasurement = true;
        latestTimestampSeconds = result.getTimestampSeconds();
        Optional<EstimatedRobotPose> photonPose = poseEstimator.update(result);

        photonPose.filter(CameraIOPhoton::filterPhotonPose);

        photonPose.ifPresentOrElse(
                (pose) -> {
                    inputs.latestFieldToRobot = pose.estimatedPose;
                    inputs.nTags = pose.targetsUsed.size();

                    inputs.latestTimestampSeconds = this.latestTimestampSeconds;
                    inputs.averageTagDistanceM = calculateAverageTagDistance(pose);
                    inputs.averageTagYaw = calculateAverageTagYaw(pose);

                    inputs.wasAccepted = true;
                },
                () -> {
                    inputs.wasAccepted = false;
                });
    }

    private static boolean filterPhotonPose(EstimatedRobotPose photonPose) {
        // TODO: Actual pose rejection
        if (photonPose.targetsUsed.size() < 2) {
            return false;
        }

        Pose3d pose = photonPose.estimatedPose;
        // check that the pose isn't insane
        if (pose.getZ() > 1 || pose.getZ() < -0.1) {
            return false;
        }

        if (calculateAverageTagDistance(photonPose)
                > CoreVisionConstants.maxAcceptedDistanceMeters) {
            return false;
        }

        return true;
    }

    private static double calculateAverageTagDistance(EstimatedRobotPose pose) {
        double distance = 0.0;
        for (PhotonTrackedTarget target : pose.targetsUsed) {
            distance +=
                    target.getBestCameraToTarget()
                            .getTranslation()
                            .getDistance(new Translation3d());
        }
        distance /= pose.targetsUsed.size();

        return distance;
    }

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
