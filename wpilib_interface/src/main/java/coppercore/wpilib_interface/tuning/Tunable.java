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
     * @return An Angle, the current position of the system
     */
    public Angle getPosition();

    /**
     * Get the velocity of a mechanism
     *
     * @return An AngularVelocity, the current velocity of the system
     */
    public AngularVelocity getVelocity();

    /**
     * Sets the output applied for the mechanism
     *
     * <p>This is referred to as 'output' since it can be a voltage, or a current for FOC
     *
     * @param output The output, in Volts for voltage control or Amps for FOC
     */
    public void setOutput(double output);

    /**
     * Set the PID gains for the mechanism's closed-loop controller
     *
     * @param p Proportional gain
     * @param i Integral gain
     * @param d Derivative gain
     */
    public void setPID(double p, double i, double d);

    /**
     * Set the feedforward gains for the mechanism's closed-loop control
     *
     * @param kS Output to overcome friction
     * @param kV Output per unit of velocity
     * @param kA Output per unit of acceleration
     * @param kG Output to overcome gravity
     */
    public void setFF(double kS, double kV, double kA, double kG);

    /**
     * Set the maximum profile properties of the mechanism for closed-loop control
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
     */
    public void setMaxProfileProperties(
            AngularVelocity maxVelocity, AngularAcceleration maxAcceleration);

    /**
     * Run the mechanism to a position using closed-loop control
     *
     * @param position The goal Angle to drive the system to
     */
    public void runToPosition(Angle position);
}
