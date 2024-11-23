package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import java.util.ArrayList;
import java.util.List;

public class CameraContainerReal implements CameraContainer {

    private List<Camera> cameras = new ArrayList<>();

    public CameraContainerReal(List<CameraParams> params, AprilTagFieldLayout layout) {
        for (CameraParams param : params) {
            cameras.add(new Camera(param, CameraIOPhoton.fromRealCameraParams(param, layout)));
        }
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void update() {
        for (Camera camera : cameras) {
            camera.update();
        }
    }
}
