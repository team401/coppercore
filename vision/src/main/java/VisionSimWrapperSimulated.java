package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.VisionSystemSim;

public class VisionSimWrapperSimulated implements VisionSimWrapper {
    VisionSystemSim visionSim;

    public VisionSimWrapperSimulated(String simName, AprilTagFieldLayout fieldLayout) {
        visionSim = new VisionSystemSim(simName);
        visionSim.addAprilTags(fieldLayout);
    }

    @Override
    public void addCamera(CameraParams cameraParams) {
        PhotonCameraSim cameraSim =
                new PhotonCameraSim(cameraParams.camera(), cameraParams.simCameraProp());
        visionSim.addCamera(cameraSim, cameraParams.robotToCamera());
    }

    @Override
    public void update(Pose3d robotPoseMeters) {}
}
