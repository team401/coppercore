package coppercore.wpilib_interface.subsystems.motors;

import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;

/**
 * Base class containing shared functionality for CAN-bus attached motor controllers, such as
 * TalonFX or SparkMax.
 */
public class CanBusMotorControllerBase {
    /**
     * MechanismConfig describing the whole mechanism; can be shared between IOs within mechanism
     */
    protected final MechanismConfig config;

    /** id encapsulating CAN ID and CAN bus name */
    protected final CANDeviceID id;

    /** A unique string for this device used for logging when something goes awry */
    protected final String deviceName;

    /** An alert to be shown whenever a config fails to apply to the motor controller */
    protected final Alert configFailedToApplyAlert;

    /**
     * An alert to be shown whenever any status signal fails to refresh, which indicates a
     * disconnected state
     */
    protected final Alert disconnectedAlert;

    /**
     * Create a new CanBusMotorControllerBase given a mechanism config and a CANDeviceID.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param id The CANDeviceID of the motor in question.
     * @param nameInfix A string to be placed between name and id, such as _TalonFX_.
     */
    protected CanBusMotorControllerBase(MechanismConfig config, CANDeviceID id, String nameInfix) {
        this.id = id;

        this.config = config;

        this.deviceName = config.name + nameInfix + id;

        String configFailedToApplyMessage = deviceName + " failed to apply configs.";

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

        String disconnectedMessage = deviceName + " disconnected/invalid status code.";

        this.disconnectedAlert = new Alert(disconnectedMessage, AlertType.kError);
    }
}
