package coppercore.monitors;

import java.util.HashMap;
import java.util.Map;

/**
 * The TotalCurrentCalculator provides a way to sum the total current used across all subsystems
 *
 * <p>Call {@link TotalCurrentCalculator#enable()} to begin logging. This is required because we
 * don't want to do unnecessary math which may cause lag. It's recommended to enable this in replay
 * sim.
 *
 * <p>{@link TotalCurrentCalculator#getTotalCurrent()} must be called periodically to get the latest
 * data.
 */
public class TotalCurrentCalculator {
    // Class should be used statically
    private TotalCurrentCalculator() {}

    private static boolean enabled = false;

    private static final Map<Integer, Double> subsystemHashToSupplyCurrentAmps = new HashMap<>();

    private static double currentSumAmps = 0.0;

    /** Enables current accumulation from future recorded subsystem currents. */
    public static void enable() {
        enabled = true;
    }

    /**
     * Gets the latest total current sum.
     *
     * @return total recorded supply current in amps
     */
    public static double getTotalCurrent() {
        return currentSumAmps;
    }

    /**
     * Records the latest current draw for one subsystem.
     *
     * @param id stable subsystem identifier
     * @param supplyCurrentAmps subsystem supply current in amps
     */
    public static void recordCurrent(int id, double supplyCurrentAmps) {
        if (!enabled) {
            return;
        }

        currentSumAmps -= subsystemHashToSupplyCurrentAmps.computeIfAbsent(id, _ignored -> 0.0);
        subsystemHashToSupplyCurrentAmps.put(id, supplyCurrentAmps);
        currentSumAmps += supplyCurrentAmps;
    }
}
