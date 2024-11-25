package coppercore.wpi_interface;

import coppercore.monitors.Monitor;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.List;
import org.littletonrobotics.junction.Logger;

public abstract class MonitoredSubsystem extends SubsystemBase {
    private List<Monitor> registeredMonitors = new ArrayList<Monitor>();

    private boolean loggingEnabled = true;

    public void addMonitor(Monitor monitor) {
        registeredMonitors.add(monitor);
    }

    @Override
    public void periodic() {
        monitoredPeriodic();
        runMonitors();
    }

    /**
     * OVERRIDE ME! This function is called every time the subsystem's periodic function is called.
     * However, MonitoredSubsytem automatically checks monitors during every periodic run.
     * Therefore, this method should be overridden as a replacement for the normal periodic function
     * in the implementation of the subsystem.
     *
     * <p>This method is called periodically by the {@link CommandScheduler}. Useful for updating
     * subsystem-specific state that you don't want to offload to a {@link Command}. Teams should
     * try to be consistent within their own codebases about which responsibilities will be handled
     * by Commands, and which will be handled here.
     */
    public abstract void monitoredPeriodic();

    private void runMonitors() {
        registeredMonitors.forEach(
                monitor -> {
                    monitor.periodic(Timer.getFPGATimestamp());

                    if (loggingEnabled && monitor.getLoggingEnabled()) {
                        Logger.recordOutput(
                                "monitors/" + monitor.getName() + "/triggered",
                                monitor.isTriggered());
                        Logger.recordOutput(
                                "monitors/" + monitor.getName() + "/faulted", monitor.isFaulted());
                    }
                });
    }

    /**
     * Set whether or not the monitored subsystem should log its monitors. This is enabled by
     * default, but can be disabled if there are RAM issues stemming from too many strings in
     * logging.
     *
     * @param loggingEnabled
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }
}
