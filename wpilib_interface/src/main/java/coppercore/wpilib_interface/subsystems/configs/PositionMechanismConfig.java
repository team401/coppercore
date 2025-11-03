package coppercore.wpilib_interface.subsystems.configs;

import edu.wpi.first.units.measure.Angle;

/** A base config for a position-control based mechanism */
public class PositionMechanismConfig extends MechanismConfig {

    /**
     * Ratio of mechanism position to rotor position
     *
     * <p>Rotor position * this ratio = Mechanism position
     */
    public double positionToRotorRatio = 1.0;

    public Angle minPosition;
    public Angle maxPosition;

    /**
     * Create a new PositionMechanismConfig.
     *
     * @param name Mechanism name, used for logging
     * @param canbus Name of the canbus to use for all devices (usually "rio" or "canivore")
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorIds CAN IDs of the follower motors. Leave empty for no followers.
     * @param minPosition Minimum position (angle) of the system.
     * @param maxPosition Maximum position (angle) of the system.
     */
    public PositionMechanismConfig(
            String name,
            String canbus,
            int leadMotorId,
            int[] followerMotorIds,
            Angle minPosition,
            Angle maxPosition) {
        super(name, canbus, leadMotorId, followerMotorIds);
        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
    }
}
