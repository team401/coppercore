package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

/** A reliable vision subsystem to be used in any robot project */
public class VisionLocalizer extends SubsystemBase {
    CameraWrapper[] cameras;

    Consumer<VisionMeasurement> visionMeasurementConsumer;
    VisionSystemSim visionSim;
    Supplier<Pose3d> simRobotPoseSupplier;

    public final boolean isSim;

    /**
     * Create a vision localizer
     *
     * @param cameraParams A list of parameters describing the cameras on the robot
     * @param fieldLayout The layout of the apriltags on the field
     */
    public VisionLocalizer(
            CameraParams[] cameraParams, AprilTagFieldLayout fieldLayout, boolean isSim) {
        cameras = new CameraWrapper[cameraParams.length];

        this.isSim = isSim;

        if (isSim) {
            visionSim = new VisionSystemSim("coppercore-vision-sim");
            visionSim.addAprilTags(fieldLayout);
        }

        for (int i = 0; i < cameras.length; i++) {
            cameras[i] =
                    new CameraWrapper(
                            cameraParams[i].camera(),
                            new PhotonPoseEstimator(
                                    fieldLayout,
                                    PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                                    cameraParams[i].robotToCamera()));

            if (isSim) {
                PhotonCameraSim cameraSim =
                        new PhotonCameraSim(
                                cameras[i].getCamera(), cameraParams[i].simCameraProp());
                visionSim.addCamera(cameraSim, cameraParams[i].robotToCamera());
            }
        }
    }

    public void setVisionMeasurementConsumer(Consumer<VisionMeasurement> newVisionMeasurementConsumer) {
        visionMeasurementConsumer = newVisionMeasurementConsumer;
    }

    public void setSimRobotPoseSupplier(Supplier<Pose3d> newSimRobotPoseSupplier) {
        simRobotPoseSupplier = newSimRobotPoseSupplier;
    }

    @Override
    public void periodic() {
        if (isSim) {
            visionSim.update(simRobotPoseSupplier.get());
        }
        for (CameraWrapper camera : cameras) {
            for (PhotonPipelineResult result : camera.getCamera().getAllUnreadResults()) {
                Optional<EstimatedRobotPose> potentialPose =
                        camera.getPoseEstimator().update(result);

                if (potentialPose.isPresent()) {
                    EstimatedRobotPose est = potentialPose.get();
                    Pose3d pose = est.estimatedPose;
                    visionMeasurementConsumer.accept( // TODO: Actual standard deviations (not zero)
                            new VisionMeasurement(
                                    pose.toPose2d(),
                                    result.getTimestampSeconds(),
                                    VecBuilder.fill(0.0, 0.0, 0.0)));
                    Logger.recordOutput(
                            "vision/" + camera.getCamera().getName() + "/latestPoseEstimate", pose);
                }
            }
        }
    }
}
