package coppercore.wpilib_interface.subsystems.motors.profile;

import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecondPerSecond;
import static edu.wpi.first.units.Units.Second;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.VelocityUnit;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MutAngularAcceleration;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVelocity;
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
 */
public final class MutableMotionProfileConfig extends MotionProfileConfig {
    private MutAngularVelocity maxVelocity;
    private MutAngularAcceleration maxAcceleration;
    private MutVelocity<AngularAccelerationUnit> maxJerk;

    private double expoKv;
    private double expoKa;

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
    public MutableMotionProfileConfig(
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {
        this.maxVelocity = maxVelocity.mutableCopy();
        this.maxAcceleration = maxAcceleration.mutableCopy();
        this.maxJerk = maxJerk.mutableCopy();
        this.expoKv = expoKv;
        this.expoKa = expoKa;
    }

    /**
     * Create a new config by copying another config.
     *
     * @param other The config to copy from.
     */
    public MutableMotionProfileConfig(MotionProfileConfig other) {
        this.maxVelocity = other.getMaxVelocity().mutableCopy();
        this.maxAcceleration = other.getMaxAcceleration().mutableCopy();
        this.maxJerk = other.getMaxJerk().mutableCopy();
        this.expoKv = other.getExpoKv();
        this.expoKa = other.getExpoKa();
    }

    /** Create a new config by setting all fields to zero. */
    public MutableMotionProfileConfig() {
        this.maxVelocity = RotationsPerSecond.mutable(0.0);
        this.maxAcceleration = RotationsPerSecondPerSecond.mutable(0.0);
        this.maxJerk =
                Velocity.ofRelativeUnits(
                                0.0, VelocityUnit.combine(RotationsPerSecondPerSecond, Second))
                        .mutableCopy();
        this.expoKv = 0;
        this.expoKa = 0;
    }

    /**
     * Create a new MutableMotionProfileConfig with all fields set to zero
     *
     * <p>This is equivalent to the default constructor, {@link #MutableMotionProfileConfig()}, but
     * exists to create a more readable option.
     *
     * <p>A config with all zeros effectively uncaps all constraints, or can be used in combination
     * with the `.with...` methods to create configs in a more readable format
     *
     * @return A new config with all fields set to zero
     */
    public static MutableMotionProfileConfig zeros() {
        return new MutableMotionProfileConfig();
    }

    /**
     * Converts this motion profile config into a motion magic config
     *
     * @return a MotionMagicConfigs object with fields from this configuration
     */
    public MotionMagicConfigs asMotionMagicConfigs() {
        return new MotionMagicConfigs()
                .withMotionMagicCruiseVelocity(this.maxVelocity)
                .withMotionMagicAcceleration(this.maxAcceleration)
                .withMotionMagicJerk(this.maxJerk)
                .withMotionMagicExpo_kV(this.expoKv)
                .withMotionMagicExpo_kA(this.expoKa);
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
    public double getExpoKv() {
        return this.expoKv;
    }

    @Override
    public double getExpoKa() {
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
    public MutableMotionProfileConfig withExpoKv(double newExpoKv) {
        this.expoKv = newExpoKv;
        return this;
    }

    /**
     * Replaces this config's exponential kA with newExpoKa and returns itself for easy method
     * chaining.
     *
     * @param newExpoKa The new max expoKa to set in the config.
     * @return This object, for easy chaining.
     */
    public MutableMotionProfileConfig withExpoKa(double newExpoKa) {
        this.expoKa = newExpoKa;
        return this;
    }
}
