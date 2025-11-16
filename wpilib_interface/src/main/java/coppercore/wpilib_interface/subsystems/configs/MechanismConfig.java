package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.signals.GravityTypeValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A base mechanism config.
 *
 * <p>Contains a name, CAN bus name, motor IDs for lead and follower motors, and a TalonFX config.
 *
 * <p>This config is used by the {@link
 * coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFX} to initialize CAN IDs,
 * motor configs, and motor inverts.
 *
 * <p>Use a {@link MechanismConfig#builder()} to create this config.
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
     * @param gravityFeedforwardType The type of gravity feedforward to use for the mechanism,
     *     either STATIC_ELEVATOR or COSINE_ARM.
     */
    protected MechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            GravityFeedforwardType gravityFeedforwardType) {
        this.name = name;
        this.leadMotorId = leadMotorId;
        this.followerMotorConfigs = followerMotorConfigs;
        this.gravityFeedforwardType = gravityFeedforwardType;
    }

    /** Create a new builder object to gracefully create a MechanismConfig. */
    public static MechanismConfigBuilder builder() {
        return new MechanismConfigBuilder();
    }

    /**
     * A builder class to easily create MechanismConfigs.
     *
     * <p>To create a MechanismConfigBuilder, use {@link MechanismConfig#builder}
     *
     * <p>`withName`, `withLeadMotorId`, and `withGravityFeedforwardType` must be called before
     * calling `build` to create the object.
     *
     * <p>`addFollower` should be used for each follower, or not called to indicate no followers.
     */
    public static class MechanismConfigBuilder {
        String name = null;
        CANDeviceID leadMotorId = null;
        List<MechanismFollowerMotorConfig> followerMotorConfigs = new ArrayList<>();
        GravityFeedforwardType gravityFeedforwardType = null;

        // Only allow MechanismConfigBuilder to be created using MechanismConfig.builder()
        protected MechanismConfigBuilder() {}

        /**
         * Configure the Mechanism's name, used for logging and error reporting. Returns this
         * builder for easy method chaining.
         *
         * @param name A string, must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public MechanismConfigBuilder withName(String name) {
            Objects.requireNonNull(name, "mechanism name must not be null.");
            this.name = name;
            return this;
        }

        /**
         * Configure the lead motor device ID. Returns this builder for easy method chaining.
         *
         * @param leadMotorId A CANDeviceID with the correct CAN bus name and integer device ID.
         *     Must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public MechanismConfigBuilder withLeadMotorId(CANDeviceID leadMotorId) {
            Objects.requireNonNull(leadMotorId, "lead motor device ID must not be null.");
            Objects.requireNonNull(
                    leadMotorId.canbus(), "lead motor device id CAN bus must not be null.");
            this.leadMotorId = leadMotorId;
            return this;
        }

        /**
         * Configure whether the device should have arm/cosine feedforward or elevator/static
         * feedforward. Returns this builder for easy method chaining.
         *
         * @param gravityFeedforwardType A GravityFeedforwardType (either STATIC_ELEVATOR or
         *     COSINE_ARM). Must not be null.
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public MechanismConfigBuilder withGravityFeedforwardType(
                GravityFeedforwardType gravityFeedforwardType) {
            Objects.requireNonNull(name, "gravity feedforward type must not be null.");
            this.gravityFeedforwardType = gravityFeedforwardType;
            return this;
        }

        /**
         * Add a follower motor to this config. Returns this builder fore asy method chaining.
         *
         * @param id A CANDeviceID with the follower motor's CAN bus and integer id. The follower
         *     must be on the same CAN bus as the leader motor.
         * @param invert A boolean describing whether or not the follower should spin in the
         *     opposite direction as the leader. True if the follower should oppose the leader's
         *     direction, false if not.
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public MechanismConfigBuilder addFollower(CANDeviceID id, boolean invert) {
            Objects.requireNonNull(id, "follower device id must not be null.");
            Objects.requireNonNull(id.canbus(), "follower device id CAN bus must not be null.");
            followerMotorConfigs.add(new MechanismFollowerMotorConfig(id, invert));
            return this;
        }

        /**
         * Verify that all parameters are not null and that CAN bus names match between leader and
         * follower motor(s).
         *
         * <p>withName, withLeadMotorId, and withGravityFeedforwardType must have been called before
         * attempting to build the config.
         *
         * <p>This method will throw an IllegalArgumentException in the case of a null or invalid
         * argument.
         *
         * <p>This method is separated from build() so that it can be extended more easily by
         * allowing subclasses to call `super.validateConfigBeforeBuilding()`.
         */
        protected void validateBeforeBuilding() {
            MechanismFollowerMotorConfig[] followerConfigsArray =
                    followerMotorConfigs.toArray(new MechanismFollowerMotorConfig[] {});
            Objects.requireNonNull(
                    name, "name must be configured with `withName(...)` prior to calling build()");
            Objects.requireNonNull(
                    leadMotorId,
                    "leadMotorId must be configured with `withLeadMotorId(...)` prior to calling"
                            + " build()");
            Objects.requireNonNull(
                    gravityFeedforwardType,
                    "gravityFeedforwardType must be configured with"
                            + " `withGravityFeedforwardType(...)` prior to calling build()");

            for (MechanismFollowerMotorConfig followerConfig : followerConfigsArray) {
                if (followerConfig.id().canbus() != leadMotorId.canbus()) {
                    throw new IllegalArgumentException(
                            "Follower with CAN ID + "
                                    + followerConfig.id().id()
                                    + " has different CAN bus ("
                                    + followerConfig.id().canbus()
                                    + ") than lead motor ("
                                    + leadMotorId.canbus()
                                    + "). Followers must be on the same CAN bus as the lead"
                                    + " motor.");
                }
            }
        }

        /**
         * Validate parameters and build a MechanismConfig using the values configured in this
         * builder.
         *
         * <p>withName, withLeadMotorId, and withGravityFeedforwardType must have been called before
         * attempting to build the config.
         *
         * @return The MechanismConfig created.
         */
        public MechanismConfig build() {
            validateBeforeBuilding();

            MechanismFollowerMotorConfig[] followerConfigsArray =
                    followerMotorConfigs.toArray(new MechanismFollowerMotorConfig[] {});

            return new MechanismConfig(
                    name, leadMotorId, followerConfigsArray, gravityFeedforwardType);
        }
    }
}
