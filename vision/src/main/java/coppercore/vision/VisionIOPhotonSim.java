package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Timer;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/** implements vision io through photon vision simulation */
public class VisionIOPhotonSim extends VisionIOPhotonReal {
    private static VisionSystemSim visionSim;

    private final Supplier<Pose2d> poseSupplier;
    private PhotonCameraSim cameraSim;

    /**
     * Creates a new VisionIOPhotonVisionSim.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String)
     */
    public VisionIOPhotonSim(
            String name, Supplier<Pose2d> poseSupplier, AprilTagFieldLayout aprilTagLayout) {
        super(name);
        this.poseSupplier = poseSupplier;

        // Initialize vision sim
        if (visionSim == null) {
            visionSim = new VisionSystemSim("main");
            visionSim.addAprilTags(aprilTagLayout);
        }
    }

    /**
     * @see VisionIO#updateInputs(coppercore.vision.VisionIO.VisionIOInputs, DoubleFunction)
     */
    @Override
    public void updateInputs(
            VisionIOInputs inputs, DoubleFunction<Optional<Transform3d>> robotToCameraAt) {
        // If the camera doesn't already exist, it should be created at the given transform. If it
        // already existed, it should be adjusted to the new transform, which may be the same as
        // before if the camera is stationary.
        if (cameraSim == null) {
            // Add sim camera
            var cameraProperties = new SimCameraProperties();
            cameraSim = new PhotonCameraSim(camera, cameraProperties);
            robotToCameraAt
                    .apply(Timer.getFPGATimestamp())
                    .ifPresentOrElse(
                            (robotToCamera) -> {
                                visionSim.addCamera(cameraSim, robotToCamera);
                            },
                            () -> {
                                System.err.println(
                                        "could not simulate camera as a transform was not given");
                            });
        } else {
            robotToCameraAt
                    .apply(Timer.getFPGATimestamp())
                    .ifPresentOrElse(
                            (robotToCamera) -> {
                                visionSim.adjustCamera(cameraSim, robotToCamera);
                            },
                            () -> {
                                System.err.println(
                                        "could not adjust camera transform as one was not given");
                            });
        }
        visionSim.update(poseSupplier.get());
        super.updateInputs(inputs, robotToCameraAt);
    }
}
