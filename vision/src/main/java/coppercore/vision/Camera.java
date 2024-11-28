package coppercore.vision;

import coppercore.vision.VisionLocalizer.CameraMeasurement;
import edu.wpi.first.math.Matrix;
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
        Matrix<N3, N1> stdDev = CoreVisionConstants.singleTagStdDev;
        // TODO: Actually calculate variances!
        double avgDistanceFromTarget = inputs.averageTagDistanceM;
        int numTags = inputs.nTags;

        if (numTags == 0) {
            return stdDev;
        } else if (numTags > 1) {
                stdDev = CoreVisionConstants.multiTagStdDev;
        } else if (numTags == 1 && avgDistanceFromTarget > CoreVisionConstants.singleTagDistanceCutoff) {
                return CoreVisionConstants.rejectionStdDev;
        }

        // distance based variance
        stdDev = stdDev.times(1 + (Math.pow(avgDistanceFromTarget, 2) / CoreVisionConstants.distanceFactor));

        return stdDev;
    }
}
