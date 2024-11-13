package coppercore.vision;

import edu.wpi.first.math.geometry.Pose3d;

public interface VisionSimWrapper {
    public void addCamera(CameraParams cameraParams);

    public void update(Pose3d robotPoseMeters);
}
