package coppercore.vision;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;

public record CameraParams(
        String name,
        int xResolution,
        int yResolution,
        int fps,
        Rotation2d fov,
        Transform3d robotToCamera) {}
