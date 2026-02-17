package coppercore.vision;

import coppercore.math.RunOnce;
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

    private final SimCameraProperties cameraProperties;

    /**
     * Creates a new VisionIOPhotonVisionSim.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @param simCameraProperties A SimCameraProperties used to specify camera-specific properties
     *     such as calibrations, framerate, latency, etc. If these values aren't known or needed,
     *     use {@link VisionIOPhotonSim#VisionIOPhotonSim(String, Supplier,
     *     coppercore.vision.VisionLocalizer.CameraType)} to use defaults instead.
     * @param logSingleTagObservations Whether or not the individual tag observations should be
     *     logged
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String)
     */
    public VisionIOPhotonSim(
            String name,
            Supplier<Pose2d> poseSupplier,
            VisionLocalizer.CameraType type,
            SimCameraProperties simCameraProperties,
            boolean logSingleTagObservations) {
        super(name, logSingleTagObservations);
        this.poseSupplier = poseSupplier;
        this.cameraType = type;

        // Initialize vision sim
        if (visionSim == null) {
            visionSim = new VisionSystemSim("main");
        }

        this.cameraProperties = simCameraProperties;
    }

    /**
     * Creates a new VisionIOPhotonVisionSim that does not log single tag observations.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @param simCameraProperties A SimCameraProperties used to specify camera-specific properties
     *     such as calibrations, framerate, latency, etc. If these values aren't known or needed,
     *     use {@link VisionIOPhotonSim#VisionIOPhotonSim(String, Supplier,
     *     coppercore.vision.VisionLocalizer.CameraType)} to use defaults instead.
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String)
     */
    public VisionIOPhotonSim(
            String name,
            Supplier<Pose2d> poseSupplier,
            VisionLocalizer.CameraType type,
            SimCameraProperties simCameraProperties) {
        this(name, poseSupplier, type, simCameraProperties, false);
    }

    /**
     * Creates a new VisionIOPhotonVisionSim.
     *
     * <p>This constructor uses the {@link SimCameraProperties#SimCameraProperties() default}
     * SimCameraProperties. To configure the SimCameraProperties, use {@link
     * VisionIOPhotonSim#VisionIOPhotonSim(String, Supplier,
     * coppercore.vision.VisionLocalizer.CameraType, SimCameraProperties)}.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String)
     */
    public VisionIOPhotonSim(
            String name, Supplier<Pose2d> poseSupplier, VisionLocalizer.CameraType type) {
        this(name, poseSupplier, type, new SimCameraProperties());
    }

    @Override
    public void updateInputs(
            VisionIOInputs inputs,
            DoubleFunction<Optional<Transform3d>> robotToCameraAt,
            RunOnce doOnce) {

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
        doOnce.run(() -> visionSim.update(poseSupplier.get()));
        super.updateInputs(inputs, robotToCameraAt, doOnce);
    }

    /**
     * Creates a camera with the given initial transform. This should be called only once when the
     * VisionIOPhotonSim is created. This is called for both mobile and stationary cameras.
     *
     * @param tagLayout the AprilTagFieldLayout currently in use
     * @param tagLayoutRunOnce a RunOnce which should be passed to all instances during
     *     initialization to ensure that tags are only added once.
     * @param robotToCameraAt the initial transform of the robot to the camera as a double function
     */
    @Override
    public void initializeCamera(
            AprilTagFieldLayout tagLayout,
            RunOnce tagLayoutRunOnce,
            DoubleFunction<Optional<Transform3d>> robotToCameraAt) {
        super.initializeCamera(tagLayout, tagLayoutRunOnce, robotToCameraAt);
        tagLayoutRunOnce.run(
                () -> {
                    visionSim.addAprilTags(tagLayout);
                });

        // Add sim camera
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
