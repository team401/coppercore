package coppercore.vision;

import coppercore.vision.VisionLocalizer.CameraMeasurement;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import org.littletonrobotics.junction.Logger;

public class Camera {
    public final String name;

    private final CameraIO io;
    private final CameraIOInputsAutoLogged inputs;

    public Camera(CameraParams params, CameraIO io) {
        name = params.name();

        this.io = io;
        inputs = new CameraIOInputsAutoLogged();
    }

    public void update() {
        io.updateInputs(inputs);
        Logger.processInputs("Vision/" + name, inputs);
    }

    public boolean hasNewMeasurement() {
        return inputs.wasAccepted;
    }

    public boolean isConnected() {
        return inputs.connected;
    }

    public CameraMeasurement getLatestMeasurement() {
        return new CameraMeasurement(
                inputs.latestFieldToRobot, inputs.latestTimestampSeconds, getLatestVariance());
    }

    public Matrix<N3, N1> getLatestVariance() {
        // TODO: Actually calculate variances!
        return VecBuilder.fill(0.0, 0.0, 0.0);
    }
}
