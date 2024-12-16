package coppercore.vision;

import coppercore.vision.VisionLocalizer.CameraMeasurement;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import org.littletonrobotics.junction.Logger;

/**
 * Represents a PhotonVision camera by wrapping a {@link CameraIO} interface. This class is
 * responsible for managing the camera's IO, logging camera data, calculating standard deviations,
 * assembling {@link CameraMeasurement} objects, and updating the camera's state.
 */
public class Camera {
    /** The name of the camera, typically defined in {@link CameraParams}. */
    public final String name;

    private final CameraIO io;
    private final CameraIOInputsAutoLogged inputs;

    /**
     * Constructs a new {@code Camera} instance.
     *
     * @param params The parameters used to configure the camera, such as its name.
     * @param io The {@link CameraIO} instance that provides the camera's input/output
     *     functionality.
     */
    public Camera(CameraParams params, CameraIO io) {
        name = params.name();

        this.io = io;
        inputs = new CameraIOInputsAutoLogged();
    }

    /**
     * Updates the camera's inputs by polling the {@link CameraIO} and processes the data for
     * logging using the {@link Logger}.
     */
    public void update() {
        io.updateInputs(inputs);
        Logger.processInputs("Vision/" + name, inputs);
    }

    /**
     * Checks if the camera has new measurements available.
     *
     * @return {@code true} if new measurements are available, as indicated by {@link
     *     coppercore.vision.CameraIO.CameraIOInputs#wasAccepted}.
     */
    public boolean hasNewMeasurement() {
        return inputs.wasAccepted;
    }

    /**
     * Checks if the camera is currently connected.
     *
     * @return {@code true} if the camera is connected, as indicated by {@link
     *     coppercore.vision.CameraIO.CameraIOInputs#connected}.
     */
    public boolean isConnected() {
        return inputs.connected;
    }

    /**
     * Retrieves the latest measurement from the camera, including the field-to-robot
     * transformation, timestamp, and variance.
     *
     * @return A {@link CameraMeasurement} object containing the latest data.
     */
    public CameraMeasurement getLatestMeasurement() {
        return new CameraMeasurement(
                inputs.latestFieldToRobot, inputs.latestTimestampSeconds, getLatestVariance());
    }

    /**
     * Calculates the variance of the latest measurement based on the distance to the target and the
     * number of detected tags.
     *
     * @return A {@link Matrix} representing the variance of the latest measurement, with dimensions
     *     {@code N3 x N1}.
     */
    public Matrix<N3, N1> getLatestVariance() {
        Matrix<N3, N1> stdDev = CoreVisionConstants.singleTagStdDev;
        // TODO: Actually calculate variances!
        double avgDistanceFromTarget = inputs.averageTagDistanceM;
        int numTags = inputs.nTags;

        if (numTags == 0) {
            return stdDev;
        } else if (numTags > 1) {
            stdDev = CoreVisionConstants.multiTagStdDev;
        } else if (numTags == 1
                && avgDistanceFromTarget > CoreVisionConstants.singleTagDistanceCutoff) {
            return CoreVisionConstants.rejectionStdDev;
        }

        // Distance-based variance
        stdDev =
                stdDev.times(
                        1
                                + (Math.pow(avgDistanceFromTarget, 2)
                                        / CoreVisionConstants.distanceFactor));

        return stdDev;
    }
}
