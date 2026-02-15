package coppercore.vision;

import coppercore.math.RunOnce;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleFunction;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;

/** This class implements io using photon vision */
public class VisionIOPhotonReal implements VisionIO {
    protected final PhotonCamera camera;
    protected final boolean logSingleTagObservations;
    public final String name;
    private AprilTagFieldLayout aprilTagLayout;

    /**
     * Creates a new VisionIOPhotonVision.
     *
     * @param name The configured name of the camera.
     * @param logSingleTagObservations Whether or not the camera should log its individual tag
     *     observations.
     */
    public VisionIOPhotonReal(String name, boolean logSingleTagObservations) {
        camera = new PhotonCamera(name);
        this.name = name;
        this.logSingleTagObservations = logSingleTagObservations;
    }

    /**
     * Creates a new VisionIOPhotonVision that does not log single tag observations.
     *
     * @param name The configured name of the camera.
     */
    public VisionIOPhotonReal(String name) {
        this(name, false);
    }

    @Override
    public void initializeCamera(
            AprilTagFieldLayout tagLayout,
            RunOnce tagLayoutRunOnce,
            DoubleFunction<Optional<Transform3d>> robotToCameraAt) {
        aprilTagLayout = tagLayout;
    }

    @Override
    public boolean isLoggingSingleTags() {
        return logSingleTagObservations;
    }

    @Override
    public void updateInputs(
            VisionIOInputs inputs,
            DoubleFunction<Optional<Transform3d>> optionalRobotToCamera,
            RunOnce _doOnce) {
        inputs.connected = camera.isConnected();

        Set<Short> tagsSeen = new HashSet<>();
        List<PoseObservation> poses = new ArrayList<>();
        List<SingleTagObservation> singleTagObservations = new ArrayList<>();

        // loop through all results to find pose and targets observed
        for (var result : camera.getAllUnreadResults()) {

            optionalRobotToCamera
                    .apply(result.getTimestampSeconds())
                    .ifPresent(
                            (robotToCamera) -> {

                                // add pose
                                if (result.multitagResult.isPresent()) {

                                    inputs.hasMultitagResult = true;
                                    var multitagResult = result.multitagResult.get();

                                    if (logSingleTagObservations) {
                                        // add observation for each tag
                                        for (var target : result.getTargets()) {
                                            singleTagObservations.add(
                                                    new SingleTagObservation(
                                                            target.getFiducialId(),
                                                            result.getTimestampSeconds(),
                                                            target.getBestCameraToTarget()
                                                                    .getTranslation()
                                                                    .getNorm(),
                                                            target.getBestCameraToTarget(),
                                                            Rotation2d.fromDegrees(target.getYaw()),
                                                            Rotation2d.fromDegrees(
                                                                    target.getPitch())));
                                        }
                                    }
                                    // convert pose from field to camera -> field to robot
                                    // Calculate robot pose
                                    Transform3d fieldToCamera = multitagResult.estimatedPose.best;
                                    Transform3d fieldToRobot =
                                            fieldToCamera.plus(robotToCamera.inverse());
                                    Pose3d robotPose =
                                            new Pose3d(
                                                    fieldToRobot.getTranslation(),
                                                    fieldToRobot.getRotation());

                                    // only need new avg tag distance if new pose
                                    double totalTagDistance = 0.0;
                                    for (var target : result.targets) {
                                        totalTagDistance +=
                                                target.bestCameraToTarget
                                                        .getTranslation()
                                                        .getNorm();
                                    }
                                    inputs.averageTagDistanceM =
                                            totalTagDistance / result.targets.size();

                                    tagsSeen.addAll(multitagResult.fiducialIDsUsed);
                                    poses.add(
                                            new PoseObservation(
                                                    result.getTimestampSeconds(),
                                                    robotPose,
                                                    multitagResult.estimatedPose.ambiguity,
                                                    multitagResult.fiducialIDsUsed.size(),
                                                    inputs.averageTagDistanceM));
                                }
                                if (!result.targets.isEmpty()) { // single tag estimation
                                    inputs.hasMultitagResult = false;
                                    var target = result.targets.get(0);

                                    var tagPose = aprilTagLayout.getTagPose(target.fiducialId);
                                    if (tagPose.isPresent()) {
                                        Pose3d robotPose =
                                                PhotonUtils.estimateFieldToRobotAprilTag(
                                                        target.getBestCameraToTarget(),
                                                        tagPose.get(),
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

                                        if (logSingleTagObservations) {
                                            // set latest single tag observation
                                            singleTagObservations.add(
                                                    new SingleTagObservation(
                                                            target.fiducialId,
                                                            result.getTimestampSeconds(),
                                                            target.getBestCameraToTarget()
                                                                    .getTranslation()
                                                                    .getNorm(),
                                                            target.getBestCameraToTarget(),
                                                            Rotation2d.fromDegrees(target.getYaw()),
                                                            Rotation2d.fromDegrees(
                                                                    target.getPitch())));
                                        }
                                    }
                                }
                            });
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

        inputs.singleTagObservations = new SingleTagObservation[singleTagObservations.size()];
        for (int j = 0; j < singleTagObservations.size(); j++) {
            inputs.singleTagObservations[j] = singleTagObservations.get(j);
        }
    }
}
