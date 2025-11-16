package coppercore.wpilib_interface.subsystems.configs;

import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.DistanceUnit;
import edu.wpi.first.units.measure.Per;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Rotations;

import java.util.Objects;

/**
 * The ElevatorMechanismConfig class extends the functionality of a {@link MechanismConfig} to
 * include a conversion ratio between lead motor position and elevator position.
 *
 * <p>Use a {@link ElevatorMechanismConfig#builder()} to create this config.
 */
public class ElevatorMechanismConfig extends MechanismConfig {
    /**
     * A ratio of distance to angle that relates how much the elevator moves per rotation of the
     * motor(s).
     *
     * <p>For example, motor position * elevator to motor ratio = elevator height.
     */
    public final Per<DistanceUnit, AngleUnit> elevatorToMotorRatio;

    /**
     * Create a new elevator mechanism config.
     *
     * @param name Mechanism name, used for logging
     * @param leadMotorId CAN ID of the lead motor
     * @param followerMotorConfigs Array of configs for follower IDs and inverts. Leave empty for no
     *     followers.
     * @param gravityFeedforwardType The type of gravity feedforward to use for the mechanism,
     *     either STATIC_ELEVATOR or COSINE_ARM.
     * @param motorToEncoderRatio Ratio of motor angle to encoder ratio. Encoder position *
     *     motorToEncoderRatio = motor position .
     * @param elevatorToMotorRatio A ratio of distance to angle that relates elevator motion to
     *     motion of the motor(s).
     */
    protected ElevatorMechanismConfig(
            String name,
            CANDeviceID leadMotorId,
            MechanismFollowerMotorConfig[] followerMotorConfigs,
            GravityFeedforwardType gravityFeedforwardType,
            double motorToEncoderRatio,
            Per<DistanceUnit, AngleUnit> elevatorToMotorRatio) {
        super(name, leadMotorId, followerMotorConfigs, gravityFeedforwardType, motorToEncoderRatio);

        this.elevatorToMotorRatio = elevatorToMotorRatio;
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
    public static class ElevatorMechanismConfigBuilder extends MechanismConfigBuilder {
        Per<DistanceUnit, AngleUnit> elevatorToMotorRatio;

        // Only allow ElevatorMechanismConfigBuilder to be created using
        // ElevatorMechanismConfig.builder()
        protected ElevatorMechanismConfigBuilder() {}

        /**
         * Configure the elevator to motor ratio. Returns this builder for easy method chaining.
         *
         * <p>Elevator to motor ratio is a ratio of elevator distance moved : motor angle moved.
         *
         * <p>For example, motor position * elevator to motor ratio = elevator height.
         *
         * @param elevatorToMotorRatio The ratio of elevator distance : motor angle. Can be created by: <pre>{@code Inches.of(5.0).div(Rotations.of(1.0))}</pre>. Must not be null
         * @return This MechanismConfigBuilder, for easy method chaining
         */
        public ElevatorMechanismConfigBuilder withElevatorToMotorRatio(
                Per<DistanceUnit, AngleUnit> elevatorToMotorRatio) {
            Objects.requireNonNull(
                    elevatorToMotorRatio, "elevator to motor ratio must not be null.");
            this.elevatorToMotorRatio = elevatorToMotorRatio;
            return this;
        }

        /**
         * Performs all checks present in {@link MechanismConfig#validateBeforeBuilding()} and then
         * checks to verify that withElevatorToMotorRatio has been called with a non-null value.
         *
         * <p>This method will throw an IllegalArgumentException in the case of an invalid argument.
         */
        @Override
        protected void validateBeforeBuilding() {
            super.validateBeforeBuilding();

            Objects.requireNonNull(
                    elevatorToMotorRatio,
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
                    elevatorToMotorRatio);
        }
    }
}
