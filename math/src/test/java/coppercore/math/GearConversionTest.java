package coppercore.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GearConversionTest {
    private static final double delta = 1 * Math.pow(10.0, -10.0);

    @Test
    public void pitchDiameterFrom3mmPulleyTest() {
        Assertions.assertEquals(
                12 / (Math.PI), GearConversionFunctions.pitchDiameterFrom3mmPulley(4), delta);
        Assertions.assertEquals(
                150 / (Math.PI), GearConversionFunctions.pitchDiameterFrom3mmPulley(50), delta);
        Assertions.assertEquals(
                0 / (Math.PI), GearConversionFunctions.pitchDiameterFrom3mmPulley(0), delta);
        Assertions.assertEquals(
                21 / (Math.PI), GearConversionFunctions.pitchDiameterFrom3mmPulley(-7), delta);
    }

    @Test
    public void pitchDiameterFrom5mmPulleyTest() {
        Assertions.assertEquals(
                20 / (Math.PI), GearConversionFunctions.pitchDiameterFrom5mmPulley(4), delta);
        Assertions.assertEquals(
                250 / (Math.PI), GearConversionFunctions.pitchDiameterFrom5mmPulley(50), delta);
        Assertions.assertEquals(
                0 / (Math.PI), GearConversionFunctions.pitchDiameterFrom5mmPulley(0), delta);
        Assertions.assertEquals(
                35 / (Math.PI), GearConversionFunctions.pitchDiameterFrom5mmPulley(-7), delta);
    }

    @Test
    public void pitchDiameterFromRT25PulleyTest() {
        Assertions.assertEquals(
                1 / (Math.PI), GearConversionFunctions.pitchDiameterFromRT25Pulley(4), delta);
        Assertions.assertEquals(
                13 / (Math.PI), GearConversionFunctions.pitchDiameterFromRT25Pulley(52), delta);
        Assertions.assertEquals(
                0 / (Math.PI), GearConversionFunctions.pitchDiameterFromRT25Pulley(0), delta);
        Assertions.assertEquals(
                2 / (Math.PI), GearConversionFunctions.pitchDiameterFromRT25Pulley(-8), delta);
    }

    @Test
    public void pitchDiameterFrom25ChainSprocketTest() {
        Assertions.assertEquals(
                0.25 / (1 / Math.sqrt(2)),
                GearConversionFunctions.pitchDiameterFrom25ChainSprocket(4),
                delta);
        Assertions.assertEquals(
                0.25 / (Math.sqrt(3) / 2),
                GearConversionFunctions.pitchDiameterFrom25ChainSprocket(3),
                delta);
    }

    @Test
    public void pitchDiameterFrom35ChainSprocketTest() {
        Assertions.assertEquals(
                0.375 / (1 / Math.sqrt(2)),
                GearConversionFunctions.pitchDiameterFrom35ChainSprocket(4),
                delta);
        Assertions.assertEquals(
                0.375 / (Math.sqrt(3) / 2),
                GearConversionFunctions.pitchDiameterFrom35ChainSprocket(3),
                delta);
    }

    @Test
    public void pitchDiameterFrom20DPGearTest() {
        Assertions.assertEquals(0.2, GearConversionFunctions.pitchDiameterFrom20DPGear(4), delta);
        Assertions.assertEquals(1, GearConversionFunctions.pitchDiameterFrom20DPGear(20), delta);
        Assertions.assertEquals(0, GearConversionFunctions.pitchDiameterFrom20DPGear(0), delta);
        Assertions.assertEquals(2.0, GearConversionFunctions.pitchDiameterFrom20DPGear(-40), delta);
    }

    @Test
    public void pitchDiameterFrom32DPGearTest() {
        Assertions.assertEquals(0.125, GearConversionFunctions.pitchDiameterFrom32DPGear(4), delta);
        Assertions.assertEquals(1.0, GearConversionFunctions.pitchDiameterFrom32DPGear(32), delta);
        Assertions.assertEquals(0, GearConversionFunctions.pitchDiameterFrom32DPGear(0), delta);
        Assertions.assertEquals(2.0, GearConversionFunctions.pitchDiameterFrom32DPGear(-64), delta);
    }
}
