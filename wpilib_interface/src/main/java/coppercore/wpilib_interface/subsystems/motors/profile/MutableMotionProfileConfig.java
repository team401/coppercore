package coppercore.wpilib_interface.subsystems.motors.profile;

import static coppercore.wpilib_interface.UnitUtils.VoltsPerRotationPerSecond;
import static coppercore.wpilib_interface.UnitUtils.VoltsPerRotationPerSecondSquared;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecondPerSecond;
import static edu.wpi.first.units.Units.Second;

import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.VelocityUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MutAngularAcceleration;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutPer;
import edu.wpi.first.units.measure.MutVelocity;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Velocity;

/**
 * A generic, mutable motion profile configuration, with methods for modifying its fields.
 *
 * <p>For an immutable/constant config, see {@link ImmutableMotionProfileConfig} or {@link
 * MotionProfileConfig#immutable}
 *
 * <p>Leaving any value at 0 signifies that it is uncapped
 *
 * <p>This class is almost identical to a Phoenix-6 MotionMagicConfigs object except that a
 * coppercore MotionProfileConfig stores all values that can reasonably have units attached with
 * units, whereas MotionMagicExpoConfigs use doubles.
 *
 * <p>Fields:
 *
 * <ul>
 *   <li><b>maxVelocity</b> - The maximum/"cruise" velocity of the profile
 *   <li><b>maxAcceleration</b> - The maximum acceleration of the profile
 *   <li><b>maxJerk</b> - The maximum allowed jerk of the profile
 *   <li><b>expoKv</b> - The kV for MotionMagicExpo/exponential motion profile. Represents the
 *       output required to maintain a certain velocity. Is in units of Output Unit / Rotations per
 *       Second. As a result, when supply voltage is fixed, a higher profile kV results in a lower
 *       profile velocity.
 *   <li><b>expoKa</b> - The kA for MotionMagicExpo/exponential motion profile. Represents the
 *       output required to apply a given acceleration. Is in units of Output Unit / (Rotations per
 *       Second / Second). As a result, when supply voltage is fixed, a higher profile kA results in
 *       a lower profile acceleration.
 * </ul>
 */
public final class MutableMotionProfileConfig extends MotionProfileConfig {
    private MutAngularVelocity maxVelocity;
    private MutAngularAcceleration maxAcceleration;
    private MutVelocity<AngularAccelerationUnit> maxJerk;

    private MutPer<VoltageUnit, AngularVelocityUnit> expoKv;
    private MutPer<VoltageUnit, AngularAccelerationUnit> expoKa;

    /**
     * A generic, mutable motion profile configuration
     *
     * <p>Leaving any value at 0 signifies that it is uncapped
     *
     * <p>This class is almost identical to a Phoenix-6 MotionMagicConfigs object except that a
     * coppercore MotionProfileConfig stores all values that can reasonably have units attached with
     * units, whereas MotionMagicExpoConfigs use doubles.
     *
     * @param maxVelocity The maximum/"cruise" velocity of the profile
     * @param maxAcceleration The maximum acceleration of the profile
     * @param maxJerk The maximum allowed jerk of the profile
     * @param expoKv The kV for MotionMagicExpo/exponential motion profile. Represents the output
     *     required to maintain a certain velocity. Is in units of Output Unit / Rotations per
     *     Second. As a result, when supply voltage is fixed, a higher profile kV results in a lower
     *     profile velocity.
     * @param expoKa The kA for MotionMagicExpo/exponential motion profile. Represents the output
     *     required to apply a given acceleration. Is in units of Output Unit / (Rotations per
     *     Second / Second). As a result, when supply voltage is fixed, a higher profile kA results
     *     in a lower profile acceleration.
     */
    protected MutableMotionProfileConfig(
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            Per<VoltageUnit, AngularVelocityUnit> expoKv,
            Per<VoltageUnit, AngularAccelerationUnit> expoKa) {
        this.maxVelocity = maxVelocity.mutableCopy();
        this.maxAcceleration = maxAcceleration.mutableCopy();
        this.maxJerk = maxJerk.mutableCopy();
        this.expoKv = expoKv.mutableCopy();
        this.expoKa = expoKa.mutableCopy();
    }

    /**
     * Create a new config by copying another config.
     *
     * @param other The config to copy from.
     */
    protected MutableMotionProfileConfig(MotionProfileConfig other) {
        this.maxVelocity = other.getMaxVelocity().mutableCopy();
        this.maxAcceleration = other.getMaxAcceleration().mutableCopy();
        this.maxJerk = other.getMaxJerk().mutableCopy();
        this.expoKv = other.getExpoKv().mutableCopy();
        this.expoKa = other.getExpoKa().mutableCopy();
    }

