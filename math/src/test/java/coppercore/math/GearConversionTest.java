package coppercore.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GearConversionTest {
    @Test
    public void pitchPulley3mmTest() {
        Assertions.assertEquals(12 / (Math.PI), GearConversionFunctions.pitchPulley3mm(4));
        Assertions.assertEquals(150 / (Math.PI), GearConversionFunctions.pitchPulley3mm(50));
        Assertions.assertEquals(0 / (Math.PI), GearConversionFunctions.pitchPulley3mm(0));
        Assertions.assertEquals(21 / (Math.PI), GearConversionFunctions.pitchPulley3mm(-7));
    }

    @Test
    public void pitchPulley5mmTest() {
        Assertions.assertEquals(20 / (Math.PI), GearConversionFunctions.pitchPulley5mm(4));
        Assertions.assertEquals(250 / (Math.PI), GearConversionFunctions.pitchPulley5mm(50));
        Assertions.assertEquals(0 / (Math.PI), GearConversionFunctions.pitchPulley5mm(0));
        Assertions.assertEquals(35 / (Math.PI), GearConversionFunctions.pitchPulley5mm(-7));
    }

    @Test
    public void pulleyRT25Test() {
        Assertions.assertEquals(1 / (Math.PI), GearConversionFunctions.pulleyRT25(4));
        Assertions.assertEquals(13 / (Math.PI), GearConversionFunctions.pulleyRT25(52));
        Assertions.assertEquals(0 / (Math.PI), GearConversionFunctions.pulleyRT25(0));
        Assertions.assertEquals(2 / (Math.PI), GearConversionFunctions.pulleyRT25(-8));
    }

    @Test
    public void chainSprocket25Test() {
        Assertions.assertEquals(
                0.25 / (1 / Math.sqrt(2)), GearConversionFunctions.chainSprocket25(4));
        Assertions.assertEquals(
                0.25 / (Math.sqrt(3) / 2), GearConversionFunctions.chainSprocket25(3));
    }

    @Test
    public void chainSprocket35Test() {
        Assertions.assertEquals(
                0.375 / (1 / Math.sqrt(2)), GearConversionFunctions.chainSprocket35(4));
        Assertions.assertEquals(
                0.375 / (Math.sqrt(3) / 2), GearConversionFunctions.chainSprocket35(3));
    }

    @Test
    public void gear20DPTest() {
        Assertions.assertEquals(0.2, GearConversionFunctions.gear20DP(4));
        Assertions.assertEquals(1, GearConversionFunctions.gear20DP(20));
        Assertions.assertEquals(0, GearConversionFunctions.gear20DP(0));
        Assertions.assertEquals(2.0, GearConversionFunctions.gear20DP(-40));
    }

    @Test
    public void gear32DPTest() {
        Assertions.assertEquals(0.125, GearConversionFunctions.gear32DP(4));
        Assertions.assertEquals(1.0, GearConversionFunctions.gear32DP(32));
        Assertions.assertEquals(0, GearConversionFunctions.gear32DP(0));
        Assertions.assertEquals(2.0, GearConversionFunctions.gear32DP(-64));
    }
}
