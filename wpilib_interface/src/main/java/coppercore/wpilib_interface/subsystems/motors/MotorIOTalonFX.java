package coppercore.wpilib_interface.subsystems.motors;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import coppercore.wpilib_interface.CTREUtil;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.units.measure.Angle;
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
}
