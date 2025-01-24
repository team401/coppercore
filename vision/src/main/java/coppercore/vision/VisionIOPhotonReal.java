package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;

/** This class implements io using photon vision */
public class VisionIOPhotonReal implements VisionIO {
    protected final PhotonCamera camera;
    protected final Transform3d robotToCamera;
    public final String name;
    public AprilTagFieldLayout aprilTagLayout;

    /**
     * Creates a new VisionIOPhotonVision.
     *
     * @param name The configured name of the camera.
     * @param robotToCamera Transform to help find robot position
     */
    public VisionIOPhotonReal(String name, Transform3d robotToCamera) {
        camera = new PhotonCamera(name);
        this.name = name;
        this.robotToCamera = robotToCamera;
        this.aprilTagLayout = null;
    }

    /**
     * Sets the april tag field layout for single tag pose estimation
     * 
     * @param tagLayout the Field layout to use for single tag pose estimation (gathers tag pose)
     */
    public void setAprilTagLayout(AprilTagFieldLayout tagLayout) {
        aprilTagLayout = tagLayout;
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
            } else if (!result.targets.isEmpty()) { // single tag estimation
                var target = result.targets.get(0);

                var tagPose = aprilTagLayout.getTagPose(target.fiducialId);
                if (tagPose.isPresent()) {
                    Pose3d robotPose =
                            PhotonUtils.estimateFieldToRobotAprilTag(
                                    target.getBestCameraToTarget(),
                                    aprilTagLayout.getTagPose(target.fiducialId).get(),
                                    robotToCamera.inverse());

                    // Add tag ID
                    tagsSeen.add((short) target.fiducialId);

                    // Add pose observation
                    poses.add(
                            new PoseObservation(
                                    result.getTimestampSeconds(), // Timestamp
                                    robotPose, // 3D pose estimate
                                    target.poseAmbiguity, // Ambiguity
                                    1, // Tag count
                                    target.getBestCameraToTarget()
                                            .getTranslation()
                                            .getNorm() // Average tag distance
                                    ));

                    // set latest single tag observation
                    inputs.latestSingleTagObservation = new SingleTagObservation(target.fiducialId, result.getTimestampSeconds(), target.getBestCameraToTarget().getTranslation().getNorm(), new Rotation2d(target.getYaw()), new Rotation2d(target.getPitch()));
                }
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
