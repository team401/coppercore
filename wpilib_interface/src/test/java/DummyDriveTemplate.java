import coppercore.wpilib_interface.DriveTemplate;
import org.wpilib.math.kinematics.ChassisVelocities;

/**
 * The DummyDriveTemplate class implements DriveTemplate by simply storing the values passed to it
 * by the DriveWithJoysticks command. This is intended to be used in unit tests for the
 * DriveWithJoysticks command.
 */
public class DummyDriveTemplate implements DriveTemplate {
    /** The last set of chassis Velocities passed to setGoalVelocities */
    private ChassisVelocities lastGoalVelocities = null;

    /** The last value of field centric passed to setGoalVelocities. Defaults to false. */
    private boolean lastFieldCentric = false;

    public void setGoalVelocities(ChassisVelocities goalVelocities, boolean fieldCentric) {
        lastGoalVelocities = goalVelocities;
        lastFieldCentric = fieldCentric;
    }

    /**
     * Get the last set of goal velocities that were passed to setGoalVelocities.
     *
     * @return A ChassisVelocities if setGoalVelocities has been called, or null if not.
     */
    public ChassisVelocities getLastGoalVelocities() {
        return lastGoalVelocities;
    }

    /**
     * Get the last value of fieldCentric passed to setGoalVelocities.
     *
     * @return A boolean, the last value of fieldCentric passed to setGoalVelocities, or false if it
     *     has not yet been called.
     */
    public boolean getLastFieldCentricValue() {
        return lastFieldCentric;
    }
}