    /** Create a new config by setting all fields to zero. */
    private MutableMotionProfileConfig() {
        this.maxVelocity = RotationsPerSecond.mutable(0.0);
        this.maxAcceleration = RotationsPerSecondPerSecond.mutable(0.0);
        this.maxJerk =
                Velocity.ofRelativeUnits(
                                0.0, VelocityUnit.combine(RotationsPerSecondPerSecond, Second))
                        .mutableCopy();
        this.expoKv = VoltsPerRotationPerSecond.mutableNative(0.0);
        this.expoKa = VoltsPerRotationPerSecondSquared.mutableNative(0.0);
    }

    /**
     * Create a new MutableMotionProfileConfig with all fields set to zero
     *
     * <p>This is equivalent to the default constructor, {@link #MutableMotionProfileConfig()}, but
     * exists to create a more readable option.
     *
     * <p>A config with all zeros effectively uncaps all constraints, or can be used in combination
     * with the `.with...` methods to create configs in a more readable format:
     *
     * <pre>{@code
     * MutableMotionProfileConfig.zeros()
     *     .withMaxVelocity(RotationsPerSecond.of(1.0))
     *     .withMaxAcceleration(RotationsPerSecondPerSecond.of(3.0))
     *     .freeze();
     * }</pre>
     *
     * @return A new config with all fields set to zero
     */
    public static MutableMotionProfileConfig zeros() {
        return new MutableMotionProfileConfig();
    }

    /**
     * Copy this config's parameters into a new immutable config and then return it.
     *
     * <p>This is useful for converting a config created using the `.with...` syntax back to be
     * immutable, as seen below:
     *
     * <pre>{@code
     * MutableMotionProfileConfig.zeros()
     *     .withMaxVelocity(RotationsPerSecond.of(1.0))
     *     .withMaxAcceleration(RotationsPerSecondPerSecond.of(3.0))
     *     .freeze();
     * }</pre>
     *
     * @return The new copy
     */
    public ImmutableMotionProfileConfig freeze() {
        return new ImmutableMotionProfileConfig(
                this.maxVelocity.copy(),
                this.maxAcceleration.copy(),
                this.maxJerk.copy(),
                this.expoKv,
                this.expoKa);
    }

    @Override
    public AngularVelocity getMaxVelocity() {
        return this.maxVelocity;
    }

    @Override
    public AngularAcceleration getMaxAcceleration() {
        return this.maxAcceleration;
    }

    @Override
    public Velocity<AngularAccelerationUnit> getMaxJerk() {
        return this.maxJerk;
    }

    @Override
    public Per<VoltageUnit, AngularVelocityUnit> getExpoKv() {
        return this.expoKv;
    }

    @Override
    public Per<VoltageUnit, AngularAccelerationUnit> getExpoKa() {
        return this.expoKa;
    }

    // === with... methods ===
    /**
     * Replaces this config's max velocity with newMaxVelocity and returns itself for easy method
     * chaining.
     *
     * @param newMaxVelocity The new max velocity to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withMaxVelocity(AngularVelocity newMaxVelocity) {
        this.maxVelocity.mut_replace(newMaxVelocity);
        return this;
    }

    /**
     * Replaces this config's max acceleration with newMaxAcceleration and returns itself for easy
     * method chaining.
     *
     * @param newMaxAcceleration The new max acceleration to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withMaxAcceleration(AngularAcceleration newMaxAcceleration) {
        this.maxAcceleration.mut_replace(newMaxAcceleration);
        return this;
    }

    /**
     * Replaces this config's max jerk with newMaxJerk and returns itself for easy method chaining.
     *
     * @param newMaxJerk The new max jerk to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withMaxJerk(Velocity<AngularAccelerationUnit> newMaxJerk) {
        this.maxJerk.mut_replace(newMaxJerk);
        return this;
    }

    /**
     * Replaces this config's exponential kV with newExpoKv and returns itself for easy method
     * chaining.
     *
     * @param newExpoKv The new max expoKv to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withExpoKv(Per<VoltageUnit, AngularVelocityUnit> newExpoKv) {
        this.expoKv.mut_replace(newExpoKv);
        return this;
    }

    /**
     * Replaces this config's exponential kA with newExpoKa and returns itself for easy method
     * chaining.
     *
     * @param newExpoKa The new max expoKa to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withExpoKa(
            Per<VoltageUnit, AngularAccelerationUnit> newExpoKa) {
        this.expoKa.mut_replace(newExpoKa);
        return this;
    }
}
