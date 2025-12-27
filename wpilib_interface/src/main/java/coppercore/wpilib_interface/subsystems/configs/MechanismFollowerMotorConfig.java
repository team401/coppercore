package coppercore.wpilib_interface.subsystems.configs;

/**
 * Configuration for a follower motor, to be used with the {@link MechanismConfig} class.
 *
 * @param id The CAN ID of the motor. Must be on the same CAN bus physically as the lead motor.
 * @param invert True if the follower's direction of rotation should oppose the leader's, false if
 *     they should rotate the same direction.
 */
public record MechanismFollowerMotorConfig(CANDeviceID id, boolean invert) {}
