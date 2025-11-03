package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.configs.TalonFXConfiguration;

/**
 * A base mechanism config.
 *
 * <p>Contains a name, CAN bus name, motor IDs for lead and follower motors, and a TalonFX config.
 */
public class MechanismConfig {
    public final String name;
    public final String canbus;
    public final int leadMotorId;

    public final int[] followerMotorIds;

    public final TalonFXConfiguration motorConfig = new TalonFXConfiguration();

    /**
     * Create a new base mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param canbus Name of the canbus to use for all devices (usually "rio" or "canivore")
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorIds CAN IDs of the follower motors. Leave empty for no followers.
     */
    public MechanismConfig(String name, String canbus, int leadMotorId, int[] followerMotorIds) {
        this.name = name;
        this.canbus = canbus;
        this.leadMotorId = leadMotorId;
        this.followerMotorIds = followerMotorIds;
    }
}
