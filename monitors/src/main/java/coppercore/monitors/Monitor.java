package coppercore.monitors;

import java.util.function.BooleanSupplier;

/**
 * This class detects faults with monitors. It checks if they are sticky or nonsticky, if it is non-
 * sticky the monitor does not report the fault after conditions return to normal, otherwise it
 * does. It finds faults by checking for how long they are occur and the invalid state. It can
 * optionally log fault occurances. It also provides a convinient way to configure a monitor object.
 */
public class Monitor {
    /** Name to log the status of the monitor under, used by MonitoredSubsystem */
    String name;

    /** Should the monitor still report a fault after conditions return to normal? */
    boolean sticky;

    /** How long the value can be unacceptable before a fault occurs */
    double timeToFault;

    /** Supplier with which to check whether the value is acceptable */
    BooleanSupplier isStateValid;

    /** Function to call when the fault happens */
    Runnable faultCallback;

    /**
     * Should the monitor be logged by the MonitoredSubsystem?
     *
     * <p>Changing this value doesn't change the behavior of the monitor, it exists only to tell
     * MonitoredSubsystem whether or not to log the monitor.
     */
    boolean loggingEnabled;

    /**
     * Timestamp when the monitor was first triggered, or -1.0 if the monitor has been triggered for
     * less than 1 loop.
     */
    double triggeredTime = -1.0;

    // If triggeredTime's value is less than or equal to zero, the monitor has been triggered for
    // less than 1
    // tick

    /** Whether the value is currently unnacceptable */
    boolean triggered = false; // Is the value currently unnacceptable?

    /**
     * Whether the monitor has detected a fault
     *
     * <p>This means either the monitor has been triggered for the 'time to fault', or the monitor
     * is sticky and has faulted without being reset.
     */
    boolean faulted = false; // Has the monitor detected a fault?

    /**
     * Creates a fault Monitor. This constructor takes all parameters at once. There is also a
     * builder pattern supplied under MonitorBuilder. Using the builder is recommended because it
     * makes code much more readable, but is not required.
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
     * @see MonitorBuilder
     */
    public Monitor(
            String name,
            boolean sticky,
            BooleanSupplier isStateValid,
            double timeToFault,
            Runnable faultCallback,
            boolean loggingEnabled) {
        this.name = name;
        this.sticky = sticky;
        this.timeToFault = timeToFault;
        this.isStateValid = isStateValid;

        this.faultCallback = faultCallback;

        this.loggingEnabled = loggingEnabled;
    }

    /**
     * This is the "main loop" of the Monitor. This should be called each in each periodic loop.
     *
     * @param currentTimeSeconds the current timestamp in seconds. This doesn't need to have a
     *     specific frame of reference, it is used to detect when conditions have been unnacceptable
     *     for enough time to fault.
     */
    public void periodic(double currentTimeSeconds) {
        // currentTimeSeconds doesn't need to be from a specific point in history
        // As long as the reference point is always the same, it could be from
        // the robot being turned on, initialized, etc.

        triggered = !isStateValid.getAsBoolean();
        if (triggered) {
            // If triggered time is less than zero, this means it hasn't been set yet.
            // Therefore, this is the first loop that the monitor is triggered and we should
            // store the current timestamp to reference how long it's been triggered for later.
            if (triggeredTime <= 0.0) {
                triggeredTime = currentTimeSeconds;
            }

            // When triggered, the monitor will fault if either:
            //  - It is already faulted (it can't transition from faulted to non-faulted while
            // triggered)
            //  or
            //  - It has been triggered for the timeToFault.
            faulted = faulted || ((currentTimeSeconds - triggeredTime) >= timeToFault);
        } else {
            if (!sticky) {
                faulted = false;
            }
            // If the monitor isn't triggered, it will only be faulted if it is sticky and already
            // faulted.
            faulted = faulted && sticky;

            // Use -1 as a sentinel value to indicate that the triggered time hasn't been stored
            // yet.
            triggeredTime = -1.0;
        }
        if (faulted) {
            faultCallback.run();
        }
    }

    /**
     * Returns a boolean describing whether or not the monitor is faulted. A monitor is considered
     * faulted when it has been triggered for a time greater than or equal to its time to fault. A
     * monitor is also considered faulted if it is sticky and has ever been faulted.
     *
     * @return whether or not the monitor is currently faulted.
     */
    public boolean isFaulted() {
        return faulted;
    }

