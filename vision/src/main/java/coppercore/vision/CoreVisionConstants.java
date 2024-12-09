package coppercore.vision;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;

/**
 * Vision constants for coppercore's internal vision constants. Not to be confused with
 * VisionConstants, which is likely your robot project's local vision constants that are configured
 * per-project.
 */
public final class CoreVisionConstants {
    // TODO: Tune this value
    // This value mainly exists so that this file stays named CoreVisionConstants.
    public static final double maxAcceptedDistanceMeters = 10.0;
    public static final Matrix<N3, N1> singleTagStdDev = VecBuilder.fill(1, 1, 1);
    public static final Matrix<N3, N1> multiTagStdDev = VecBuilder.fill(0.5, 0.5, 0.5);
    public static final Matrix<N3, N1> rejectionStdDev =
            VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    public static final double singleTagDistanceCutoff = 4.0;
    public static final double distanceFactor = 30.0;
}
