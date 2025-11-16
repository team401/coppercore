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
 * convert all values to be in terms of motor position & velocity.
 *
 * @see coppercore.wpilib_interface.motors.talonfx.MotorIOTalonFXPositionSim
 * @see edu.wpi.first.wpilibj.simulation.SingleJointedArmSim
 * @see edu.wpi.first.wpilibj.simulation.ElevatorSim
 */
public interface PositionSimAdapter {
    /**
     * Get the current position of the motor, calculated by converting the physics sim's position to
     * a motor position.
     */
    public Angle getMotorPosition();

    /**
     * Get the current position of the motor, calculated by converting the physics sim's angular
     * velocity to a motor angular velocity.
     */
    public AngularVelocity getMotorAngularVelocity();

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
