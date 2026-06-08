package coppercore.monitors.test;

import coppercore.monitors.BatteryVoltageAlert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// This file was written by Codex 5.5.
public class BatteryVoltageAlertTests {
    private static final double LOW_VOLTAGE_THRESHOLD = 10.0;
    private static final double NOMINAL_VOLTAGE = 12.0;
    private static final double LOW_VOLTAGE = 8.0;

    private static void runSamples(BatteryVoltageAlert alert, double voltage, int samples) {
        for (int i = 0; i < samples; i++) {
            alert.isBatteryBelowThreshold(voltage);
        }
    }

    @Test
    public void zeroFilterTimeReportsCurrentVoltageAgainstThreshold() {
        BatteryVoltageAlert alert =
                BatteryVoltageAlert.createBatteryVoltageAlert(LOW_VOLTAGE_THRESHOLD, 0.0, 0.02);

        Assertions.assertTrue(alert.isBatteryBelowThreshold(LOW_VOLTAGE));
        Assertions.assertFalse(alert.isBatteryBelowThreshold(LOW_VOLTAGE_THRESHOLD));
        Assertions.assertFalse(alert.isBatteryBelowThreshold(NOMINAL_VOLTAGE));
    }

    @Test
    public void filteredAlertIgnoresBriefLowVoltageDip() {
        BatteryVoltageAlert alert =
                BatteryVoltageAlert.createBatteryVoltageAlert(LOW_VOLTAGE_THRESHOLD, 0.1, 0.02);

        runSamples(alert, NOMINAL_VOLTAGE, 50);

        Assertions.assertFalse(alert.isBatteryBelowThreshold(LOW_VOLTAGE));
    }

    @Test
    public void filteredAlertReportsPersistentLowVoltage() {
        BatteryVoltageAlert alert =
                BatteryVoltageAlert.createBatteryVoltageAlert(LOW_VOLTAGE_THRESHOLD, 0.1, 0.02);

        runSamples(alert, NOMINAL_VOLTAGE, 50);
        runSamples(alert, LOW_VOLTAGE, 20);

        Assertions.assertTrue(alert.isBatteryBelowThreshold(LOW_VOLTAGE));
    }

    @Test
    public void filteredAlertRequiresPersistentRecovery() {
        BatteryVoltageAlert alert =
                BatteryVoltageAlert.createBatteryVoltageAlert(LOW_VOLTAGE_THRESHOLD, 0.1, 0.02);

        runSamples(alert, NOMINAL_VOLTAGE, 50);
        runSamples(alert, LOW_VOLTAGE, 20);

        Assertions.assertTrue(alert.isBatteryBelowThreshold(LOW_VOLTAGE));
        Assertions.assertTrue(alert.isBatteryBelowThreshold(NOMINAL_VOLTAGE));

        runSamples(alert, NOMINAL_VOLTAGE, 20);

        Assertions.assertFalse(alert.isBatteryBelowThreshold(NOMINAL_VOLTAGE));
    }
}
