package coppercore.math;

import edu.first.wpilib.units.Distance;

public class GearConversionFunctions {
    public static Distance pitchDiameterFrom3mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Millimeters.of(3 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static double pitchDiameterFrom5mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Millimeters.of(5 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static double pitchDiameterFromRT25Pulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.25 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static double pitchDiameterFrom25ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.25 / (double) Math.sin(Math.PI / teeth));
        return pitchDiameter;
    }

    public static double pitchDiameterFrom35ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.375 / (double) Math.sin(Math.PI / teeth));
        return pitchDiameter;
    }

    public static double pitchDiameterFrom20DPGear(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of((double) teeth / 20);
        return pitchDiameter;
    }

    public static double pitchDiameterFrom32DPGear(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of((double) teeth / 32);
        return pitchDiameter;
    }
}
