package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.CANBus;

/**
 * A record to store a CAN ID with its associated CAN bus name.
 *
 * @param canbus The name of the CAN bus, usually "rio" or "canivore"
 * @param id The integer CAN ID
 */
public record CANDeviceID(CANBus canbus, int id) {
    @Override
    public String toString() {
        return canbus + "_" + id;
    }
}
