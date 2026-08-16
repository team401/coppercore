package coppercore.wpilib_interface.subsystems.sim;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Volts;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;
import org.wpilib.wpilibj.simulation.DCMotorSim;

/**
 * The DCMotorSimAdapter class wraps a DCMotorSim to implement the CoppercoreSimAdapter interface,
 * allowing DCMotorSim to be used with Coppercore motor IOs in simulation.
 */
public class DCMotorSimAdapter extends BaseSimAdapter {
    protected final DCMotorSim dcMotorSim;

    public DCMotorSimAdapter(MechanismConfig config, DCMotorSim dcMotorSim) {
        super(config);

        this.dcMotorSim = dcMotorSim;
    }

    @Override
    public Angle getMotorPosition() {
        return dcMotorSim.getAngularPosition();
    }

    @Override
    public AngularVelocity getMotorAngularVelocity() {
        return dcMotorSim.getAngularVelocity();
    }

    @Override
    public Angle getEncoderPosition() {
        return dcMotorSim.getAngularPosition().div(config.motorToEncoderRatio);
    }

    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        return dcMotorSim.getAngularVelocity().div(config.motorToEncoderRatio);
    }

    @Override
    public Current getCurrentDraw() {
        return Amps.of(dcMotorSim.getCurrentDrawAmps());
    }

    @Override
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds) {
        dcMotorSim.setInput(motorAppliedOutput.in(Volts));
        dcMotorSim.update(deltaTimeSeconds);
    }

    @Override
    protected void setState(Angle motorAngle, AngularVelocity motorVelocity) {
        dcMotorSim.setState(motorAngle.in(Radians), motorVelocity.in(RadiansPerSecond));
    }
}
