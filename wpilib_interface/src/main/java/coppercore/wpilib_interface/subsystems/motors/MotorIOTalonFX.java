package coppercore.wpilib_interface.subsystems.motors;

import static edu.wpi.first.units.Units.Rotations;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicExpoTorqueCurrentFOC;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.hardware.TalonFX;
import coppercore.wpilib_interface.CTREUtil;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import java.util.Optional;

/**
 * A base motor IO that implements closed-loop control for a TalonFX-supporting motor using
 * MotionMagicExpo, MotionMagicVelocity, and TorqueCurrentFOC wherever possible.
 */
public class MotorIOTalonFX implements MotorIO {
    private final MechanismConfig config;

    private final TalonFXConfiguration talonFXConfig;

    private final TalonFX talon;

    private final String deviceName;

    private final Alert configFailedToApplyAlert;

    private final StatusSignal<AngularVelocity> velocitySignal;
    private final StatusSignal<Angle> positionSignal;
    private final StatusSignal<Voltage> appliedVoltageSignal;
    private final StatusSignal<Current> statorCurrentSignal;
    private final StatusSignal<Current> supplyCurrentSignal;
    private final StatusSignal<Angle> rawRotorPositionSignal;

    private final BaseStatusSignal[] signals;

    private final PositionTorqueCurrentFOC unprofiledPositionRequest =
            new PositionTorqueCurrentFOC(Rotations.zero());
    private final MotionMagicExpoTorqueCurrentFOC profiledPositionRequest =
            new MotionMagicExpoTorqueCurrentFOC(Rotations.zero());

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An Optional containing either the index of the follower motor (what
     *     position in config.followerIds this motor is) or None if this is the lead motor.
     */
    public MotorIOTalonFX(MechanismConfig config, Optional<Integer> followerIndex) {
        this.config = config;

        int id =
                followerIndex.map((idx) -> config.followerMotorIds[idx]).orElse(config.leadMotorId);

        this.deviceName =
                new StringBuilder()
                        .append(config.name)
                        .append("_TalonFX_")
                        .append(config.canbus)
                        .append("_")
                        .append(id)
                        .toString();

        this.talonFXConfig = CTREUtil.cloneTalonFXConfig(config.motorConfig);

        this.talon = new TalonFX(id, config.canbus);

        this.velocitySignal = talon.getVelocity();
        this.positionSignal = talon.getPosition();
        this.appliedVoltageSignal = talon.getMotorVoltage();
        this.statorCurrentSignal = talon.getStatorCurrent();
        this.supplyCurrentSignal = talon.getSupplyCurrent();
        this.rawRotorPositionSignal = talon.getRotorPosition();

        this.signals =
                new BaseStatusSignal[] {
                    velocitySignal,
                    positionSignal,
                    appliedVoltageSignal,
                    statorCurrentSignal,
                    supplyCurrentSignal,
                    rawRotorPositionSignal,
                };

        String configFailedToApplyMessage =
                new StringBuilder()
                        .append(deviceName)
                        .append(" failed to apply configs.")
                        .toString();

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

        CTREUtil.tryUntilOk(
                () -> talon.getConfigurator().apply(talonFXConfig),
                id,
                (code) -> {
                    configFailedToApplyAlert.set(true);
                });

        CTREUtil.tryUntilOk(
                () -> BaseStatusSignal.setUpdateFrequencyForAll(50.0, signals), id, (code) -> {});
        CTREUtil.tryUntilOk(() -> talon.optimizeBusUtilization(), id, (code) -> {});
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        BaseStatusSignal.refreshAll(signals);

        inputs.velocity.mut_replace(velocitySignal.getValue());
        inputs.position.mut_replace(positionSignal.getValue());
        inputs.appliedVolts = appliedVoltageSignal.getValueAsDouble();
        inputs.statorCurrentAmps = statorCurrentSignal.getValueAsDouble();
        inputs.supplyCurrentAmps = supplyCurrentSignal.getValueAsDouble();
        inputs.rawRotorPosition.mut_replace(rawRotorPositionSignal.getValue());
    }

    @Override
    public void controlToPositionUnprofiled(Angle positionSetpoint) {
        talon.setControl(unprofiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToPositionProfiled(Angle positionSetpoint) {
        talon.setControl(profiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            PerUnit<AngularAccelerationUnit, TimeUnit> maxJerk,
            double expoKv,
            double expoKa) {
        // TODO: Figure out if it's possible to vary the profile in real-time with motion magic expo
        throw new UnsupportedOperationException(
                "controlToPositionProfiled does not yet support real-time modification of"
                        + " profile.");
    }
}
