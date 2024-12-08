package coppercore.vision;

import java.util.ArrayList;
import java.util.List;

/**
 * CameraContainerReplay simulates a container for cameras in a replay system. It does not interact
 * with a real camera or simulation, and instead it updates the cameras based on replayed data.
 */
public class CameraContainerReplay implements CameraContainer {
    private List<Camera> cameras = new ArrayList<>();

    /**
     * Initializes the CameraContainerReplay with the specified camera parameters.
     *
     * @param params A list of camera parameters for the replay.
     */
    public CameraContainerReplay(List<CameraParams> params) {
        for (CameraParams param : params) {
            cameras.add(new Camera(param, new CameraIO() {})); // Use a no-op CameraIO for replay
        }
    }

    /**
     * Gets the list of cameras in the replay system.
     *
     * @return A list of cameras.
     */
    @Override
    public List<Camera> getCameras() {
        return cameras;
    }

    /**
     * Updates the cameras by calling the update method on each camera. This does not interact with
     * a real vision system but updates based on replayed data.
     */
    @Override
    public void update() {
        for (Camera camera : cameras) {
            camera.update();
        }
    }
}
