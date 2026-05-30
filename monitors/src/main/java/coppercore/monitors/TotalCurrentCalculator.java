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
 * <p>{@link TotalCurrentCalculator#periodic()} must be called periodically to log the latest data.
 */
public class TotalCurrentCalculator {
    // Class should be used statically
    private TotalCurrentCalculator() {}

    private static boolean enabled = false;

    private static final Map<Integer, Double> subsystemHashToSupplyCurrentAmps = new HashMap<>();

    private static double currentSumAmps = 0.0;

    public static void enable() {
        enabled = true;
    }

    public static double getTotalCurrent() {
        return currentSumAmps;
    }

    public static void recordCurrent(int id, double supplyCurrentAmps) {
        if (!enabled) {
            return;
        }

        currentSumAmps -= subsystemHashToSupplyCurrentAmps.computeIfAbsent(id, _ignored -> 0.0);
        subsystemHashToSupplyCurrentAmps.put(id, supplyCurrentAmps);
        currentSumAmps += supplyCurrentAmps;
    }
}
