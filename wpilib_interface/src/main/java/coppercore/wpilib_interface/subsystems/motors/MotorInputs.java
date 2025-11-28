package coppercore.wpilib_interface.subsystems.motors;

import org.littletonrobotics.junction.AutoLog;

/**
 * A generic set of inputs for a motor.
 *
 * <p>Contains doubles for velocity, position, raw rotor position, stator current, supply current,
 * and applied voltage.
 *
 * <p>This class doesn't use MutMeasures because they have caused issues in fromLog using replay
 * sim. Instead, all values are labelled with their units and stored in their base units to help
 * avoid confusion.
 */
@AutoLog
public class MotorInputs {
    /**
     * Tracks whether, the last time inputs were updated, all values successfully refreshed from the
     * motor controller. If any value fails to refresh, this likely indicates a disconnected motor
     * and connected will be set to false until the next update.
     *
     * <p>The methods updating this value do not handle any debouncing, nor do they refer to past
     * values to make a determination of connectivity. It is merely an indicator of the connectivity
     * state at the instant of the most recent update. To more accurately filter out momentary
     * issues, a debouncer is recommended before using this value to disable subsystems.
     */
    public boolean connected = false;

    /**
     * The current position of the motor as reported by the motor controller.
     *
     * <p>When using a TalonFX, this can be the position of a remote sensor and is affected by the
     * RotorToSensorRatio and SensorToMechanismRatio configs, as well as calls to
     * setCurrentPosition.
     */
    public double positionRadians = 0.0;

    /**
     * The current velocity of the motor as reported by the motor controller.
     *
     * <p>When using a TalonFX, this can be the velocity of a remote sensor and is affected by the
     * RotorToSensorRatio and SensorToMechanismRatio configs.
     */
    public double velocityRadiansPerSecond = 0.0;

    /** The current output voltage applied to the motor, as reported by the motor controller. */
    public double appliedVolts = 0.0;

    /**
     * The current corresponding to the stator windings, where positive indicates motoring
     * regardless of direction and negative indicates regenerative braking.
     *
     * <p>Not supported by MotorIOSparkMax.
     */
    public double statorCurrentAmps = 0.0;

    /** The measured supply side current. */
    public double supplyCurrentAmps = 0.0;

    /**
     * The rotor position of the motor. This is useful when using a TalonFX IO configured to use a
     * remote sensor, as positionRadians will return the position of the remote sensor.
     *
     * <p>This value is only affected by the RotorOffset config and calls to setPosition.
     *
     * <p>Not supported by MotorIOSparkMax.
     */
    public double rawRotorPositionRadians = 0.0;

    /**
     * The current "mechanism position" that the motor controller is targetting with closed loop
     * control. This value will not be updated if onboard motor closed-loop control isn't supported
     * or used.
     */
    public double closedLoopReference = 0.0;

    /**
     * The current "mechanism velocity" that the motor controller is targetting with closed loop
     * control. This value will not be updated if onboard motor closed-loop control isn't supported
     * or used.
     */
    public double closedLoopReferenceSlope = 0.0;
}
