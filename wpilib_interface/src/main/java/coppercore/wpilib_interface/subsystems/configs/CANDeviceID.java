package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.CANBus;
import org.wpilib.hardware.hal.CANBusMap;

/**
 * A record to store a CAN ID with its associated CAN bus name.
 *
 * <p>A CAN bus can be provided either via an external CAN bus connector such as the CTR CANivore,
 * or it can be attached to the robot control unit itself. Pre-2027 Roborios supported only a single
 * CAN bus, whereas 2027 systemcore has 5 CAN busses. In WPILib, these busses are referred to as
 * org.wpilib.hardware.hal.CANBusMap.CAN_S0 through CAN_S4
 *
 * <p>REV's SparkMax motors, which do not support CANivore, now expect a CAN bus id in their
 * constructor.
 *
 * <p>CTR instead chose to continue name CAN busses using strings, and provides methods to refer to
 * the systemcore busses via factory methods: CANBus.systemcore(int).
 *
 * <p>We continue to piggyback on Phoenix6's CANBus abstraction, but add a method to retrieve the
 * systemcore CAN bus id for systemcore busses.
 *
 * @param canbus The Phoenix6 CANBus object describing CAN bus
 * @param id The integer CAN ID
 */
public record CANDeviceID(CANBus canbus, int id) {
    @Override
    public String toString() {
        return canbus + "_" + id;
    }

    /**
     * Return the systemcore id of the CAN bus. If this CAN bus is not one of systemcore's, throw an
     * Error
     */
    public int systemCoreBusId() {
        String name = canbus.getName();
        if (!name.startsWith("can_s"))
            throw new UnsupportedOperationException(this + " is not a systemcore bus");

        // see
        // https://github.wpilib.org/allwpilib/docs/2027/java/constant-values.html#org.wpilib.hardware.hal.CANBusMap.CAN_S0
        return CANBusMap.CAN_S0 + name.charAt(5) - '0';
    }
}
