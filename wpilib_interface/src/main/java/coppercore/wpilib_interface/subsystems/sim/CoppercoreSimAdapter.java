package coppercore.wpilib_interface.subsystems.sim;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * The CoppercoreSimAdapter interface provides the MotorIO{TalonFX/SparkMax}Sim classes with a
 * common interface to interact with DCMotorSim, ElevatorSim, and SingleJointedArmSim to avoid
 * having to duplicate the IO to interface with different simulated mechanisms.
 *
 * <p>This is necessary because the physics simulation classes that WPILib provides, while all
 * extending LinearSystemSim, don't provide any common interface for interacting with the simulation
 * that is consistent across the different types of mechanism. CoppercoreSimAdapter implementations
 * serve to "translate" between the individual interface of a WPILib physics simulation and the
 * universal interface expected by a coppercore sim IO.
 *
 * @see coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFXSim
 * @see edu.wpi.first.wpilibj.simulation.SingleJointedArmSim
 * @see edu.wpi.first.wpilibj.simulation.ElevatorSim
 */
public interface CoppercoreSimAdapter {
    /**
     * Get the current position of the motor, calculated by converting the physics sim's position to
     * a motor position.
     *
     * <p>This value will NOT account for encoder offsets. All offsets should be handled in the IO.
     *
     * @return An Angle representing the current position of the motor
     */
    public Angle getMotorPosition();

    /**
     * Get the current angular velocity of the motor, calculated by converting the physics sim's
     * angular velocity to a motor angular velocity.
     *
     * @return An AngularVelocity representing the current angular velocity of the motor
     */
    public AngularVelocity getMotorAngularVelocity();

    /**
     * Get the current position of the encoder, calculated by converting the physics sim's position
     * to an encoder position.
     *
     * <p>This value will NOT account for encoder offsets. All offsets should be handled in the IO.
     *
     * @return An Angle representing the current position of the encoder
     */
    public Angle getEncoderPosition();

    /**
     * Get the current angular velocity if the encoder, calculated by converting the physics sim's
     * angular velocity to an encoder angular velocity.
     *
     * @return An AngularVelocity representing the current angular velocity of the encoder
     */
    public AngularVelocity getEncoderAngularVelocity();

    /**
     * Get the current amount of current being drawn reported by the physics sim
     *
     * <p>This is the total current draw summed for all motors.
     *
     * @return A Current representing the total current draw reported by the sim after its last
     *     update
     */
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
