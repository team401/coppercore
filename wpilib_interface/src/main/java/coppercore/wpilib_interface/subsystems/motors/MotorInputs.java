package coppercore.wpilib_interface.subsystems.motors;

import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import org.littletonrobotics.junction.AutoLog;

/**
 * A generic set of inputs for a motor.
 *
 * <p>Contains measures for velocity, position, and raw rotor position, as well as double values for
 * applied voltage and supply and stator current.
 */
@AutoLog
public class MotorInputs {
    public boolean connected = false;
    public MutAngularVelocity velocity = RotationsPerSecond.mutable(0.0);
    public MutAngle position = Rotations.mutable(0.0);
    public double appliedVolts = 0.0;
    public double statorCurrentAmps = 0.0;
    public double supplyCurrentAmps = 0.0;
    public MutAngle rawRotorPosition = Rotations.mutable(0.0);
}
