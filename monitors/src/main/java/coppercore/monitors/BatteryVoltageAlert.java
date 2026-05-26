package coppercore.monitors;

import edu.wpi.first.math.filter.LinearFilter;

/**
 * BatteryVoltageAlert is a utility class that checks whether the battery voltage drops below a
 * given threshold. Internally, it uses a linear filter to see if the battery's voltage is below the
 * given threshold for at least the given filter time.
 */
public class BatteryVoltageAlert {
    private final double lowVoltageThreshold;
    private LinearFilter filter;

    private BatteryVoltageAlert(double lowVoltageThreshold, double filterTime, double period) {
        this.lowVoltageThreshold = lowVoltageThreshold;
        this.filter = LinearFilter.singlePoleIIR(filterTime, period);
    }

    /**
     * Creates and returns a BatteryVoltageAlert that can be checked to see if the battery voltage
     * has been below the given threshold for the given amount of time.
     *
     * @param lowVoltageThreshold the amount of voltage for the battery to be considered low
     * @param filterTime the amount of time the battery must be below the threshold
     * @param period the cycle time of the robot code
     * @return a BatteryVoltageAlert with the given parameters
     */
    public static BatteryVoltageAlert createBatteryVoltageAlert(
            double lowVoltageThreshold, double filterTime, double period) {
        return new BatteryVoltageAlert(lowVoltageThreshold, filterTime, period);
    }

    /**
     * Uses a linear filter to compute if the battery is below the given voltage threshold.
     *
     * @param voltage battery voltage
     * @return true if the average battery voltage is below the threshold
     */
    public boolean isBatteryBelowThreshold(double voltage) {
        return filter.calculate(voltage) < lowVoltageThreshold;
    }
}
