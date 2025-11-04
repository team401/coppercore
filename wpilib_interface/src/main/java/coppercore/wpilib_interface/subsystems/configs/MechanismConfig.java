package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.signals.GravityTypeValue;

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
    /**
     * Represents whether a mechanism's gravity is static (like an elevator) or cosine-based (like
     * an arm)
     */
    public enum GravityFeedforwardType {
        STATIC_ELEVATOR,
        COSINE_ARM;

        /**
         * Convert this value to a Phoenix-6 compatible GravityTypeValue
         *
         * @return A GravityTypeValue, Elevator_Static for STATIC_ELEVATOR and Arm_Cosine for
         *     COSINE_ARM
         */
        public GravityTypeValue toPhoenix6GravityTypeValue() {
            return switch (this) {
                case STATIC_ELEVATOR -> GravityTypeValue.Elevator_Static;
                case COSINE_ARM -> GravityTypeValue.Arm_Cosine;
            };
        }
    }

    public final String name;
    public final CANDeviceID leadMotorId;

    public final MechanismFollowerMotorConfig[] followerMotorConfigs;

    public final GravityFeedforwardType gravityFeedforwardType;

    /**
     * Create a new base mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorConfigs Array of configs for follower IDs and inverts. Leave empty for no
     *     followers.
     */
    public MechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            GravityFeedforwardType gravityFeedforwardType) {
        this.name = name;
        this.leadMotorId = leadMotorId;
        this.followerMotorConfigs = followerMotorConfigs;
        this.gravityFeedforwardType = gravityFeedforwardType;
    }
}
