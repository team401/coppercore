package coppercore.wpilib_interface.subsystems.sim;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Volts;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;

/**
 * The ElevatorSimAdapter class adapts the wpilib SingleJointedArmSim for use with coppercore's
 * MotorIO{TalonFX/SparkMax}Sim by implementing CoppercoreSimAdapter.
 *
 * <p>It does this by converting an arm angle reported by the SingleJointedArmSim to a motor or
 * encoder position using ratios configured in the mechanism config.
 */
public class ArmSimAdapter implements CoppercoreSimAdapter {
    private final MechanismConfig config;
    private final SingleJointedArmSim armSim;

    /**
     * Create a new ArmSimAdapter to adapt a given SingleJointedArmSim using parameters specified in
     * a given MechanismConfig.
     *
     * @param config The MechanismConfig, used to find ratios of encoder to mechanism and motor to
     *     encoder.
     * @param armSim The SingleJointedArmSim, whose position and velocity will be used to update
     *     inputs in sim.
     */
    public ArmSimAdapter(MechanismConfig config, SingleJointedArmSim armSim) {
        this.config = config;
        this.armSim = armSim;
    }

    @Override
    public Angle getMotorPosition() {
        return getEncoderPosition().times(config.motorToEncoderRatio);
    }

    @Override
    public AngularVelocity getMotorAngularVelocity() {
        return getEncoderAngularVelocity().times(config.motorToEncoderRatio);
    }

    @Override
    public Angle getEncoderPosition() {
        Angle mechanismPos = Radians.of(armSim.getAngleRads());
        Angle encoderPos = mechanismPos.times(config.encoderToMechanismRatio);

        return encoderPos;
    }

    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        double mechanismVelRadPerSec = armSim.getVelocityRadPerSec();
        double encoderVelRadPerSec = mechanismVelRadPerSec * config.encoderToMechanismRatio;

        return RadiansPerSecond.of(encoderVelRadPerSec);
    }

    @Override
    public Current getCurrentDraw() {
        return Amps.of(armSim.getCurrentDrawAmps());
    }

    @Override
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds) {
        armSim.setInput(motorAppliedOutput.in(Volts));
        armSim.update(deltaTimeSeconds);
    }

    /**
     * Manually sets the physics sim's position and velocity. This is intended to be used by the
     * DummySimAdapter to mock different positions and observe how a physics sim responds.
     *
     * <p>This method must NOT be called by normal sim code unless you know what you are doing. It
     * will instantly set the position of a mechanism with no regard for physical limitations.
     *
     * @param motorAngle A double representing the position to set, in radians
     * @param motorVelocity A double representing the velocity to set, in radians per second
     */
    protected void setState(Angle motorAngle, AngularVelocity motorVelocity) {
        armSim.setState(motorAngle.in(Radians), motorVelocity.in(RadiansPerSecond));
    }
}
