package coppercore.wpilib_interface.subsystems.motors;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Rotations;

import coppercore.wpilib_interface.subsystems.motors.profile.MotionProfileConfig;
import coppercore.wpilib_interface.subsystems.motors.profile.MutableMotionProfileConfig;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.units.measure.Voltage;

/**
 * A generic motor IO. Contains methods to update inputs, control to positions, and specify
 * closed-loop gains.
 */
public interface MotorIO {
    /**
     * A vendor-agnostic value to represent neutral-mode behavior.
     *
     * <p>This determines whether the motor will spin freely (coast) or resist movement (brake) when
     * 0 output is applied.
     *
     * <p>Docs for this can be found here:
     *
     * <ul>
     *   <li>For Spark:
     *       https://docs.revrobotics.com/brushless/spark-flex/spark-flex-feature-description/operating-modes
     *   <li>For TalonFX:
     *       https://api.ctr-electronics.com/phoenix6/release/java/com/ctre/phoenix6/configs/MotorOutputConfigs.html#NeutralMode
     * </ul>
     */
    public enum NeutralMode {
        /**
         * Effectively connects motor wires together when 0 output is applied so that the motor
         * provides "back-EMF" that will rapidly slow down the motor and resist movement.
         */
        Brake,
        /** Disconnects motor wires when 0 output is applied so that the motor will spin freely. */
        Coast
    }

    /**
     * A type-safe selector for the closed-loop gain slot to configure or activate.
     *
     * <p>Motor controllers such as TalonFX and SparkMax support multiple independent sets of PID
     * and feedforward gains, each stored in a numbered slot. Use {@link MotorIO#setGains(GainSlot,
     * double, double, double, double, double, double, double)} to write gains to a slot and {@link
     * MotorIO#selectGainSlot(GainSlot)} to make that slot active.
     */
    public enum GainSlot {
        /** Gain slot 0 (the default). */
        Slot0,
        /** Gain slot 1. */
        Slot1,
        /** Gain slot 2. */
        Slot2
    }

    /**
     * Take a set of inputs and update them with the latest data from the motor controller
     *
     * @param inputs The inputs object, which will be mutated.
     */
    public void updateInputs(MotorInputs inputs);

    /** Command a zero or "neutral" output. If in brake mode, brake. If in coast mode, coast. */
    public void controlNeutral();

    /** Command a zero/neutral output by allowing the motor to coast (spin freely). */
    public void controlCoast();

    /** Command a zero/neutral output while telling the motor to brake (resist movement). */
    public void controlBrake();

    /**
     * Control the motor to a certain position setpoint without using a motion profile
     *
     * @param positionSetpoint The new position to control the mechanism to
     */
    public void controlToPositionUnprofiled(Angle positionSetpoint);

    /**
     * Control the motor to a certain position setpoint using closed-loop control and a motion
     * profile
     *
     * <p>To use this overload, setProfileConstraints must have been called with the correct profile
     * constraints
     *
     * @param positionSetpoint The position to control the motor to
     */
    public void controlToPositionProfiled(Angle positionSetpoint);

