package coppercore.vision;

import java.util.Optional;
import java.util.function.Consumer;

import org.littletonrobotics.junction.Logger;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/** A reliable vision subsystem to be used in any robot project */
public class VisionLocalizer extends SubsystemBase {
    CameraWrapper cameras[];

    Consumer<VisionMeasurement> visionMeasurementConsumer;
    VisionSystemSim visionSim;

    /**
     * Create a vision localizer
     *
     * @param cameraParams A list of parameters describing the cameras on the robot
     * @param fieldLayout The layout of the apriltags on the field
     */
    public VisionLocalizer(CameraParams[] cameraParams, AprilTagFieldLayout fieldLayout) {
        cameras = new CameraWrapper[cameraParams.length];

        if (RobotBase.isSimulation()) {
            visionSim = new VisionSystemSim("coppercore");

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
        }
    }

    @Override
    public void periodic() {
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
