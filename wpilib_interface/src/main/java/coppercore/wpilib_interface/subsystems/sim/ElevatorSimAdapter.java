package coppercore.wpilib_interface.subsystems.sim;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Volts;

import coppercore.wpilib_interface.subsystems.configs.ElevatorMechanismConfig;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;

/**
 * The ElevatorSimAdapter class adapts wpilib ElevatorSim for use with
 * MotorIO{TalonFX/SparkMax}PositionSim by implementing PositionSimAdapter.
 *
 * <p>It does this by converting an elevator position yielded by the ElevatorSim to a motor angle
 * using the elevatorToMotorRatio in the ElevatorMechanismConfig. It does the same for velocities.
 *
 * @see coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFXPositionSim
 * @see coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter
 */
public class ElevatorSimAdapter implements PositionSimAdapter {
    private final ElevatorMechanismConfig config;
    private final ElevatorSim elevatorSim;

    /**
     * Create a new ElevatorSimAdapter to adapt a given ElevatorSim for use according to parameters
     * specified in an ElevatorMechanismConfig.
     *
     * @param config The ElevatorMechanismConfig, which will be used to get the elevator to motor
     *     ratio.
     * @param elevatorSim The ElevatorSim, whose position and velocity will be read to update inputs
     *     in sim.
     */
    public ElevatorSimAdapter(ElevatorMechanismConfig config, ElevatorSim elevatorSim) {
        this.config = config;
        this.elevatorSim = elevatorSim;
    }

    @Override
    public Angle getMotorPosition() {
        Angle mechanismPos =
                Meters.of(elevatorSim.getPositionMeters())
                        .timesConversionFactor(config.elevatorToMechanismRatio.reciprocal());
        Angle encoderPos = mechanismPos.times(config.encoderToMechanismRatio);
        Angle motorPos = encoderPos.times(config.motorToEncoderRatio);

        return motorPos;
    }

    @Override
    public AngularVelocity getMotorAngularVelocity() {
        double velocityMetersPerSecond = elevatorSim.getVelocityMetersPerSecond();
        double metersPerRotation = config.elevatorToMechanismRatio.in(Meters.per(Rotations));

        double mechanismVelRotationsPerSecond = velocityMetersPerSecond / metersPerRotation;
        double encoderVelRotationsPerSecond =
                mechanismVelRotationsPerSecond * config.encoderToMechanismRatio;
        double motorVelRotationsPerSecond =
                encoderVelRotationsPerSecond * config.motorToEncoderRatio;

        return RotationsPerSecond.of(motorVelRotationsPerSecond);
    }

    @Override
    public Angle getEncoderPosition() {
        Angle mechanismPos =
                Meters.of(elevatorSim.getPositionMeters())
                        .timesConversionFactor(config.elevatorToMechanismRatio.reciprocal());
        Angle encoderPos = mechanismPos.times(config.encoderToMechanismRatio);

        return encoderPos;
    }

    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        double velocityMetersPerSecond = elevatorSim.getVelocityMetersPerSecond();
        double metersPerRotation = config.elevatorToMechanismRatio.in(Meters.per(Rotations));

        double mechanismVelRotationsPerSecond = velocityMetersPerSecond / metersPerRotation;
        double encoderVelRotationsPerSecond =
                mechanismVelRotationsPerSecond * config.encoderToMechanismRatio;

        return RotationsPerSecond.of(encoderVelRotationsPerSecond);
    }

    @Override
    public Current getCurrentDraw() {
        return Amps.of(elevatorSim.getCurrentDrawAmps());
    }

    @Override
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds) {
        elevatorSim.setInput(motorAppliedOutput.in(Volts));
        elevatorSim.update(deltaTimeSeconds);
    }
}
