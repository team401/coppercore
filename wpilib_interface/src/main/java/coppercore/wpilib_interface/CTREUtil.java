package coppercore.wpilib_interface;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
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
     * Clone a TalonFXConfiguration by serializing it and then deserializing it into a new config.
     *
     * <p>This method probably incurs a massive performance cost and therefore should be used
     * sparingly.
     *
     * @param config The old config to clone.
     * @return A brand new config, with the old config serialized and deserialized into it.
     */
    public static TalonFXConfiguration cloneTalonFXConfig(TalonFXConfiguration config) {
        var newConfig = new TalonFXConfiguration();
        newConfig.deserialize(config.serialize());
        return newConfig;
    }
}
