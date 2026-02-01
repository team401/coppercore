package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform3d;
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
    private final PhotonCameraSim cameraSim;

    /**
     * Creates a new VisionIOPhotonVisionSim.
     *
     * @param name The name of the camera.
     * @param poseSupplier Supplier for the robot pose to use in simulation.
     * @see VisionIOPhotonReal#VisionIOPhotonReal(String, Transform3d)
     */
    public VisionIOPhotonSim(
            String name,
            Transform3d robotToCamera,
            Supplier<Pose2d> poseSupplier,
            AprilTagFieldLayout aprilTagLayout) {
        super(name, robotToCamera);
        this.poseSupplier = poseSupplier;

        // Initialize vision sim
        if (visionSim == null) {
            visionSim = new VisionSystemSim("main");
            visionSim.addAprilTags(aprilTagLayout);
        }

        // Add sim camera
        var cameraProperties = new SimCameraProperties();
        cameraSim = new PhotonCameraSim(camera, cameraProperties);
        visionSim.addCamera(cameraSim, robotToCamera);
    }

    /**
     * @see VisionIO#updateInputs(coppercore.vision.VisionIO.VisionIOInputs, DoubleFunction)
     */
    @Override
    public void updateInputs(
            VisionIOInputs inputs, DoubleFunction<Optional<Transform3d>> robotToCamera) {
        visionSim.update(poseSupplier.get());
        super.updateInputs(inputs, robotToCamera);
    }
}
