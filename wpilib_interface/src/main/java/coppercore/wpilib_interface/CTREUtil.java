package coppercore.wpilib_interface;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.S1CloseStateValue;
import com.ctre.phoenix6.signals.S1StateValue;
import com.ctre.phoenix6.signals.S2CloseStateValue;
import com.ctre.phoenix6.signals.S2StateValue;
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

    /**
     * The DigitalSignalCloseState enum provides a common type for CTRE's signal-specific
     * close-state values. This is used to allow the simulated CANdi IO to correctly mock the value
     * of the <a
     * href="https://api.ctr-electronics.com/phoenix6/stable/java/com/ctre/phoenix6/hardware/core/CoreCANdi.html#getS1Closed()">S1Closed</a>
     * or <a
     * href="https://api.ctr-electronics.com/phoenix6/stable/java/com/ctre/phoenix6/hardware/core/CoreCANdi.html#getS2Closed()">S2Closed</a>
     * signals without a ton of code duplication.
     *
     * <p>This exists because Phoenix-6 has separate types for <a
     * href="https://api.ctr-electronics.com/phoenix6/stable/java/com/ctre/phoenix6/signals/S1CloseStateValue.html">S1CloseStateValue</a>
     * and <a
     * href="https://api.ctr-electronics.com/phoenix6/stable/java/com/ctre/phoenix6/signals/S2CloseStateValue.html">S2CloseStateValue</a>.
     */
    public enum DigitalSignalCloseState {
        CloseWhenLow,
        CloseWhenFloating,
        CloseWhenHigh,
        CloseWhenNotLow,
        CloseWhenNotFloating,
        CloseWhenNotHigh;

        public static DigitalSignalCloseState from(S1CloseStateValue s1CloseStateValue) {
            return switch (s1CloseStateValue) {
                case CloseWhenLow -> CloseWhenLow;
                case CloseWhenFloating -> CloseWhenFloating;
                case CloseWhenHigh -> CloseWhenHigh;
                case CloseWhenNotLow -> CloseWhenNotLow;
                case CloseWhenNotFloating -> CloseWhenNotFloating;
                case CloseWhenNotHigh -> CloseWhenNotHigh;
            };
        }

        public static DigitalSignalCloseState from(S2CloseStateValue s2CloseStateValue) {
            return switch (s2CloseStateValue) {
                case CloseWhenLow -> CloseWhenLow;
                case CloseWhenFloating -> CloseWhenFloating;
                case CloseWhenHigh -> CloseWhenHigh;
                case CloseWhenNotLow -> CloseWhenNotLow;
                case CloseWhenNotFloating -> CloseWhenNotFloating;
                case CloseWhenNotHigh -> CloseWhenNotHigh;
            };
        }

        public S1StateValue getS1Closed() {
            return switch (this) {
                case CloseWhenLow -> S1StateValue.Low;
                case CloseWhenFloating -> S1StateValue.Floating;
                case CloseWhenHigh -> S1StateValue.High;
                case CloseWhenNotLow -> S1StateValue.High;
                case CloseWhenNotFloating -> S1StateValue.Low;
                case CloseWhenNotHigh -> S1StateValue.Low;
            };
        }

        public S1StateValue getS1Open() {
            return switch (this) {
                case CloseWhenLow -> S1StateValue.High;
                case CloseWhenFloating -> S1StateValue.Low;
                case CloseWhenHigh -> S1StateValue.Low;
                case CloseWhenNotLow -> S1StateValue.Low;
                case CloseWhenNotFloating -> S1StateValue.Floating;
                case CloseWhenNotHigh -> S1StateValue.High;
            };
        }

        public S1StateValue getS1FromOpenState(boolean isOpen) {
            if (isOpen) {
                return getS1Open();
            } else {
                return getS1Closed();
            }
        }

        public S2StateValue getS2Closed() {
            return switch (this) {
                case CloseWhenLow -> S2StateValue.Low;
                case CloseWhenFloating -> S2StateValue.Floating;
                case CloseWhenHigh -> S2StateValue.High;
                case CloseWhenNotLow -> S2StateValue.High;
                case CloseWhenNotFloating -> S2StateValue.Low;
                case CloseWhenNotHigh -> S2StateValue.Low;
            };
        }

        public S2StateValue getS2Open() {
            return switch (this) {
                case CloseWhenLow -> S2StateValue.High;
                case CloseWhenFloating -> S2StateValue.Low;
                case CloseWhenHigh -> S2StateValue.Low;
                case CloseWhenNotLow -> S2StateValue.Low;
                case CloseWhenNotFloating -> S2StateValue.Floating;
                case CloseWhenNotHigh -> S2StateValue.High;
            };
        }

        public S2StateValue getS2FromOpenState(boolean isOpen) {
            if (isOpen) {
                return getS2Open();
            } else {
                return getS2Closed();
            }
        }
    }
}
