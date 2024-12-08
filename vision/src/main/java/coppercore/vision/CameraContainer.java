package coppercore.vision;

import java.util.List;

/**
 * Represents a container for managing multiple {@link Camera} objects and their associated data.
 * This interface provides methods to retrieve the cameras and update their state.
 *
 * <p>Note: This is not an AdvantageKit IO interface but rather a utility for grouping and managing
 * cameras.
 */
public interface CameraContainer {

    /**
     * Retrieves the list of {@link Camera} objects managed by this container.
     *
     * @return A {@link List} of {@link Camera} objects.
     */
    public List<Camera> getCameras();

    /** Updates the state of all {@link Camera} objects in the container. */
    public void update();
}
