package coppercore.wpilib_interface;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.motors.MotorIO.NeutralMode;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Utilities for working with CTRE hardware */
public final class CTREUtil {
    // Prevent instantiation
    private CTREUtil() {}

    private static final int defaultMaxTries = 10;

    /**
     * Run `function` until it returns an OK StatusCode, or until it has tried and failed 10 times.
     *
     * @param function A function returning a Phoenix6 StatusCode
     * @param deviceId The CAN ID of the related device for error logging
     * @param onFailure A function which will accept a StatusCode for additional action to be taken
     *     if tryUntilOk fails.
     * @return The StatusCode from the final call.
     */
    public static StatusCode tryUntilOk(
            Supplier<StatusCode> function, CANDeviceID deviceId, Consumer<StatusCode> onFailure) {
        return tryUntilOk(function, defaultMaxTries, deviceId, onFailure);
    }

    /**
     * Run `function` until it returns an OK StatusCode, or until it has tried and failed `maxTries`
     * times, at which point it will call `onFailure`.
     *
     * @param function A function returning a Phoenix6 StatusCode
     * @param maxTries The maximum number of times to call `function` before giving up.
     * @param deviceId The CAN ID of the related device for error logging
     * @param onFailure A function which will accept a StatusCode for additional action to be taken
     *     if tryUntilOk fails.
     * @return The StatusCode from the final call.
     */
    public static StatusCode tryUntilOk(
            Supplier<StatusCode> function,
            int maxTries,
            CANDeviceID deviceId,
            Consumer<StatusCode> onFailure) {
        StatusCode code = StatusCode.StatusCodeNotInitialized;

        for (int i = 0; i < maxTries; i++) {
            code = function.get();
            if (code.isOK()) {
                return code;
            }
        }

        DriverStation.reportError(
                "tryUntilOk failed after "
                        + maxTries
                        + " attempts (deviceId: "
                        + deviceId
                        + ") with status code"
                        + code,
                true);

        onFailure.accept(code);

        return code;
    }

    /**
     * Translate a vendor-agnostic NeutralMode into a Phoenix-6 NeutralModeValue for use with
     * TalonFX
     *
     * @param neutralMode The {@link
     *     coppercore.wpilib_interface.subsystems.motors.MotorIO.NeutralMode} to translate
     * @return A {@link com.ctre.phoenix6.signals.NeutralModeValue} representing the same value
     *     (Brake/Coast) as the original neutralMode
     */
    public static NeutralModeValue translateNeutralMode(NeutralMode neutralMode) {
        return switch (neutralMode) {
            case Coast -> NeutralModeValue.Coast;
            case Brake -> NeutralModeValue.Brake;
        };
    }

    /**
     * Convert a boolean representing whether or not to invert a follower motor into a Phoenix6
     * MotorAlignmentValue.
     *
     * @param invertFollower True if the follower motor should spin in the opposite direction as the
     *     leader, false if they should spin in the same direction.
     * @return Opposed if invertFollower is true, Aligned if invertFollower is false
     */
    public static MotorAlignmentValue translateFollowerInvert(boolean invertFollower) {
        return invertFollower ? MotorAlignmentValue.Opposed : MotorAlignmentValue.Aligned;
    }
}
