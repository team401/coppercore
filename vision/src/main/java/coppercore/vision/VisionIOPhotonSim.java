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
    private VisionLocalizer.CameraType cameraType;

    /**
     * Creates a new VisionIOPhotonVisionSim.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String)
     */
    public VisionIOPhotonSim(
            String name, Supplier<Pose2d> poseSupplier, VisionLocalizer.CameraType type) {
        super(name);
        this.poseSupplier = poseSupplier;
        this.cameraType = type;

        // Initialize vision sim
        if (visionSim == null) {
            visionSim = new VisionSystemSim("main");
        }
    }

    /**
     * @see VisionIO#updateInputs(coppercore.vision.VisionIO.VisionIOInputs, DoubleFunction)
     */
    @Override
    public void updateInputs(
            VisionIOInputs inputs, DoubleFunction<Optional<Transform3d>> robotToCameraAt) {

        if (cameraType == VisionLocalizer.CameraType.MOBILE) {
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

    /**
     * Creates a camera with the given initial transform. This should be called only once when the
     * VisionIOPhotonSim is created. This is called for both mobile and stationary cameras.
     *
     * @param robotToCameraAt the initial transform of the robot to the camera as a double function
     */
    @Override
    public void initializeCamera(
            AprilTagFieldLayout tagLayout, DoubleFunction<Optional<Transform3d>> robotToCameraAt) {
        super.initializeCamera(tagLayout, robotToCameraAt);
        visionSim.clearAprilTags();
        visionSim.addAprilTags(tagLayout);

        // Add sim camera
        var cameraProperties = new SimCameraProperties();
        cameraSim = new PhotonCameraSim(camera, cameraProperties, tagLayout);
        robotToCameraAt
                .apply(Timer.getFPGATimestamp())
                .ifPresentOrElse(
                        (robotToCamera) -> {
                            visionSim.addCamera(cameraSim, robotToCamera);
                        },
                        () -> {
                            System.err.println(
                                    "could not add camera as robotToCamera does not exist");
                        });
    }
}
