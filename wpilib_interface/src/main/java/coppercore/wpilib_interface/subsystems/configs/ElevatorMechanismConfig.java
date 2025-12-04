package coppercore.wpilib_interface.subsystems.configs;

import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.DistanceUnit;
import edu.wpi.first.units.measure.Per;
import java.util.Objects;

/**
 * The ElevatorMechanismConfig class extends the functionality of a {@link MechanismConfig} to
 * include a conversion ratio between encoder/lead motor position and elevator position.
 *
 * <p>Use a {@link ElevatorMechanismConfig#builder()} to create this config.
 */
public class ElevatorMechanismConfig extends MechanismConfig {
    /**
     * A ratio of distance to angle that relates how much the elevator moves per rotation of the
     * "mechanism". The "mechanism" is the concept described by the encoderToMechanismRatio value of
     * the config.
     *
     * <p>For example, encoder position / encoderToMechanismRatio * elevatorToMechanismRatio =
     * elevator height.
     */
    public final Per<DistanceUnit, AngleUnit> elevatorToMechanismRatio;

    /**
     * Create a new elevator mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorConfigs Array of configs for follower IDs and inverts. Leave empty for no
     *     followers.
     * @param gravityFeedforwardType The type of gravity feedforward to use for the mechanism,
     *     either STATIC_ELEVATOR or COSINE_ARM.
     * @param motorToEncoderRatio Ratio of motor angle to encoder angle. Encoder position *
     *     motorToEncoderRatio = motor position .
     * @param encoderToMechanismRatio Ratio of encoder angle to mechanism angle. Mechanism position
     *     * encoderToMechanismRatio = encoder position .
     * @param elevatorToMechanismRatio A ratio of distance to angle that relates elevator motion to
     *     motion of the mechanism.
     */
    protected ElevatorMechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            GravityFeedforwardType gravityFeedforwardType,
            double motorToEncoderRatio,
            double encoderToMechanismRatio,
            Per<DistanceUnit, AngleUnit> elevatorToMechanismRatio) {
        super(
                name,
                leadMotorId,
                followerMotorConfigs,
                gravityFeedforwardType,
                motorToEncoderRatio,
                encoderToMechanismRatio);

        this.elevatorToMechanismRatio = elevatorToMechanismRatio;
    }

    /**
     * Create a new builder object to gracefully create an ElevatorMechanismConfig.
     *
     * @return an ElevatorMechanismConfigBuilder with empty fields except for a default
     *     motorToEncoderRatio of 1.0 and a default encoderToMechanismRatio of 1.0.
     */
    public static ElevatorMechanismConfigBuilder<?> builder() {
        return new ElevatorMechanismConfigBuilder<>();
    }

    /**
     * A builder class to easily create ElevatorMechanismConfigs.
     *
     * <p>To create an ElevatorMechanismConfigBuilder, use {@link ElevatorMechanismConfig#builder}
     *
     * <p>`withName`, `withLeadMotorId`, `withGravityFeedforwardType`, and
     * `withElevatorToMotorRatio` must be called before calling `build` to create the object.
     *
     * <p>`addFollower` should be used for each follower, or not called to indicate no followers.
     */
    public static class ElevatorMechanismConfigBuilder<T extends ElevatorMechanismConfigBuilder<T>>
            extends MechanismConfigBuilder<ElevatorMechanismConfigBuilder<T>> {
        Per<DistanceUnit, AngleUnit> elevatorToMechanismRatio;

        // Only allow ElevatorMechanismConfigBuilder to be created using
        // ElevatorMechanismConfig.builder()
        protected ElevatorMechanismConfigBuilder() {}

        /**
         * Return this object, but with the correct type of any builder that may extend this one.
         */
        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        /**
         * Configure the elevator to mechanism ratio. Returns this builder for easy method chaining.
         *
         * <p>Elevator to mechanism ratio is a ratio of elevator distance moved : mechanism angle
         * moved.
         *
         * <p>The "mechanism" here is the mechanism configured in the remainder of the config, by
         * motorToEncoderRatio and encoderToMechanismRatio. Usually, the mechanism should be
         * configured as a 1:1 with the encoder, or a 1:1 with the elevator spool.
         *
         * <p>For example, mechanism position * elevator to motor ratio = elevator height.
         *
         * @param elevatorToMechanismRatio The ratio of elevator distance : mechanism angle. Can be
         *     created by something like the following:
         *     <pre>{@code Inches.of(5.0).div(Rotations.of(1.0))}</pre>
         *     Must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public T withElevatorToMechanismRatio(
                Per<DistanceUnit, AngleUnit> elevatorToMechanismRatio) {
            Objects.requireNonNull(
                    elevatorToMechanismRatio, "elevator to mechanism ratio must not be null.");
            this.elevatorToMechanismRatio = elevatorToMechanismRatio;
            return self();
        }

        /**
         * Performs all checks present in {@link MechanismConfigBuilder#validateBeforeBuilding} and
         * then checks to verify that withElevatorToMotorRatio has been called with a non-null
         * value.
         *
         * <p>This method will throw an IllegalArgumentException in the case of an invalid argument.
         */
        @Override
        protected void validateBeforeBuilding() {
            super.validateBeforeBuilding();

            Objects.requireNonNull(
                    elevatorToMechanismRatio,
                    "elevatorToMotorRatio must be configured with `.withElevatorToMotorRatio(...)`"
                            + " prior to calling build()");
        }

        /**
         * Validate parameters and build an ElevatorMechanismConfig using the values configured in
         * this builder.
         *
         * <p>withName, withLeadMotorId, withGravityFeedforwardType, and withElevatorToMotorRatio
         * must have been called before * attempting to build the config.
         *
         * @return The MechanismConfig created.
         */
        @Override
        public ElevatorMechanismConfig build() {
            validateBeforeBuilding();

            MechanismFollowerMotorConfig[] followerConfigsArray =
                    followerMotorConfigs.toArray(new MechanismFollowerMotorConfig[] {});

            return new ElevatorMechanismConfig(
                    name,
                    leadMotorId,
                    followerConfigsArray,
                    gravityFeedforwardType,
                    motorToEncoderRatio,
                    encoderToMechanismRatio,
                    elevatorToMechanismRatio);
        }
    }
}
