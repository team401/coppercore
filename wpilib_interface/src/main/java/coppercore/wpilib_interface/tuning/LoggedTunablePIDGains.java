package coppercore.wpilib_interface.tuning;

import coppercore.parameter_tools.LoggedTunableNumber;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;

public class LoggedTunablePIDGains {
    LoggedTunableNumber kP;
    LoggedTunableNumber kI;
    LoggedTunableNumber kD;
    LoggedTunableNumber kS;
    LoggedTunableNumber kG;
    LoggedTunableNumber kV;
    LoggedTunableNumber kA;

    /**
     * Creates zeroed PID/feedforward tunables.
     *
     * @param namePrefix logged tunable path prefix
     */
    public LoggedTunablePIDGains(String namePrefix) {
        this(namePrefix, new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
    }

    /**
     * Creates PID/feedforward tunables from default gains.
     *
     * @param namePrefix logged tunable path prefix
     * @param defaultGains default gains
     */
    public LoggedTunablePIDGains(String namePrefix, PIDGains defaultGains) {
        this(
                namePrefix,
                new double[] {
                    defaultGains.kP(),
                    defaultGains.kI(),
                    defaultGains.kD(),
                    defaultGains.kS(),
                    defaultGains.kG(),
                    defaultGains.kV(),
                    defaultGains.kA()
                });
    }

    /**
     * Creates PID/feedforward tunables from raw defaults.
     *
     * @param namePrefix logged tunable path prefix
     * @param defaultValues kP, kI, kD, kS, kG, kV, and kA values
     */
    public LoggedTunablePIDGains(String namePrefix, double[] defaultValues) {
        if (defaultValues.length != 7) {
            throw new IllegalArgumentException("defaultValues must have length 7");
        }

        kP = new LoggedTunableNumber(namePrefix + "/kP", defaultValues[0]);
        kI = new LoggedTunableNumber(namePrefix + "/kI", defaultValues[1]);
        kD = new LoggedTunableNumber(namePrefix + "/kD", defaultValues[2]);
        kS = new LoggedTunableNumber(namePrefix + "/kS", defaultValues[3]);
        kG = new LoggedTunableNumber(namePrefix + "/kG", defaultValues[4]);
        kV = new LoggedTunableNumber(namePrefix + "/kV", defaultValues[5]);
        kA = new LoggedTunableNumber(namePrefix + "/kA", defaultValues[6]);
    }

    /**
     * Gets the backing logged tunable numbers.
     *
     * @return gains in kP, kI, kD, kS, kG, kV, kA order
     */
    public LoggedTunableNumber[] getGainsArray() {
        return new LoggedTunableNumber[] {kP, kI, kD, kS, kG, kV, kA};
    }

    /**
     * Reads the latest tuned gains.
     *
     * @return current PID/feedforward gains
     */
    public PIDGains getCurrentGains() {
        return new PIDGains(
                kP.getAsDouble(),
                kI.getAsDouble(),
                kD.getAsDouble(),
                kS.getAsDouble(),
                kG.getAsDouble(),
                kV.getAsDouble(),
                kA.getAsDouble());
    }

    /**
     * Runs a callback when any gain changes for a caller id.
     *
     * @param id caller id used to track changes independently
     * @param callback callback receiving the updated gains
     */
    public void ifChanged(int id, GainsConsumer callback) {
        LoggedTunableNumber.ifChanged(
                id, gains -> callback.accept(getCurrentGains()), getGainsArray());
    }

    /**
     * Sends the current gains to a callback.
     *
     * @param callback callback receiving the current gains
     */
    public void getValues(GainsConsumer callback) {
        callback.accept(getCurrentGains());
    }

    /**
     * Creates a callback that applies gains to one motor IO.
     *
     * @param motorIO motor IO to update
     * @return gains consumer for the motor IO
     */
    public GainsConsumer getMotorIOApplier(MotorIO motorIO) {
        return pidGains -> pidGains.applyToMotorIO(motorIO);
    }

    /**
     * Creates a callback that applies gains to multiple motor IOs.
     *
     * @param motorIOs motor IOs to update
     * @return gains consumer for all motor IOs
     */
    public GainsConsumer getMotorIOAppliers(MotorIO... motorIOs) {
        return pidGains -> {
            for (MotorIO motorIO : motorIOs) {
                pidGains.applyToMotorIO(motorIO);
            }
        };
    }

    @FunctionalInterface
    public interface GainsConsumer {
        GainsConsumer noOp = pidGains -> {};

        /**
         * Accepts updated PID/feedforward gains.
         *
         * @param pidGains updated gains
         */
        void accept(PIDGains pidGains);

        /**
         * Chains another gains consumer after this one.
         *
         * @param after consumer to run after this one
         * @return combined consumer
         */
        default GainsConsumer andThen(GainsConsumer after) {
            return pidGains -> {
                accept(pidGains);
                after.accept(pidGains);
            };
        }

        /**
         * Gets a consumer that ignores gain updates.
         *
         * @return no-op consumer
         */
        static GainsConsumer noOp() {
            return noOp;
        }
    }
}
