package coppercore.vision;

import coppercore.math.RunOnce;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import org.photonvision.estimation.TargetModel;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.simulation.VisionTargetSim;
import org.wpilib.fields.Field;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.system.Timer;

/** Implements vision IO through PhotonVision simulation. */
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
            // TODO: PhotonLib alpha-2 still calls the removed SmartDashboard API here. This
            // compiles against WPILib alpha-7, but simulation needs an alpha-7-compatible
            // PhotonLib.
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
                    .apply(Timer.getTimestamp())
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
     * @param tagLayout the field layout currently in use
     * @param tagLayoutRunOnce a RunOnce which should be passed to all instances during
     *     initialization to ensure that tags are only added once.
     * @param robotToCameraAt the initial transform of the robot to the camera as a double function
     */
    @Override
    public void initializeCamera(
            Field tagLayout,
            RunOnce tagLayoutRunOnce,
            DoubleFunction<Optional<Transform3d>> robotToCameraAt) {
        super.initializeCamera(tagLayout, tagLayoutRunOnce, robotToCameraAt);
        tagLayoutRunOnce.run(() -> addAprilTagsToSimulation(tagLayout));

        // Add sim camera
        // TODO: Pass tagLayout when PhotonCameraSim accepts Field. The alpha-2 overload loads an
        // old default AprilTagFieldLayout, losing our field for multitag simulation and relying on
        // another API removed from WPILib alpha-7.
        cameraSim = new PhotonCameraSim(camera, cameraProperties);
        robotToCameraAt
                .apply(Timer.getTimestamp())
                .ifPresentOrElse(
                        (robotToCamera) -> {
                            visionSim.addCamera(cameraSim, robotToCamera);
                        },
                        () -> {
                            System.err.println(
                                    "could not add camera as robotToCamera does not exist");
                        });
    }

    /** Adapts WPILib's Field to the target API in PhotonLib alpha-2. */
    private static void addAprilTagsToSimulation(Field tagLayout) {
        // TODO: Replace this adapter with PhotonLib's Field-based addAprilTags method when
        // available.
        // Keep the singular group name: PhotonLib's clearAprilTags() removes "apriltag".
        for (var tag : tagLayout.getTags()) {
            // getTagPose applies the field's current origin; tag.getPose() does not.
            tagLayout
                    .getTagPose(tag.getID())
                    .ifPresent(
                            pose ->
                                    visionSim.addVisionTargets(
                                            "apriltag",
                                            new VisionTargetSim(
                                                    pose,
                                                    TargetModel.kAprilTag36h11,
                                                    tag.getID())));
        }
    }
}
