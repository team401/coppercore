package coppercore.math;

public class GearConversionFunctions {
    public static double pitchPulley3mm(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 3 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double pitchPulley5mm(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 5 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double pulleyRT25(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.25 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double chainSprocket25(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.25 / (double) Math.sin(Math.PI / teeth);
        return pitchDiameter;
    }

    public static double chainSprocket35(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.375 / (double) Math.sin(Math.PI / teeth);
        return pitchDiameter;
    }

    public static double gear20DP(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = (double) teeth / 20;
        return pitchDiameter;
    }

    public static double gear32DP(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = (double) teeth / 32;
        return pitchDiameter;
    }
}
