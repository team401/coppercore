package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.configs.TalonFXConfiguration;

/**
 * A base mechanism config.
 *
 * <p>Contains a name, CAN bus name, motor IDs for lead and follower motors, and a TalonFX config.
 *
 * <p>This config is used by the {@link
 * coppercore.wpilib_interface.subsystems.motors.MotorIOTalonFX} to initialize CAN IDs, motor
 * configs, and motor inverts.
 */
public class MechanismConfig {
    public final String name;
    public final CANDeviceID leadMotorId;

    public final MechanismFollowerMotorConfig[] followerMotorConfigs;

    public TalonFXConfiguration motorConfig;

    /**
     * Create a new base mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorConfigs Array of configs for follower IDs and inverts. Leave empty for no
     *     followers.
     * @param motorConfig The TalonFXConfiguration to apply to all motors
     */
    public MechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            TalonFXConfiguration motorConfig) {
        this.name = name;
        this.leadMotorId = leadMotorId;
        this.followerMotorConfigs = followerMotorConfigs;
    }
}
