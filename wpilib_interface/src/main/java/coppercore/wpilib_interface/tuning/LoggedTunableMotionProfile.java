package coppercore.wpilib_interface.tuning;

import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecondPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import coppercore.parameter_tools.LoggedTunableMeasure;
import coppercore.parameter_tools.LoggedTunableMeasure.*;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.profile.MotionProfileConfig;
import coppercore.wpilib_interface.subsystems.motors.profile.MutableMotionProfileConfig;

public class LoggedTunableMotionProfile {
    LoggedAngularVelocity maxVelocity;
    LoggedAngularAcceleration maxAcceleration;
    LoggedAngularJerk maxJerk;
    LoggedVoltagePerAngularVelocity expoKV;
    LoggedVoltagePerAngularAcceleration expoKA;

    MutableMotionProfileConfig currentMotionProfile;

    public static final MotionProfileConfig defaultMotionProfileConfig =
            MotionProfileConfig.immutable(
                    RotationsPerSecond.zero(),
                    RotationsPerSecondPerSecond.zero(),
                    RotationsPerSecondPerSecond.zero().div(Seconds.of(1.0)),
                    Volts.of(0).div(RotationsPerSecond.of(1)),
                    Volts.of(0).div(RotationsPerSecondPerSecond.of(1)));

    /**
     * Creates tunables for a motion profile.
     *
     * @param namePrefix logged tunable path prefix
     * @param defaultProfile default profile values
     */
    public LoggedTunableMotionProfile(String namePrefix, MotionProfileConfig defaultProfile) {
        this(namePrefix, defaultProfile, false);
    }

    /**
     * Creates tunables for a motion profile with optional unit suffixes.
     *
     * @param namePrefix logged tunable path prefix
     * @param defaultProfile default profile values
     * @param addUnitSuffix whether to append unit names to tunable paths
     */
    public LoggedTunableMotionProfile(
            String namePrefix, MotionProfileConfig defaultProfile, boolean addUnitSuffix) {
        this.currentMotionProfile = defaultProfile.derive();

        this.maxVelocity =
                LoggedTunableMeasure.ANGULAR_VELOCITY.of(
                        namePrefix + "/MaxVelocity",
                        defaultProfile.getMaxVelocity(),
                        RotationsPerSecond,
                        addUnitSuffix);
        this.maxAcceleration =
                LoggedTunableMeasure.ANGULAR_ACCELERATION.of(
                        namePrefix + "/MaxAcceleration",
                        defaultProfile.getMaxAcceleration(),
                        RotationsPerSecondPerSecond,
                        addUnitSuffix);
        this.maxJerk =
                LoggedTunableMeasure.ANGULAR_JERK.of(
                        namePrefix + "/MaxJerk",
                        defaultProfile.getMaxJerk(),
                        RotationsPerSecondPerSecond.per(Seconds),
                        addUnitSuffix);
        this.expoKV =
                LoggedTunableMeasure.VOLTAGE_PER_ANGULAR_VELOCITY.of(
                        namePrefix + "/expoKV",
                        defaultProfile.getExpoKv(),
                        Volts.per(RotationsPerSecond),
                        addUnitSuffix);
        this.expoKA =
                LoggedTunableMeasure.VOLTAGE_PER_ANGULAR_ACCELERATION.of(
                        namePrefix + "/expoKA",
                        defaultProfile.getExpoKa(),
                        Volts.per(RotationsPerSecondPerSecond),
                        addUnitSuffix);
    }

    /**
     * Creates tunables using the default zeroed motion profile.
     *
     * @param namePrefix logged tunable path prefix
     */
    public LoggedTunableMotionProfile(String namePrefix) {
        this(namePrefix, defaultMotionProfileConfig);
    }

    /**
     * Gets the latest tuned motion profile.
     *
     * @return current motion profile config
     */
    public MotionProfileConfig getCurrentMotionProfile() {
        updateProfile(hashCode());
        return currentMotionProfile;
    }

    private boolean updateProfile(int id) {
        boolean hasChanged = false;
        if (maxVelocity.hasChanged(id)) {
            currentMotionProfile.withMaxVelocity(maxVelocity.get());
            hasChanged = true;
        }
        if (maxAcceleration.hasChanged(id)) {
            currentMotionProfile.withMaxAcceleration(maxAcceleration.get());
            hasChanged = true;
        }
        if (maxJerk.hasChanged(id)) {
            currentMotionProfile.withMaxJerk(maxJerk.get());
            hasChanged = true;
        }
        if (expoKV.hasChanged(id)) {
            currentMotionProfile.withExpoKv(expoKV.get());
            hasChanged = true;
        }
        if (expoKA.hasChanged(id)) {
            currentMotionProfile.withExpoKa(expoKA.get());
            hasChanged = true;
        }
        return hasChanged;
    }

    /**
     * Runs a callback when any profile value changes for a caller id.
     *
     * @param id caller id used to track changes independently
     * @param callback callback receiving the updated profile
     */
    public void ifChanged(int id, MotionProfileConsumer callback) {
        boolean hasChanged = updateProfile(id);
        if (hasChanged) {
            callback.accept(currentMotionProfile);
        }
    }

    /**
     * Creates a callback that applies profile constraints to one motor IO.
     *
     * @param motorIO motor IO to update
     * @return motion profile consumer for the motor IO
     */
    public MotionProfileConsumer getMotorIOApplier(MotorIO motorIO) {
        return motionProfile -> motorIO.setProfileConstraints(motionProfile);
    }

    /**
     * Creates a callback that applies profile constraints to multiple motor IOs.
     *
     * @param motorIOs motor IOs to update
     * @return motion profile consumer for all motor IOs
     */
    public MotionProfileConsumer getMotorIOAppliers(MotorIO... motorIOs) {
        return motionProfile -> {
            for (MotorIO motorIO : motorIOs) {
                motorIO.setProfileConstraints(motionProfile);
            }
        };
    }

    @FunctionalInterface
    public interface MotionProfileConsumer {
        MotionProfileConsumer noOp = motionProfile -> {};

        /**
         * Accepts an updated motion profile.
         *
         * @param motionProfile updated profile config
         */
        void accept(MotionProfileConfig motionProfile);

        /**
         * Chains another consumer after this one.
         *
         * @param after consumer to run after this one
         * @return combined consumer
         */
        default MotionProfileConsumer chain(MotionProfileConsumer after) {
            return motionProfile -> {
                accept(motionProfile);
                after.accept(motionProfile);
            };
        }

        /**
         * Gets a consumer that ignores profile updates.
         *
         * @return no-op consumer
         */
        static MotionProfileConsumer noOp() {
            return noOp;
        }
    }
}
