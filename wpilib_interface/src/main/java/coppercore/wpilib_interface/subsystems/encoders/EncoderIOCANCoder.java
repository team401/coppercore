package coppercore.wpilib_interface.subsystems.encoders;

import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import coppercore.wpilib_interface.CTREUtil;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * The EncoderIOCANCoder class implements the EncoderIO interface for a physical CANCoder device by
 * leveraging Phoenix-6.
 */
public class EncoderIOCANCoder implements EncoderIO {
    protected final String deviceName;

    protected final CANDeviceID deviceID;

    protected final CANcoder cancoder;

    /** An alert to be shown whenever a config fails to apply */
    protected final Alert configFailedToApplyAlert;

    /**
     * An alert to be shown whenever any status signal fails to refresh, which indicates a
     * disconnected state
     */
    protected final Alert disconnectedAlert;

    /** Position StatusSignal cached for easy repeated access */
    protected final StatusSignal<Angle> positionSignal;

    /** Absolute Position StatusSignal cached for easy repeated access */
    protected final StatusSignal<Angle> absolutePositionSignal;

    /** Velocity StatusSignal cached for easy repeated access */
    protected final StatusSignal<AngularVelocity> velocitySignal;

    /** An array of status signals ot be easily passed to refreshAll */
    protected final BaseStatusSignal[] signals;

    public EncoderIOCANCoder(CANDeviceID id, CANcoderConfiguration cancoderConfig) {
        this.deviceID = id;

        this.deviceName = "CANcoder_" + id;

        this.cancoder = new CANcoder(id.id(), id.canbus());

        String configFailedToApplyMessage = deviceName + " failed to apply configs.";

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

        String disconnectedMessage = deviceName + " disconnected/invalid status code.";

        this.disconnectedAlert = new Alert(disconnectedMessage, AlertType.kError);

        CTREUtil.tryUntilOk(
                () -> cancoder.getConfigurator().apply(cancoderConfig),
                id,
                (code) -> configFailedToApplyAlert.set(true));

        this.positionSignal = cancoder.getPosition();
        this.absolutePositionSignal = cancoder.getAbsolutePosition();
        this.velocitySignal = cancoder.getVelocity();

        this.signals =
                new BaseStatusSignal[] {
                    positionSignal, absolutePositionSignal, velocitySignal,
                };

        CTREUtil.tryUntilOk(
                () -> BaseStatusSignal.setUpdateFrequencyForAll(50.0, signals), id, (code) -> {});
        CTREUtil.tryUntilOk(() -> cancoder.optimizeBusUtilization(), id, (code) -> {});
    }

    @Override
    public void updateInputs(EncoderInputs inputs) {
        StatusCode code = BaseStatusSignal.refreshAll(signals);

        inputs.connected = code.isOK();

        disconnectedAlert.set(!code.isOK());

        if (code.isError()) {
            DriverStation.reportError(
                    deviceName + ": Failed to refresh status signals: " + code, false);
        } else if (code.isWarning()) {

            DriverStation.reportError(
                    deviceName + ": Warning while refreshing status signals: " + code, false);
        }

        inputs.positionRadians = positionSignal.getValue().in(Radians);
        inputs.absolutePositionRadians = absolutePositionSignal.getValue().in(Radians);
        inputs.velocityRadiansPerSecond = velocitySignal.getValue().in(RadiansPerSecond);
    }

    @Override
    public void setCurrentPosition(Angle position) {
        cancoder.setPosition(position);
    }
}
