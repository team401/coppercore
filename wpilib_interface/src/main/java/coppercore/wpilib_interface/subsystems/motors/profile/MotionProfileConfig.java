package coppercore.wpilib_interface.subsystems.motors.profile;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Velocity;

/**
 * A generic motion profile configuration
 *
 * <p>Leaving any value at 0 signifies that it is uncapped
 *
 * <p>This class is almost identical to a Phoenix-6 MotionMagicConfigs object except that a
 * coppercore MotionProfileConfig stores all values with units attached using the WPILib units
 * library, whereas MotionMagicExpoConfigs use doubles.
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
public abstract sealed class MotionProfileConfig implements Cloneable
        permits MutableMotionProfileConfig, ImmutableMotionProfileConfig {
    /**
     * Create a new immutable motion profile configuration.
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
    public static ImmutableMotionProfileConfig immutable(
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            Per<VoltageUnit, AngularVelocityUnit> expoKv,
            Per<VoltageUnit, AngularAccelerationUnit> expoKa) {
        return new ImmutableMotionProfileConfig(
                maxVelocity, maxAcceleration, maxJerk, expoKv, expoKa);
    }

    /**
     * Create a new mutable motion profile configuration.
     *
     * <p>Leaving any value at 0 signifies that it is uncapped
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
    public static MutableMotionProfileConfig mutable(
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            Per<VoltageUnit, AngularVelocityUnit> expoKv,
            Per<VoltageUnit, AngularAccelerationUnit> expoKa) {
        return new MutableMotionProfileConfig(
                maxVelocity, maxAcceleration, maxJerk, expoKv, expoKa);
    }

    /**
     * Copy this config's parameters into a new mutable config and then return it.
     *
     * <p>This is useful for creating a new config using the `.with...` syntax from a base/default
     * immutable config, as seen below:
     *
     * <pre>{@code
     * immutableConfig.derive()
     *     .withExpoKv(0.5)
     *     .withExpoKa(0.2);
     * }</pre>
     *
     * <p>This method behaves identically to `clone` on a Mutable config.
     *
     * @return A new, mutable copy of this config.
     */
    public MutableMotionProfileConfig derive() {
        return new MutableMotionProfileConfig(this);
    }

    /**
     * Make a new copy of this object, maintaining its mutability/immutability.
     *
     * <p>This method also copies mutable measures, where needed.
     *
     * @return The new copy.
     */
    public abstract MotionProfileConfig clone();

    /**
     * Get the maximum velocity of the motion profile.
     *
     * @return An AngularVelocity representing either the max velocity, if it is capped, or 0 if it
     *     is uncapped.
     */
    public abstract AngularVelocity getMaxVelocity();

    /**
     * Get the maximum acceleration of the motion profile.
     *
     * @return An AngularAcceleration representing either the max acceleration, if it is capped, or
     *     0 if it is uncapped.
     */
    public abstract AngularAcceleration getMaxAcceleration();

    /**
     * Get the maximum jerk of the motion profile.
     *
     * @return A measure of AngularAcceleration per time representing either the max jerk, if it is
     *     capped, or 0 if it is uncapped.
     */
    public abstract Velocity<AngularAccelerationUnit> getMaxJerk();

    /**
     * Get the exponential profile Kv of the motion profile.
     *
     * @return A double representing expo Kv, if it is capped, or 0 if expo Kv limiting is not used
     */
    public abstract Per<VoltageUnit, AngularVelocityUnit> getExpoKv();

    /**
     * Get the exponential profile Ka of the motion profile.
     *
     * @return A double representing expo Ka, if it is capped, or 0 if expo Ka limiting is not used
     */
    public abstract Per<VoltageUnit, AngularAccelerationUnit> getExpoKa();

    /**
     * Converts this motion profile config into a CTRE/Phoenix-6 motion magic config
     *
     * @return a MotionMagicConfigs object with fields from this configuration
     */
    public MotionMagicConfigs asMotionMagicConfigs() {
        return new MotionMagicConfigs()
                .withMotionMagicCruiseVelocity(this.getMaxVelocity())
                .withMotionMagicAcceleration(this.getMaxAcceleration())
                .withMotionMagicJerk(this.getMaxJerk())
                .withMotionMagicExpo_kV(this.getExpoKv())
                .withMotionMagicExpo_kA(this.getExpoKa());
    }
}
