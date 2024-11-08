package coppercore.vision;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;

/**
 * A vision measurement so that the consumer can be typed as a Consumer<VisionMeasurement> This is
 * only necessary because java has no class for a consumer with 3 arguments.
 */
public record VisionMeasurement(Pose2d pose, double timestamp, Matrix<N3, N1> stdDevs) {}
