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
 * MotorIO{TalonFX/SparkMax}PositionSim by implementing PositionSimAdapter.
 *
 * <p>It does this by converting an arm angle reported by the SingleJointedArmSim to a motor or
 * encoder position using ratios configured in the mechanism config.
 */
public class ArmSimAdapter implements PositionSimAdapter {
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
}