    /**
     * Returns a boolean describing whether or not the monitor is currently triggered. A monitor is
     * triggered when isStateValid returns false.
     *
     * @return whether or not the monitor is currently triggered
     */
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Reset a sticky fault. This means that, if a Monitor is sticky, and is currently faulted,
     * calling this function will return it to a non-faulted state.
     */
    public void resetStickyFault() {
        faulted = false;
    }

    /**
     * Get the name of the Monitor.
     *
     * @return a string, the name of the monitor that it uses for logging.
     */
    public String getName() {
        return name;
    }

    /**
     * Set whether or not the monitor should be logged.
     *
     * <p>Changing this value doesn't change the behavior of the monitor, it exists only to tell
     * MonitoredSubsystem whether or not to log the monitor.
     *
     * @param loggingEnabled whether or not or monitor should be logged.
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    /**
     * Get whether or not the monitor should be logged.
     *
     * <p>Changing this value doesn't change the behavior of the monitor, it exists only to tell
     * MonitoredSubsystem whether or not to log the monitor.
     *
     * @return Whether or not logging should be enabled
     */
    public boolean getLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * This class is meant to build a fault monitor. Create a builder, then call withName,
     * withStickyness, withTimeToFault, and withIsStateValid, and withFaultCallback to configure its
     * fields. Once every field is configured, call build() to return a shiny new fault monitor.
     */
    public static class MonitorBuilder {
        protected String name; // Name to log the status of the monitor under
        protected boolean
                sticky; // Should the monitor still report a fault after conditions return to
        // normal?
        protected double
                timeToFault; // How long the value can be unacceptable before a fault occurs
        protected BooleanSupplier
                isStateValid; // Supplier with which to check whether the value is acceptable
        protected Runnable faultCallback; // Function to call when the fault happens
        protected boolean loggingEnabled =
                true; // Whether or not to log the monitor. Defaults to true.

        /**
         * Sets the name of the monitor. This name will be used when the monitor is logged by
         * MonitoredSubsystem.
         *
         * @param name the name of the monitor, which is used for logging by MonitoredSubsystem or
         *     can be used for manual logging outside of a MonitoredSubsystem.
         * @return the monitor builder, so that successive builder calls can be chained
         */
        public MonitorBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets whether or not the monitor is sticky or not, and returns itself.
         *
         * @param sticky a boolean, whether or not the monitor should remain faulted after
         *     conditions return to normal.
         * @return the monitor builder, so that successive builder calls can be chained.
         */
        public MonitorBuilder withStickyness(boolean sticky) {
            this.sticky = sticky;
            return this;
        }

        /**
         * Sets how long the monitor may be triggered before it faults, and returns itself.
         *
         * @param timeToFault a double, how long the monitor can be triggered (in an unnacceptable
         *     state) before it becomes faulted in seconds.
         * @return the monitor builder, so that successive builder calls can be chained.
         */
        public MonitorBuilder withTimeToFault(double timeToFault) {
            this.timeToFault = timeToFault;
            return this;
        }

        /**
         * Sets the supplier for whether or not the state is currently valid.
         *
         * @param isStateValid a boolean supplier, which should return true when the state is valid
         *     and false when the state is invalid. This supplier doesn't need to account for
         *     timeToFault, this is automatically handled by the monitor.
         * @return the monitor, so that successive builder calls can be chained.
         */
        public MonitorBuilder withIsStateValidSupplier(BooleanSupplier isStateValid) {
            this.isStateValid = isStateValid;
            return this;
        }

        /**
         * Sets how long the monitor may be triggered before it faults, and returns itself.
         *
         * @param faultCallback a runnable, which will be called periodic unnacceptable state)
         *     before it becomes faulted in seconds.
         * @return the monitor builder, so that successive builder calls can be chained.
         */
        public MonitorBuilder withFaultCallback(Runnable faultCallback) {
            this.faultCallback = faultCallback;
            return this;
        }

        /**
         * Sets whether or not the monitor should be logged.
         *
         * <p>This value is only used to tell the MonitoredSubsystem whether or not to log each
         * monitor. This value defaults to true unless withLoggingEnabled(false) is called on the
         * builder!
         *
         * @param loggingEnabled whether or not the monitor should be logged
         * @return the monitor builder, so that successive builder calls can be chained.
         */
        public MonitorBuilder withLoggingEnabled(boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
            return this;
        }

        /**
         * Instantiates a monitor and returns it. This method should be called after all of the
         * fields of the monitor are configured using with[Field] methods.
         *
         * @return a monitor with the fields set by the builder.
         */
        public Monitor build() {
            return new Monitor(
                    name, sticky, isStateValid, timeToFault, faultCallback, loggingEnabled);
        }
    }
}
