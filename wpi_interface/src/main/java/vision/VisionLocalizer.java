package coppercore.wpilib_interface.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Optional;
import java.util.function.Consumer;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.targeting.PhotonPipelineResult;

/** A reliable vision subsystem to be used in any robot project */
public class VisionLocalizer extends SubsystemBase {
    CameraWrapper cameras[];

    Consumer<VisionMeasurement> visionMeasurementConsumer;

    /**
     * Create a vision localizer
     *
     * @param cameraParams A list of parameters describing the cameras on the robot
     * @param fieldLayout The layout of the apriltags on the field
     */
    public VisionLocalizer(CameraParams[] cameraParams, AprilTagFieldLayout fieldLayout) {
        cameras = new CameraWrapper[cameraParams.length];
        for (int i = 0; i < cameras.length; i++) {
            cameras[i] =
                    new CameraWrapper(
                            cameraParams[i].camera(),
                            new PhotonPoseEstimator(
                                    fieldLayout,
                                    PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                                    cameraParams[i].robotToCamera()));
        }
    }

    @Override
    public void periodic() {
        for (CameraWrapper camera : cameras) {
            for (PhotonPipelineResult result : camera.getCamera().getAllUnreadResults()) {
                Optional<EstimatedRobotPose> pose = camera.getPoseEstimator().update(result);
                visionMeasurementConsumer.accept(
                        // TODO: Actual standard deviations (not zero)
                        new VisionMeasurement(
                                pose.get().estimatedPose.toPose2d(), result.getTimestampSeconds(), VecBuilder.fill(0.0, 0.0, Double.MAX_VALUE)));
            }
        }
    }
}
