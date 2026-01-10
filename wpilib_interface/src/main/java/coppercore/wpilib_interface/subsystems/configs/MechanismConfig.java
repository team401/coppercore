package coppercore.wpilib_interface.subsystems.configs;

import com.ctre.phoenix6.signals.GravityTypeValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A base mechanism config.
 *
 * <p>Contains a name, CAN bus name, motor IDs for lead and follower motors, and ratios between
 * motor and encoder and between encoder and mechanism.
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
        /**
         * Gravity Feedforward Type for an Elevator: Gravity is calculated by a static (positive)
         * value of kG that doesn't change based on the position of the mechanism. This feedforward
         * type should be used for all mechanisms that don't change orientation or change resistance
         * based on position.
         */
        STATIC_ELEVATOR,
        /**
         * Gravity Feedforward Type for an Arm: Gravity is calculated by taking the cosine of the
         * angle and multiplying it by kG. This is useful for systems like an arm, where changing
         * the angle of the mechanism changes the amount of force that gravity applies on the
         * system. Systems using this value must be calibrated such that an encoder angle of zero is
         * horizontal (or the center of mass is horizontal from the pivot) so that feedforward can
         * be accurately calculated.
         */
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

    /** The name of the Mechanism, used for logging. */
    public final String name;

    /** The CANDeviceID of the lead motor. */
    public final CANDeviceID leadMotorId;

    /**
     * An array of MechanismFollowerMotorConfig objects specifying the CANDeviceIDs and inverts of
     * all followers, if any are present.
     */
    public final MechanismFollowerMotorConfig[] followerMotorConfigs;

    /**
     * The type of calculation to use for gravity feedforward.
     *
     * @see GravityFeedforwardType The GravityFeedforwardType enum for more information on gravity
     *     feedforwards and which values may be used here.
     */
    public final GravityFeedforwardType gravityFeedforwardType;

    /**
     * The ratio of motor angle : encoder angle.
     *
     * <p>E.g. if motorToEncoderRatio = 5.0, 5 motor rotations = 1 encoder rotation.
     *
     * <p>If the mechanism does not have an external encoder, this ratio should be 1.0, so the value
     * can always be used for calculations.
     */
    public final double motorToEncoderRatio;

    /**
     * The ratio of encoder angle : mechanism angle.
     *
     * <p>E.g. if encoderToMechanismRatio = 5.0, 5 encoder rotations = 1 mechanism rotation.
     *
     * <p>If the mechanism does not have an external encoder, this ratio should be 1.0, so the value
     * can always be used for calculations.
     */
    public final double encoderToMechanismRatio;

    /**
     * Create a new base mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorConfigs Array of configs for follower IDs and inverts. Leave empty for no
     *     followers.
     * @param gravityFeedforwardType The type of gravity feedforward to use for the mechanism,
     *     either STATIC_ELEVATOR or COSINE_ARM.
     * @param motorToEncoderRatio Ratio of motor angle to encoder ratio. Encoder position *
     *     motorToEncoderRatio = motor position .
     * @param encoderToMechanismRatio A nonzero value representing the ratio of encoder rotations :
     *     mechanism rotations. This value can be calculated with (mechanism gear teeth / encoder
     *     gear teeth).
     */
    protected MechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            GravityFeedforwardType gravityFeedforwardType,
            double motorToEncoderRatio,
            double encoderToMechanismRatio) {
        this.name = name;
        this.leadMotorId = leadMotorId;
        this.followerMotorConfigs = followerMotorConfigs;
        this.gravityFeedforwardType = gravityFeedforwardType;
        this.motorToEncoderRatio = motorToEncoderRatio;
        this.encoderToMechanismRatio = encoderToMechanismRatio;
    }

    /**
     * Create a new builder object to gracefully create a MechanismConfig.
     *
     * @return a MechanismConfigBuilder with empty fields except for a default motorToEncoderRatio
     *     of 1.0 and a default encoderToMechanismRatio of 1.0.
     */
    public static MechanismConfigBuilder<?> builder() {
        return new MechanismConfigBuilder<>();
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
     *
     * <p>When using an external CANcoder with a ratio other than 1.0, `withMotorToEncoderRatio`
     * should be called once to configure that ratio.
     */
    public static class MechanismConfigBuilder<T extends MechanismConfigBuilder<T>> {
        String name = null;
        CANDeviceID leadMotorId = null;
        List<MechanismFollowerMotorConfig> followerMotorConfigs = new ArrayList<>();
        GravityFeedforwardType gravityFeedforwardType = null;
        double motorToEncoderRatio = 1.0;
        double encoderToMechanismRatio = 1.0;

        // Only allow MechanismConfigBuilder to be created using MechanismConfig.builder()
        /**
         * Create a new MechanismConfigBuilder with 1.0 ratios, no followers, and all other fields
         * null.
         *
         * <p>This constructor is protected so that this class may only be instantiated using
         * MechanismConfig.builder()
         */
        protected MechanismConfigBuilder() {}

        /**
         * Return this object, but with the correct type of any builder that may extend this one.
         *
         * @return This builder object
         */
        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        /**
         * Configure the Mechanism's name, used for logging and error reporting. Returns this
         * builder for easy method chaining.
         *
         * @param name A string, must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public T withName(String name) {
            Objects.requireNonNull(name, "mechanism name must not be null.");
            this.name = name;
            return self();
        }

        /**
         * Configure the lead motor device ID. Returns this builder for easy method chaining.
         *
         * @param leadMotorId A CANDeviceID with the correct CAN bus name and integer device ID.
         *     Must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public T withLeadMotorId(CANDeviceID leadMotorId) {
            Objects.requireNonNull(leadMotorId, "lead motor device ID must not be null.");
            Objects.requireNonNull(
                    leadMotorId.canbus(), "lead motor device id CAN bus must not be null.");
            this.leadMotorId = leadMotorId;
            return self();
        }

        /**
         * Configure whether the device should have arm/cosine feedforward or elevator/static
         * feedforward. Returns this builder for easy method chaining.
         *
         * @param gravityFeedforwardType A GravityFeedforwardType (either STATIC_ELEVATOR or
         *     COSINE_ARM). Must not be null.
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public T withGravityFeedforwardType(GravityFeedforwardType gravityFeedforwardType) {
            Objects.requireNonNull(name, "gravity feedforward type must not be null.");
            this.gravityFeedforwardType = gravityFeedforwardType;
            return self();
        }

        /**
         * Add a follower motor to this config. Returns this builder for easy method chaining.
         *
         * @param id A CANDeviceID with the follower motor's CAN bus and integer id. The follower
         *     must be on the same CAN bus as the leader motor.
         * @param invert A boolean describing whether or not the follower should spin in the
         *     opposite direction as the leader. True if the follower should oppose the leader's
         *     direction, false if not.
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public T addFollower(CANDeviceID id, boolean invert) {
            Objects.requireNonNull(id, "follower device id must not be null.");
            Objects.requireNonNull(id.canbus(), "follower device id CAN bus must not be null.");
            followerMotorConfigs.add(new MechanismFollowerMotorConfig(id, invert));
            return self();
        }

        /**
         * Configure the motor to encoder ratio when using an external CANcoder with a ratio other
         * than 1.0.
         *
         * <p>For example, a value of 5.0 means that the motor will turn 5 full rotations for each
         * rotation of the encoder.
         *
         * <p>This value must be nonzero (a ratio of zero means that the motor would not turn at all
         * and the encoder would magically change) and positive (encoder direction should be handled
         * in the CANCoder config's invert settings such that lead motor positive rotation =
         * CANcoder positive rotation).
         *
         * @param motorToEncoderRatio A positive value representing the ratio of motor rotor
         *     rotations : encoder rotations. This value can be calculated with (encoder gear teeth
         *     / motor gear teeth).
         * @return this MechanismConfigBuilder, for easy method chaining
         */
        public T withMotorToEncoderRatio(double motorToEncoderRatio) {
            if (motorToEncoderRatio <= 0.0) {
                throw new IllegalArgumentException(
                        "motor to encoder ratio must be positive (and nonzero), but was configured"
                                + " with value "
                                + motorToEncoderRatio
                                + ".");
            }
            this.motorToEncoderRatio = motorToEncoderRatio;
            return self();
        }

        /**
         * Configure the encoder to mechanism ratio when using an external CANCoder with a ratio
         * other than 1.0.
         *
         * <p>For example, a value of 5.0 means that the encoder will turn 5 full rotations for each
         * rotation of the mechanism.
         *
         * <p>This value must be nonzero (a ratio of zero means that the encoder would not turn at
         * all and the mechanism would magically change).
         *
         * @param encoderToMechanismRatio A nonzero value representing the ratio of encoder
         *     rotations : mechanism rotations. This value can be calculated with (mechanism gear
         *     teeth / encoder gear teeth).
         * @return this MechanismConfigBuilder, for easy method chaining
         */
        public T withEncoderToMechanismRatio(double encoderToMechanismRatio) {
            if (encoderToMechanismRatio == 0.0) {
                throw new IllegalArgumentException(
                        "encoder to mechanism ratio must be nonzero, but was configured with value "
                                + motorToEncoderRatio
                                + ".");
            }
            this.encoderToMechanismRatio = encoderToMechanismRatio;
            return self();
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
                    followerMotorConfigs.toArray(MechanismFollowerMotorConfig[]::new);
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
                if (!followerConfig.id().canbus().equals(leadMotorId.canbus())) {
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

            if (motorToEncoderRatio <= 0.0) {
                throw new IllegalArgumentException(
                        "motor to encoder ratio must be positive (and nonzero), but was configured"
                                + " with value "
                                + motorToEncoderRatio
                                + ".");
            }

            if (encoderToMechanismRatio == 0.0) {
                throw new IllegalArgumentException(
                        "encoder to mechanism ratio must be nonzero, but was configured with value "
                                + motorToEncoderRatio
                                + ".");
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
                    followerMotorConfigs.toArray(MechanismFollowerMotorConfig[]::new);

            return new MechanismConfig(
                    name,
                    leadMotorId,
                    followerConfigsArray,
                    gravityFeedforwardType,
                    motorToEncoderRatio,
                    encoderToMechanismRatio);
        }
    }
}
