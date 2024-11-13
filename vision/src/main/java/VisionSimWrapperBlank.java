package coppercore.vision;

import edu.wpi.first.math.geometry.Pose3d;

public class VisionSimWrapperBlank implements VisionSimWrapper {
    @Override
    public void addCamera(CameraParams cameraParams) {}

    @Override
    public void update(Pose3d robotPoseMeters) {}
}
