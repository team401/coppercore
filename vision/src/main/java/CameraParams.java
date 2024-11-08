package coppercore.vision;

import edu.wpi.first.math.geometry.Transform3d;
import org.photonvision.PhotonCamera;

public record CameraParams(PhotonCamera camera, Transform3d robotToCamera) {}
