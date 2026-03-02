package coppercore.wpilib_interface.subsystems;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import java.util.ArrayList;
import java.util.Arrays;
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

    private record BusInfo(CANBus bus, String loggingPath) {}

    private static BusInfo[] buses = {};

    /**
     * Updates all of the signals in the manager. This must be called BEFORE {@code
     * CommandScheduler.getInstance().run()} in the robot periodic to work properly.
     */
    public static void refreshAll() {
        for (var busInfo : buses) {
            // Refresh all signals on this bus and log the resulting status code
            var status = BaseStatusSignal.refreshAll(canBusToSignalsMap.get(busInfo.bus()));
            Logger.recordOutput(busInfo.loggingPath(), status);
        }
    }

    private static void addBusIfNotAlreadyAdded(CANBus bus) {
        boolean busAlreadyAdded = false;
        for (var busInfo : buses) {
            if (busInfo.bus() == bus) {
                busAlreadyAdded = true;
                break;
            }
        }

        if (!busAlreadyAdded) {
            BusInfo[] newBuses = Arrays.copyOf(buses, buses.length + 1);
            newBuses[buses.length] = new BusInfo(bus, "StatusSignalRefresher/StatusCode/" + bus);
            buses = newBuses;
        }
    }

    /**
     * Add a group of status signals to the StatusSignalRefresher
     *
     * @param bus The CANBus these signals will come from
     * @param signals A List of signals to add
     */
    public static void addSignals(CANBus bus, List<BaseStatusSignal> signals) {
        var existingSignals =
                canBusToSignalsMap.computeIfAbsent(bus, _ignored -> new ArrayList<>());

        existingSignals.addAll(signals);

        addBusIfNotAlreadyAdded(bus);
    }

    /**
     * Add a group of status signals to the StatusSignalRefresher
     *
     * @param bus The CANBus these signals will come from
     * @param signals An array of signals to add
     */
    public static void addSignals(CANBus bus, BaseStatusSignal[] signals) {
        var existingSignals =
                canBusToSignalsMap.computeIfAbsent(bus, _ignored -> new ArrayList<>());

        for (var signal : signals) {
            existingSignals.add(signal);
        }

        addBusIfNotAlreadyAdded(bus);
    }

    /**
     * Add a single status signal to the StatusSignalRefresher
     *
     * @param bus The bus this signal will come from
     * @param signal A BaseStatusSignal to add
     */
    public static void addSignal(CANBus bus, BaseStatusSignal signal) {
        var existingSignals =
                canBusToSignalsMap.computeIfAbsent(bus, _ignored -> new ArrayList<>());

        existingSignals.add(signal);

        addBusIfNotAlreadyAdded(bus);
    }
}
