package coppercore.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.photonvision.PhotonCamera;

public class VisionIOPhotonReal implements VisionIO {
    protected final PhotonCamera camera;
    protected final Transform3d robotToCamera;
    public final String name;

    /**
     * Creates a new VisionIOPhotonVision.
     *
     * @param name The configured name of the camera.
     * @param rotationSupplier The 3D position of the camera relative to the robot.
     */
    public VisionIOPhotonReal(String name, Transform3d robotToCamera) {
        camera = new PhotonCamera(name);
        this.name = name;
        this.robotToCamera = robotToCamera;
    }

    @Override
    public void updateInputs(VisionIOInputs inputs) {
        inputs.connected = camera.isConnected();

        Set<Short> tagsSeen = new HashSet<>();
        List<PoseObservation> poses = new LinkedList<>();

        // loop through all results to find pose and targets observed
        for (var result : camera.getAllUnreadResults()) {

            // find latest target
            if (result.hasTargets()) {
                inputs.latestTargetObservation =
                        new TargetObservation(
                                Rotation2d.fromDegrees(result.getBestTarget().getYaw()),
                                Rotation2d.fromDegrees(result.getBestTarget().getPitch()));
            } else {
                inputs.latestTargetObservation =
                        new TargetObservation(new Rotation2d(), new Rotation2d());
            }

            // add pose
            if (result.multitagResult.isPresent()) {
                var multitagResult = result.multitagResult.get();

                // convert pose from field to camera -> field to robot
                Transform3d fieldToCamera = multitagResult.estimatedPose.best;
                Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
                Pose3d robotPose =
                        new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());

                // only need new avg tag distance if new pose
                double totalTagDistance = 0.0;
                for (var target : result.targets) {
                    totalTagDistance += target.bestCameraToTarget.getTranslation().getNorm();
                }
                inputs.averageTagDistanceM = totalTagDistance / result.targets.size();

                tagsSeen.addAll(multitagResult.fiducialIDsUsed);
                poses.add(
                        new PoseObservation(
                                result.getTimestampSeconds(),
                                robotPose,
                                multitagResult.estimatedPose.ambiguity,
                                multitagResult.fiducialIDsUsed.size(),
                                inputs.averageTagDistanceM));
            }
        }

        // Save pose observations to inputs object
        inputs.poseObservations = new PoseObservation[poses.size()];
        for (int i = 0; i < poses.size(); i++) {
            inputs.poseObservations[i] = poses.get(i);
        }

        // Save tag IDs to inputs objects
        inputs.tagIds = new int[tagsSeen.size()];
        int i = 0;
        for (int id : tagsSeen) {
            inputs.tagIds[i] = id;
            i++;
        }
    }
}
