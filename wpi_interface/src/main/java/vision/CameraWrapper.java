package coppercore.wpilib_interface.vision;

import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;

/** A wrapper for a PhotonCamera. Holds the container and its respective pose estimator */
public class CameraWrapper {
    private PhotonCamera camera;
    private PhotonPoseEstimator poseEstimator;

    public CameraWrapper(PhotonCamera camera, PhotonPoseEstimator poseEstimator) {
        this.camera = camera;
        this.poseEstimator = poseEstimator;
    }

    public PhotonPoseEstimator getPoseEstimator() {
        return poseEstimator;
    }

    public PhotonCamera getCamera() {
        return camera;
    }
}
