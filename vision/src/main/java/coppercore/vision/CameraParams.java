package coppercore.vision;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;

/**
 * Represents the configuration parameters for a camera.
 *
 * @param name The camera name.
 * @param xResolution Horizontal resolution in pixels.
 * @param yResolution Vertical resolution in pixels.
 * @param fps Frames per second.
 * @param fov Field of view as a {@link Rotation2d}.
 * @param robotToCamera Transform from the robot to the camera as a {@link Transform3d}.
 */
public record CameraParams(
        String name,
        int xResolution,
        int yResolution,
        int fps,
        Rotation2d fov,
        Transform3d robotToCamera) {}
