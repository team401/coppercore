package coppercore.wpilib_interface;

import coppercore.monitors.Monitor;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import java.util.function.BooleanSupplier;

public class MonitorWithAlert extends Monitor {
    Alert alert;

    /**
     * Creates a fault monitor which uses persistent alerts. This constructor takes all parameters
     * at once. There is also a builder pattern supplied under MonitorWithAlertBuilder. Using the
     * builder is recommended because it makes code much more readable, but is not required.
     *
     * @param name the name of the monitor, which will be used by MonitoredSubsystem for logging.
     * @param sticky whether the fault should remain faulted after conditions return to an
     *     acceptable state.
     * @param isStateValid supplier for whether the state is CURRENTLY valid. This doesn't need to
     *     handle persistence, the monitor class will handle this automatically.
     * @param timeToFault the time, in seconds, that isStateValid must return false before the a
     *     fault is triggered.
     * @param faultCallback a function called on every periodic loop while the monitor is in a
     *     faulted state.
     * @param loggingEnabled whether or not the monitor should be logged. This value is only used by
     *     MonitoredSubsystem to enable or disable logging for each monitor.
     * @param group The group to display the alert under, e.g. "Alerts"
     * @param alertText the text to display in the persistent alert
     * @param alertType the type of the alert, e.g. AlertType.kWarning
     * @see MonitorWithAlertBuilder
     */
    public MonitorWithAlert(
            String name,
            boolean sticky,
            BooleanSupplier isStateValid,
            double timeToFault,
            Runnable faultCallback,
            boolean loggingEnabled,
            String group,
            String alertText,
            AlertType alertType) {
        super(name, sticky, isStateValid, timeToFault, faultCallback, loggingEnabled);

        alert = new Alert(group, alertText, alertType);
    }

    /**
     * This is the "main loop" of the Monitor. This should be called each in each periodic loop.
     *
     * @param currentTimeSeconds the current timestamp in seconds. This doesn't need to have a
     *     specific frame of reference, it is used to detect when conditions have been unnacceptable
     *     for enough time to fault.
     */
    public void periodic(double currentTimeSeconds) {
        super.periodic(currentTimeSeconds);

        alert.set(isFaulted());
    }

    public static class MonitorWithAlertBuilder extends MonitorBuilder {
        String group = "Alerts";
        String alertText;
        AlertType alertType;

        @Override
        public MonitorWithAlertBuilder withName(String name) {
            super.withName(name);
            return this;
        }

        @Override
        public MonitorWithAlertBuilder withStickyness(boolean sticky) {
            super.withStickyness(sticky);

            return this;
        }

        @Override
        public MonitorWithAlertBuilder withTimeToFault(double timeToFault) {
            super.withTimeToFault(timeToFault);

            return this;
        }

        @Override
        public MonitorWithAlertBuilder withIsStateValidSupplier(BooleanSupplier isStateValid) {
            super.withIsStateValidSupplier(isStateValid);

            return this;
        }

        @Override
        public MonitorWithAlertBuilder withFaultCallback(Runnable faultCallback) {
            super.withFaultCallback(faultCallback);

            return this;
        }

        @Override
        public MonitorWithAlertBuilder withLoggingEnabled(boolean loggingEnabled) {
            super.withLoggingEnabled(loggingEnabled);

            return this;
        }

        /**
         * Sets the group of the monitor. The monitor's persistent alert will be logged under this
         * group.
         *
         * <p>This group should be "Alerts", otherwise the alert won't display properly in elastic!
         *
         * @param group the group to place the alert into. Defaults to "Alerts"
         * @return the monitor builder, so that successive builder calls can be chained
         */
        public MonitorWithAlertBuilder withGroup(String group) {
            this.group = group;
            return this;
        }

        /**
         * Sets alert text of the monitor. This text will be displayed in the dashboard when the
         * monitor is faulted.
         *
         * @param alertText the alert text of the monitor can be used for manual logging outside of
         *     a MonitoredSubsystem.
         * @return the monitor builder, so that successive builder calls can be chained
         */
        public MonitorWithAlertBuilder withAlertText(String alertText) {
            this.alertText = alertText;
            return this;
        }

        /**
         * Sets alert type of the alert. This alert type will be used by the Persistent Alert (like
         * a log level)
         *
         * @param alertType The alertType of the alert, e.g. kWarning
         * @return the monitor builder, so that successive builder calls can be chained
         */
        public MonitorWithAlertBuilder withAlertType(AlertType alertType) {
            this.alertType = alertType;
            return this;
        }

        /**
         * Instantiates a MonitorWithAlert and returns it. This method should be called after all of
         * the fields of the monitor are configured using with[Field] methods.
         *
         * @return a MonitorWithAlert with the fields set by the builder.
         */
        public MonitorWithAlert build() {
            return new MonitorWithAlert(
                    name,
                    sticky,
                    isStateValid,
                    timeToFault,
                    faultCallback,
                    loggingEnabled,
                    group,
                    alertText,
                    alertType);
        }
    }
}
