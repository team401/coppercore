package coppercore.vision;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.Consumer;

/**
 * Localizes the robot using camera measurements. Periodically updates camera data and allows for
 * custom handling of new measurements.
 */
public class VisionLocalizer extends SubsystemBase {

    private CameraContainer container;

    // Default no-op consumer to avoid NullPointerExceptions
    private Consumer<CameraMeasurement> cameraConsumer = (c) -> {};

    /**
     * Constructs a VisionLocalizer with a given camera container.
     *
     * @param container The container holding cameras for localization.
     */
    public VisionLocalizer(CameraContainer container) {
        this.container = container;
    }

    /** Periodically updates the camera data and processes new measurements. */
    @Override
    public void periodic() {
        container.update();
        for (Camera camera : container.getCameras()) {
            if (camera.hasNewMeasurement()) {
                cameraConsumer.accept(camera.getLatestMeasurement());
            }
        }
    }

    /**
     * Sets a custom consumer for handling camera measurements.
     *
     * @param cameraConsumer The consumer to handle new camera measurements.
     */
    public void setCameraConsumer(Consumer<CameraMeasurement> cameraConsumer) {
        this.cameraConsumer = cameraConsumer;
    }

    /**
     * Checks if the coprocessor is connected.
     *
     * @return True if the coprocessor is connected, otherwise false.
     */
    public boolean coprocessorConnected() {
        return container.getCameras().get(0).isConnected();
    }

    /** Represents a camera measurement with a pose, timestamp, and variance. */
    public static record CameraMeasurement(
            Pose3d pose, double timestamp, Matrix<N3, N1> variance) {}
}
