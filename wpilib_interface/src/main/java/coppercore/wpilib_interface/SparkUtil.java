package coppercore.wpilib_interface;

import com.revrobotics.REVLibError;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.motors.MotorIO.NeutralMode;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** Utilities for working with REV/Spark hardware */
public class SparkUtil {
    // Cannot be instantiated
    private SparkUtil() {}

    private static final int defaultMaxTries = 10;

    /**
     * Calls a supplier and processes a value from a spark only if the value is valid.
     *
     * @param spark The spark object to query for errors after running the supplier
     * @param supplier The supplier to get the value
     * @param consumer The consumer to use the value only if spark doesn't have an error
     * @return Is ok: True if there was no error, false if there was an error
     */
    public static boolean ifOk(SparkBase spark, DoubleSupplier supplier, DoubleConsumer consumer) {
        double value = supplier.getAsDouble();
        if (spark.getLastError() == REVLibError.kOk) {
            consumer.accept(value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Run `function` until it returns a kOk REVLibError, or until it has tried and failed 10 times.
     *
     * @param function A function returning a REVLibError
     * @param deviceId The CAN ID of the related device for error logging
     * @param onFailure A function which will accept a REVLibError for additional action to be taken
     *     if tryUntilOk fails.
     * @return the REVLibError from the final call.
     */
    public static REVLibError tryUntilOk(
            Supplier<REVLibError> function, CANDeviceID deviceId, Consumer<REVLibError> onFailure) {
        return tryUntilOk(function, defaultMaxTries, deviceId, onFailure);
    }

    /**
     * Run `function` until it returns a kOk REVLibError, or until it has tried and failed
     * `maxTries` times, at which point it will call `onFailure`
     *
     * @param function A function returning a REVLibError
     * @param maxTries The maximum number of times to call `function` before giving up.
     * @param deviceId The CAN ID of the related device for error logging
     * @param onFailure A function which will accept a REVLibError for additional action to be taken
     *     if tryUntilOk fails.
     * @return the REVLibError from the final call.
     */
    public static REVLibError tryUntilOk(
            Supplier<REVLibError> function,
            int maxTries,
            CANDeviceID deviceId,
            Consumer<REVLibError> onFailure) {
        REVLibError err = REVLibError.kUnknown;

        for (int i = 0; i < maxTries; i++) {
            err = function.get();
            if (err == REVLibError.kOk) {
                return err;
            }
        }

        DriverStation.reportError(
                "tryUntilOk failed after"
                        + maxTries
                        + " attempts (deviceId: "
                        + deviceId
                        + ") with error "
                        + err,
                true);

        onFailure.accept(err);

        return err;
    }

    /**
     * Translate a vendor-agnostic NeutralMode into a REVLib IdleMode for use with a
     * SparkMax/SparkFlex
     *
     * @param neutralMode The {@link
     *     coppercore.wpilib_interface.subsystems.motors.MotorIO.NeutralMode} to translate
     * @return A {@link com.revrobotics.spark.config.SparkBaseConfig.IdleMode} representing the same
     *     value (kBrake/kCoast) as the original neutralMode
     */
    public static IdleMode translateNeutralMode(NeutralMode neutralMode) {
        return switch (neutralMode) {
            case Coast -> IdleMode.kCoast;
            case Brake -> IdleMode.kBrake;
        };
    }
}
