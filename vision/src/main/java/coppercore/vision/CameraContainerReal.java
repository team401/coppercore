package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * A implementation of {@link CameraContainer} that manages a list of {@link Camera} objects for
 * real-life matches. This class initializes cameras based on a list of {@link CameraParams} and an
 * {@link AprilTagFieldLayout}, and provides methods to retrieve and update them.
 */
public class CameraContainerReal implements CameraContainer {

    private List<Camera> cameras = new ArrayList<>();

    /**
     * Constructs a {@code CameraContainerReal} with a list of camera parameters and an AprilTag
     * field layout. Each {@link Camera} is initialized with the given parameters and a
     * corresponding {@link CameraIOPhoton}.
     *
     * @param params the list of {@link CameraParams} used to configure the cameras
     * @param layout the {@link AprilTagFieldLayout} associated with the cameras
     */
    public CameraContainerReal(List<CameraParams> params, AprilTagFieldLayout layout) {
        for (CameraParams param : params) {
            cameras.add(new Camera(param, CameraIOPhoton.fromRealCameraParams(param, layout)));
        }
    }

    /**
     * Retrieves the list of cameras managed by this container.
     *
     * @return a {@link List} of {@link Camera} objects
     */
    public List<Camera> getCameras() {
        return cameras;
    }

    /**
     * Updates all cameras in the container by invoking their {@link Camera#update()} method. This
     * ensures the cameras are synchronized with the latest data.
     */
    public void update() {
        for (Camera camera : cameras) {
            camera.update();
        }
    }
}
