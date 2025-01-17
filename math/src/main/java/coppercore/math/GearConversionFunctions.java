package coppercore.math;

public class GearConversionFunctions {
    public static double pitchDiameterFrom3mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 3 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double pitchDiameterFrom5mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 5 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double pitchDiameterFromRT25Pulley(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.25 * teeth / ((double) Math.PI);
        return pitchDiameter;
    }

    public static double pitchDiameterFrom25ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.25 / (double) Math.sin(Math.PI / teeth);
        return pitchDiameter;
    }

    public static double pitchDiameterFrom35ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = 0.375 / (double) Math.sin(Math.PI / teeth);
        return pitchDiameter;
    }

    public static double pitchDiameterFrom20DPGear(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = (double) teeth / 20;
        return pitchDiameter;
    }

    public static double pitchDiameterFrom32DPGear(int teeth) {
        teeth = Math.abs(teeth);
        double pitchDiameter = (double) teeth / 32;
        return pitchDiameter;
    }
}
