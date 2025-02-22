package coppercore.math;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Millimeters;

import edu.wpi.first.units.measure.Distance;

public class GearConversionFunctions {
    public static Distance pitchDiameterFrom3mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Millimeters.of(3 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static Distance pitchDiameterFrom5mmPulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Millimeters.of(5 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static Distance pitchDiameterFromRT25Pulley(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.25 * teeth / ((double) Math.PI));
        return pitchDiameter;
    }

    public static Distance pitchDiameterFrom25ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.25 / (double) Math.sin(Math.PI / teeth));
        return pitchDiameter;
    }

    public static Distance pitchDiameterFrom35ChainSprocket(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of(0.375 / (double) Math.sin(Math.PI / teeth));
        return pitchDiameter;
    }

    public static Distance pitchDiameterFrom20DPGear(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of((double) teeth / 20);
        return pitchDiameter;
    }

    public static Distance pitchDiameterFrom32DPGear(int teeth) {
        teeth = Math.abs(teeth);
        Distance pitchDiameter = Inches.of((double) teeth / 32);
        return pitchDiameter;
    }
}
