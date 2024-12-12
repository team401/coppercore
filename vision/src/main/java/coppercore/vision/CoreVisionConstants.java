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
    // TODO: Tune this value.
    // This value mainly exists so that this file stays named CoreVisionConstants.
    public static final double maxAcceptedDistanceMeters = 10.0;
    public static final double linearStdDevFactor = 0.02;
    public static final double angularStdDevFactor = 0.06;
    public static final double singleTagDistanceCutoff = 4.0;
    public static final double distanceFactor = 1.0;
    public static final double maxZCutoff = 1.0;
}