    /**
     * Control the motor to a certain position setpoint using closed-loop control and a motion
     * profile
     *
     * @see MutableMotionProfileConfig
     * @param positionSetpoint The setpoint to control the motor to
     * @param maxVelocity The max profile velocity. Use 0 for uncapped.
     * @param maxAcceleration The max profile acceleration. Use 0 for uncapped.
     * @param maxJerk The max profile jerk (change in acceleration over time). Use 0 for uncapped.
     * @param expoKv The Kv for MotionMagicExpo (or other exponential profile).
     * @param expoKa The Ka for MotionMagicExpo (or other exponential profile).
     */
    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa);

    /**
     * Control the motor to a certain position setpoint using closed-loop control and a motion
     * profile
     *
     * @param positionSetpoint The setpoint to control the motor to
     * @param profileConfig The motion profile config to control motion profile constraints.
     */
    public void controlToPositionProfiled(
            Angle positionSetpoint, MotionProfileConfig profileConfig);

    /**
     * Control the motor to a certain position setpoint using closed-loop control and an exponential
     * motion profile. The profile must have been configured, either in the motor config or using
     * {@link setProfileConstraints}.
     *
     * @param positionSetpoint The position to control the motor to The position to control the
     *     motor to.
     */
    public void controlToPositionExpoProfiled(Angle positionSetpoint);

    /**
     * Control the motor to a certain velocity using closed-loop control without a motion profile
     *
     * @param velocitySetpoint The velocity to control the motor to
     */
    public void controlToVelocityUnprofiled(AngularVelocity velocitySetpoint);

    /**
     * Control the motor to a certain velocity using closed-loop control with a motion profile. The
     * motion profile must have been configured using the motor config or with
     * setProfileConstraints.
     *
     * @param velocitySetpoint The velocity to control the motor to
     */
    public void controlToVelocityProfiled(AngularVelocity velocitySetpoint);

    /**
     * Control the motor by applying a certain Voltage
     *
     * @param voltage The voltage to apply to the motor
     */
    public void controlOpenLoopVoltage(Voltage voltage);

    /**
     * Control the motor by applying a certain current with FOC
     *
     * @param current The current to apply to the motor
     */
    public void controlOpenLoopCurrent(Current current);

    /**
     * Control the motor by following another motor
     *
     * <p>The follower (this motor) must be on the same CAN bus as the leader motor
     *
     * <p>When using a SparkMax, this requires reconfiguring the motor controller and will take
     * significant time. Therefore, it is not recommended to call this method in periodic when using
     * a SparkMax.
     *
     * @param leaderId The leader motor ID
     * @param opposeLeaderDirection True if the motor should spin in the opposite direction as its
     *     leader, false if it should spin in the same direction
     */
    public void follow(int leaderId, boolean opposeLeaderDirection);

    /**
     * Set the default motion profile constraints that will be used for closed-loop control.
     *
     * <p>This method should be assumed to be blocking, and may take significant time to finish. If
     * periodic changes to profile constraints are required, use the various overloads of
     * controlToPositionProfiled to provide constraints.
     *
     * <p>For example, for TalonFX, this will update the motion magic configuration and apply it to
     * the motor.
     *
     * @param profileConfig The motion profile configuration to use.
     */
    public void setProfileConstraints(MotionProfileConfig profileConfig);

    /**
     * Set the closed-loop control gains used by the motor in the specified gain slot.
     *
     * <p>This method should be assumed to be blocking, and may take significant time to finish. If
     * periodic changes to gains are required, use a PID controller running in robot code.
     *
     * <p>For example, applying a config to a TalonFX is a blocking operation:
     * https://v6.docs.ctr-electronics.com/en/latest/docs/api-reference/api-usage/configuration.html#applying-configs
     *
     * <p>Units listed are for position control. When using velocity control, error will be in
     * rotations/second instead.
     *
     * <p>For Spark IOs, the usage of kS, kG, or kA in separate slots is implemented by keeping feed
     * forward gains in memory and reapplying the configuration for the respective active slot.
     * (Which is blocking.)
     *
     * <p>Use {@link #selectGainSlot(GainSlot)} to activate a previously configured slot.
     *
     * @param slot The gain slot to configure.
     * @param kP Proportional gain. Unit is output units / rotation of error.
     * @param kI Integral gain. Unit is output units / (rotation of error * seconds).
     * @param kD Derivative gain. Unit is output units / (rotation per second)
     * @param kS Static feed-forward gain. Unit is output units.
     * @param kG Gravity feed-forward gain. Unit is output units.
     * @param kV Velocity feed-forward gain. Unit is output units / requested input velocity.
     * @param kA Accelerated feed-forward. Unit is output units / requested input acceleration.
     */
    public void setGains(
            GainSlot slot,
            double kP,
            double kI,
            double kD,
            double kS,
            double kG,
            double kV,
            double kA);

    /**
     * Set the closed-loop control gains used by the motor in {@link GainSlot#Slot0}.
     *
     * <p>This is equivalent to calling {@link #setGains(GainSlot, double, double, double, double,
     * double, double, double)} with {@code slot = GainSlot.Slot0}.
     *
     * @param kP Proportional gain. Unit is output units / rotation of error.
     * @param kI Integral gain. Unit is output units / (rotation of error * seconds).
     * @param kD Derivative gain. Unit is output units / (rotation per second)
     * @param kS Static feed-forward gain. Unit is output units.
     * @param kG Gravity feed-forward gain. Unit is output units.
     * @param kV Velocity feed-forward gain. Unit is output units / requested input velocity.
     * @param kA Accelerated feed-forward. Unit is output units / requested input acceleration.
     */
    public default void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {
        setGains(GainSlot.Slot0, kP, kI, kD, kS, kG, kV, kA);
    }

    /**
     * Activate a previously configured gain slot for use in closed-loop control requests.
     *
     * <p>After calling this method, all subsequent closed-loop control requests will use the gains
     * stored in the specified slot. Gains must have been written to the slot previously using
     * {@link #setGains(GainSlot, double, double, double, double, double, double, double)}.
     *
     * @param slot The gain slot to activate.
     */
    public void selectGainSlot(GainSlot slot);

    /**
     * Sets the arbitrary feedforward used by this motor IO in all closed loop requests that support
     * an arbitrary feedforward. After calling this method with a value, all closed-loop requests
     * will be applied with this extra feedforward added until it is called with a different value,
     * or until {@link #clearArbitraryFeedForward()} is called.
     *
     * <p>This value is a Current because it is only supported by MotorIOSparkMax and its
     * subclasses, which use TorqueCurrentFOC for closed-loop control.
     *
     * <p>Not supported by Spark IOs.
     *
     * @param feedForward A Current representing the arbitrary feedforward to add to the output of
     *     the closed-loop controller before the desired output is applied to the motor.
     */
    public void setArbitraryFeedForward(Current feedForward);

    /**
     * Clears the arbitrary feedforward used by this motor IO. This is equivalent to calling {@link
     * #setArbitraryFeedForward(Current)} with a value of zero amps.
     *
     * <p>Not supported by spark IOs.
     */
    public default void clearArbitraryFeedForward() {
        setArbitraryFeedForward(Amps.zero());
    }

    /**
     * Set whether the motor should brake or coast when a neutral (0) output is commanded.
     *
     * @param neutralMode A NeutralMode, either Brake to resist movement when neutral or Coast to
     *     spin freely when neutral.
     */
    public void setNeutralMode(NeutralMode neutralMode);

    /**
     * Set the current position of the system in terms of the motor's encoder.
     *
     * <p>This does NOT move the system. It tells the motor encoder that it is now at `position`,
     * updating its offset accordingly.
     *
     * @param position The position to set as the current encoder position.
     */
    public void setCurrentPosition(Angle position);

    /**
     * Set the current position of the system in terms of the motor's encoder to zero.
     *
     * <p>This does NOT move the system. It tells the motor encoder that it is now at zero.
     */
    public default void setCurrentPositionAsZero() {
        setCurrentPosition(Rotations.zero());
    }

    /**
     * Set the requested update frequency for control requests to the motor.
     *
     * <p>By default, the default update frequency for each request is used. However, once this
     * method is called, all requests will attempt to use this frequency instead.
     *
     * <p>This method is not supported by spark IOs.
     *
     * <p>Some update frequencies are not supported and will be promoted up to the next highest
     * supported frequency.
     *
     * <p>If this value is set to 0 Hz, the control request will be sent immediately as a one-shot
     * frame. This may be useful for advanced applications that require outputs to be synchronized
     * with data acquisition. In this case, we recommend not exceeding 50 ms between control calls.
     *
     * @param updateFrequency A Frequency measure specifying the desired update frequency of
     *     requests. For MotionMagicExpoTorqueCurrentFOC requests, the default is 100hz, while for
     *     requests like Follower, the default is 20hz.
     */
    public void setRequestUpdateFrequency(Frequency updateFrequency);
}
