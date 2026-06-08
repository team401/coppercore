package coppercore.wpilib_interface.subsystems.sim;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Volts;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

/**
 * The FlywheelSimAdapter class wraps a WPILib FlywheelSim for use with CopperCore motor IOs by
 * implementing the CoppercoreSimAdapter interface.
 *
 * <p>FlywheelSimAdapter also extends BaseSimAdapter to allow for its use with DummySimAdapters.
 *
 * <p>Because FlywheelSim doesn't provide a position, this adapter integrates the velocity and
 * acceleration in its update method to provide a position estimate.
 */
public class FlywheelSimAdapter extends BaseSimAdapter {
    protected final FlywheelSim physicsSim;
    MutAngle integratedPosition = Radians.mutable(0.0);

    /**
     * Creates an adapter around a WPILib flywheel simulation.
     *
     * @param config mechanism gearing and sensor configuration
     * @param flywheelSim simulation model to wrap
     */
    public FlywheelSimAdapter(MechanismConfig config, FlywheelSim flywheelSim) {
        super(config);
        this.physicsSim = flywheelSim;
    }

    /** {@inheritDoc} */
    @Override
    public void update(Voltage inputVoltage, double deltaTimeSeconds) {
        physicsSim.setInputVoltage(inputVoltage.in(Volts));
        physicsSim.update(deltaTimeSeconds);

        double dthetaRadians =
                physicsSim.getAngularVelocityRadPerSec() * deltaTimeSeconds
                        + 0.5
                                * physicsSim.getAngularAccelerationRadPerSecSq()
                                * deltaTimeSeconds
                                * deltaTimeSeconds;
        integratedPosition.mut_plus(Radians.of(dthetaRadians));
    }

    /** {@inheritDoc} */
    @Override
    public Angle getMotorPosition() {
        return getEncoderPosition().times(config.motorToEncoderRatio);
    }

    /** {@inheritDoc} */
    @Override
    public AngularVelocity getMotorAngularVelocity() {
        return getEncoderAngularVelocity().times(config.motorToEncoderRatio);
    }

    /** {@inheritDoc} */
    @Override
    public Angle getEncoderPosition() {
        return integratedPosition.times(config.encoderToMechanismRatio);
    }

    /** {@inheritDoc} */
    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        return physicsSim.getAngularVelocity().times(config.encoderToMechanismRatio);
    }

    /** {@inheritDoc} */
    @Override
    public Current getCurrentDraw() {
        return Amps.of(physicsSim.getCurrentDrawAmps());
    }

    /** {@inheritDoc} */
    @Override
    public void setState(Angle newAngle, AngularVelocity newAngularVelocity) {
        integratedPosition.mut_replace(newAngle);
        physicsSim.setAngularVelocity(newAngularVelocity.in(RadiansPerSecond));
    }
}
