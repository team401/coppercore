package coppercore.wpilib_interface.subsystems.motors.profile;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Velocity;

/**
 * A generic motion profile configuration
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
 *     required to maintain a certain velocity. Is in units of Output Unit / Rotations per Second.
 *     As a result, when supply voltage is fixed, a higher profile kV results in a lower profile
 *     velocity.
 * @param expoKa The kA for MotionMagicExpo/exponential motion profile. Represents the output
 *     required to apply a given acceleration. Is in units of Output Unit / (Rotations per Second /
 *     Second). As a result, when supply voltage is fixed, a higher profile kA results in a lower
 *     profile acceleration.
 */
public final class ImmutableMotionProfileConfig extends MotionProfileConfig {
    private final AngularVelocity maxVelocity;
    private final AngularAcceleration maxAcceleration;
    private final Velocity<AngularAccelerationUnit> maxJerk;
    private final double expoKv;
    private final double expoKa;

    /**
     * A generic, immutable motion profile configuration
     *
     * <p>For a mutable config, see {@link MutableMotionProfileConfig} or {@link
     * MotionProfileConfig#mutable}
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
    public ImmutableMotionProfileConfig(
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.maxJerk = maxJerk;
        this.expoKv = expoKv;
        this.expoKa = expoKa;
    }

    /**
     * Create a new config by copying another config.
     *
     * @param other The config to copy from.
     */
    public ImmutableMotionProfileConfig(MotionProfileConfig other) {
        this.maxVelocity = other.getMaxVelocity();
        this.maxAcceleration = other.getMaxAcceleration();
        this.maxJerk = other.getMaxJerk();
        this.expoKv = other.getExpoKv();
        this.expoKa = other.getExpoKa();
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
}
