package coppercore.monitors;

import java.util.function.BooleanSupplier;

public class Monitor {
    String name; // Name to log the status of the monitor under
    boolean sticky; // Should the monitor still report a fault after conditions return to normal?
    double timeToFault; // How long the value can be unacceptable before a fault occurs
    BooleanSupplier isStateValid; // Supplier with which to check whether the value is acceptable
    Runnable faultCallback; // Function to call when the fault happens

    double triggeredTime = 0.0; // Timestamp when monitor was first triggered
    // If this value is zero, the monitor has been triggered for less than 1 tick

    boolean triggered = false; // Is the value currently unnacceptable?
    boolean faulted = false; // Has the monitor detected a fault?

    public Monitor(
            String name,
            boolean sticky,
            BooleanSupplier isStateValid,
            double timeToFault,
            Runnable faultCallback) {
        this.name = name;
        this.sticky = sticky;
        this.timeToFault = timeToFault;
        this.isStateValid = isStateValid;

        this.faultCallback = faultCallback;
    }

    public void periodic(double currentTimeSeconds) {
        // currentTimeSeconds doesn't need to be from a specific point in history
        // As long as the reference point is always the same, it could be from
        // the robot being turned on, initialized, etc.

        triggered = !isStateValid.getAsBoolean();
        if (triggered) {
            if (triggeredTime == 0.0) {
                triggeredTime = currentTimeSeconds;
            }

            if (currentTimeSeconds - triggeredTime > timeToFault) {
                faulted = true;
            }
        } else {
            if (!sticky) {
                faulted = false;
            }

            triggeredTime = 0.0;
        }
        if (faulted) {
            faultCallback.run();
        }

        // TODO: Move logging to MonitoredSubsystem under wpi interface
        // Logger.recordOutput("monitors/" + name + "/triggered", triggered);
        // Logger.recordOutput("monitors/" + name + "/faulted", faulted);
    }

    public boolean isFaulted() {
        return faulted;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void resetStickyFault() {
        faulted = false;
    }
}
