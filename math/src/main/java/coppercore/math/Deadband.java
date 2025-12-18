package coppercore.math;

/**
 * The Deadband class is designed to be able to filter out small and unintentional inputs, whether
 * it is a linear input, one in a 2-D circular format, or one in a 3-D sphere.
 */
public class Deadband {
    /**
     * This filters out unintentional linear inputs
     *
     * @param input the input that is being given
     * @param deadband how big an input can be to still be filtered out
     * @return whether or not we use the input
     */
    // 1D Deadband in linear format
    public static double oneAxisDeadband(double input, double deadband) {
        if (Math.abs(input) < deadband) {
            return 0;
        } else {
            return input;
        }
    }

    /**
     * This filters out inputs that are in a 2-D circular shape
     *
     * @param inputX The x-value of the input
     * @param inputY The y-value of the input
     * @param deadband How big an input can be to still be filtered out
     * @return whether or not we use the input
     */
    // 2D Deadband in a circular format
    public static double[] twoAxisDeadband(double inputX, double inputY, double deadband) {
        double[] output = new double[2];
        if (Math.sqrt(Math.pow(inputX, 2) + Math.pow(inputY, 2)) < deadband) {
            output[0] = 0;
            output[1] = 0;
        } else {
            output[0] = inputX;
            output[1] = inputY;
        }
        return output;
    }

    /**
     * Filter out inputs that are inside a radius of `deadband` from 0,0 (applies a circular
     * deadband). Then, normalizes values so that the magnitude is on a scale from 0-1 beginning
     * where 0 is the outside ring of the deadband circle and 1 is a circle with radius 1.
     *
     * @param inputX The x-value of the input
     * @param inputY The y-value of the input
     * @param deadband How big an input can be to still be filtered out
     * @return A 2-element array of doubles where x is element 0 and y is element 1
     */
    public static double[] twoAxisDeadbandNormalized(
            double inputX, double inputY, double deadband) {
        if (deadband < 0.0) {
            throw new IllegalArgumentException(
                    "Deadband must be zero or positive, but was " + deadband);
        }
        double[] output = new double[2];

        double magnitude = Math.hypot(inputX, inputY);

        System.out.print("2ADBN " + inputX + " " + inputY + " " + magnitude + "/" + deadband + ":");

        if (magnitude <= deadband) {
            output[0] = 0.0;
            output[1] = 0.0;
        } else {
            double newMagnitude = (magnitude - deadband) / (1 - deadband);
            output[0] = inputX / magnitude * newMagnitude;
            output[1] = inputY / magnitude * newMagnitude;
        }

        System.out.println(output[0] + " " + output[1]);

        return output;
    }

    /**
     * This filters out inputs that are in a 3-D spherical format
     *
     * @param inputX The x-value of the input
     * @param inputY The y-value of the input
     * @param inputZ The z-value of the input
     * @param deadband How big the input can be to still be filtered out
     * @return Whether or not we use the input
     */
    // 3D Deadband in a spherical format
    public static double[] threeAxisDeadband(
            double inputX, double inputY, double inputZ, double deadband) {
        double[] output = new double[3];
        if (Math.sqrt(Math.pow(inputX, 2) + Math.pow(inputY, 2) + Math.pow(inputZ, 2)) < deadband) {
            output[0] = 0;
            output[1] = 0;
            output[2] = 0;
        } else {
            output[0] = inputX;
            output[1] = inputY;
            output[2] = inputZ;
        }
        return output;
    }
}
