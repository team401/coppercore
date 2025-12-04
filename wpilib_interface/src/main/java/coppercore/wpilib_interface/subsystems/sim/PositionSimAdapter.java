package coppercore.wpilib_interface.subsystems.sim;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * The PositionSimAdapter interface provides the MotorIO{TalonFX/SparkMax}PositionSim classes a
 * common interface to interact with ElevatorSim and SingleJointedArmSim to avoid having to
 * duplicate the IO to interface with elevators and arms.
 *
 * <p>Adapters implementing PositionSimAdapter should take in whatever parameters are required to
 * convert all values to be in terms of motor position and velocity.
 *
 * @see coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFXPositionSim
 * @see edu.wpi.first.wpilibj.simulation.SingleJointedArmSim
 * @see edu.wpi.first.wpilibj.simulation.ElevatorSim
 */
public interface PositionSimAdapter {
    /**
     * Get the current position of the motor, calculated by converting the physics sim's position to
     * a motor position.
     *
     * <p>This value will NOT account for encoder offsets. All offsets should be handled in the IO.
     */
    public Angle getMotorPosition();

    /**
     * Get the current angular velocity of the motor, calculated by converting the physics sim's
     * angular velocity to a motor angular velocity.
     */
    public AngularVelocity getMotorAngularVelocity();

    /**
     * Get the current position of the encoder, calculated by converting the physics sim's position
     * to an encoder position.
     *
     * <p>This value will NOT account for encoder offsets. All offsets should be handled in the IO.
     */
    public Angle getEncoderPosition();

    /**
     * Get the current angular velocity if the encoder, calculated by converting the physics sim's
     * angular velocity to an encoder angular velocity.
     */
    public AngularVelocity getEncoderAngularVelocity();

    /** Get the current amount of current being drawn reported by the physics sim */
    public Current getCurrentDraw();

    /**
     * Update the physics sim based on motor voltage and the period of time that has elapsed.
     *
     * @param motorAppliedOutput The current applied voltage reported by the motor sim state as
     *     output.
     * @param deltaTimeSeconds The amount of time that has elapsed since the method was last called,
     *     measured in seconds.
     */
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds);
}
