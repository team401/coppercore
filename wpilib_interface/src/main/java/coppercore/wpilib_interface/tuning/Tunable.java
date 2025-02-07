package coppercore.wpilib_interface.tuning;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * An interface to allow a subsystem to be tuned in a standardized way. Implementing Tunable allows
 * the subsystem to use TuneG, TuneS, and TuneV.
 */
public interface Tunable {
    /**
     * Get the position of a mechanism
     *
     * @param slot The slot of the mechanism to get the position of
     * @return An Angle, the current position of the system
     */
    public Angle getPosition(int slot);

    /**
     * Get the velocity of a mechanism
     *
     * @param slot The slot of the mechanism to get the velocity of
     * @return An AngularVelocity, the current velocity of the system
     */
    public AngularVelocity getVelocity(int slot);

    /**
     * Sets the output applied for a certain slot
     *
     * <p>This is referred to as 'output' since it can be a voltage, or a current for FOC
     *
     * @param output The output, in Volts for voltage control or Amps for FOC
     * @param slot The slot of the mechanism to set the output of
     */
    public void setOutput(double output, int slot);

    /**
     * Set the PID gains for the mechanism in a given slot
     *
     * @param p Proportional gain
     * @param i Integral gain
     * @param d Derivative gain
     * @param slot The slot of the mechanism to set the gains of
     */
    public void setPID(double p, double i, double d, int slot);

    /**
     * Set the feedforward gains for the mechanism in a given slot
     *
     * @param kS Output to overcome friction
     * @param kV Output per unit of velocity
     * @param kA Output per unit of acceleration
     * @param kG Output to overcome gravity
     * @param slot The slot of the mechanism to set the gains of
     */
    public void setFF(double kS, double kV, double kA, double kG, int slot);

    /**
     * Set the maximum profile properties of the mechanism in the given slot
     *
     * <p>When using MotionMagicExpo, these numbers will be converted to an Expo_kA and Expo_kV: <a>
     * https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/device-specific/talonfx/motion-magic.html#motion-magic-expo</a>
     *
     * <p><code>[Supply voltage] / Expo_kA = max acceleration</code>
     *
     * <p>Therefore: <code> [Supply voltage] / maxAcceleration = Expo_kA</code>
     *
     * @param maxVelocity The maximum allowed velocity of the system
     * @param maxAcceleration The maximum allowed acceleration of the system
     * @param slot The slot of the mechanism to apply the profile to
     */
    public void setMaxProfileProperties(
            AngularVelocity maxVelocity, AngularAcceleration maxAcceleration, int slot);

    /**
     * Run the mechanism to a position using closed-loop control
     *
     * @param position The goal Angle to drive the system to
     * @param slot The slot of the mechanism to set the goal Angle of
     */
    public void runToPosition(Angle position, int slot);
}
