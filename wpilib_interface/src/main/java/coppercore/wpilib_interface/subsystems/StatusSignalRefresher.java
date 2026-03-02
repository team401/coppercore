package coppercore.wpilib_interface.subsystems;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.littletonrobotics.junction.Logger;

/**
 * The StatusSignalRefresher class handles refreshing all status signals for each CAN bus with a
 * single call to {@link BaseStatusSignal#refreshAll(BaseStatusSignal...)} to avoid incurring
 * additional overhead by calling it multiple times.
 *
 * <p>To use it, use {@link #addSignal(CANBus, BaseStatusSignal)}, {@link #addSignals(CANBus,
 * BaseStatusSignal[])}, or {@link #addSignals(CANBus, List)} to add signals during code init. Then,
 * call {@link #refreshAll()} in robotPeriodic BEFORE any signal values are used (e.g. before
 * CommandScheduler.run()) in order to ensure that all status signals are up to date.
 *
 * <p>The methods to add signals require a CANBus as a parameter because {@link
 * BaseStatusSignal#refreshAll(BaseStatusSignal...)} requires that all signals be on the same CAN
 * bus, so it must be called once for each bus.
 */
public class StatusSignalRefresher {
    // this is a static class and may not be instantiated
    private StatusSignalRefresher() {}

    /** Maps each CANBus to the signals present on that bus. */
    private static Map<CANBus, List<BaseStatusSignal>> canBusToSignalsMap = new HashMap<>();

    /**
     * Updates all of the signals in the manager. This must be called BEFORE {@code
     * CommandScheduler.getInstance().run()} in the robot periodic to work properly.
     */
    public static void refreshAll() {
        if (canBusToSignalsMap.isEmpty()) {
            return;
        }

        for (CANBus bus : canBusToSignalsMap.keySet()) {
            // Refresh all signals on this bus and log the resulting status code
            var status = BaseStatusSignal.refreshAll(canBusToSignalsMap.get(bus));
            Logger.recordOutput("StatusSignalRefresher/StatusCode/" + bus, status);
        }
    }

    /**
     * Add a group of status signals to the StatusSignalRefresher
     *
     * @param bus The CANBus these signals will come from
     * @param signals A List of signals to add
     */
    public static void addSignals(CANBus bus, List<BaseStatusSignal> signals) {
        if (!canBusToSignalsMap.containsKey(bus)) {
            canBusToSignalsMap.put(bus, new ArrayList<>());
        }
        canBusToSignalsMap.get(bus).addAll(signals);
    }

    /**
     * Add a group of status signals to the StatusSignalRefresher
     *
     * @param bus The CANBus these signals will come from
     * @param signals An of signals to add
     */
    public static void addSignals(CANBus bus, BaseStatusSignal[] signals) {
        if (!canBusToSignalsMap.containsKey(bus)) {
            canBusToSignalsMap.put(bus, new ArrayList<>());
        }
        for (var signal : signals) {
            canBusToSignalsMap.get(bus).add(signal);
        }
    }

    /**
     * Add a single status signal to the StatusSignalRefresher
     *
     * @param bus The bus this signal will come from
     * @param signal A BaseStatusSignal to add
     */
    public static void addSignal(CANBus bus, BaseStatusSignal signal) {
        if (!canBusToSignalsMap.containsKey(bus)) {
            canBusToSignalsMap.put(bus, new ArrayList<>());
        }
        canBusToSignalsMap.get(bus).add(signal);
    }
}
