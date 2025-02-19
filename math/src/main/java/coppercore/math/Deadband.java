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
