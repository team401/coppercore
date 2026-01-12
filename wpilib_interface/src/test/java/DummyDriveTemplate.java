import coppercore.wpilib_interface.DriveTemplate;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * The DummyDriveTemplate class implements DriveTemplate by simply storing the values passed to it
 * by the DriveWithJoysticks command. This is intended to be used in unit tests for the
 * DriveWithJoysticks command.
 */
public class DummyDriveTemplate implements DriveTemplate {
    /** The last set of chassis speeds passed to setGoalSpeeds */
    private ChassisSpeeds lastGoalSpeeds = null;

    /** The last value of field centric passed to setGoalSpeeds. Defaults to false. */
    private boolean lastFieldCentric = false;

    public void setGoalSpeeds(ChassisSpeeds goalSpeeds, boolean fieldCentric) {
        lastGoalSpeeds = goalSpeeds;
        lastFieldCentric = fieldCentric;
    }

    /**
     * Get the last set of goal speeds that were passed to setGoalSpeeds.
     *
     * @return A ChassisSpeeds if setGoalSpeeds has been called, or null if not.
     */
    public ChassisSpeeds getLastGoalSpeeds() {
        return lastGoalSpeeds;
    }

    /**
     * Get the last value of fieldCentric passed to setGoalSpeeds.
     *
     * @return A boolean, the last value of fieldCentric passed to setGoalSpeeds, or false if it has
     *     not yet been called.
     */
    public boolean getLastFieldCentricValue() {
        return lastFieldCentric;
    }
}
