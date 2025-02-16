package coppercore.wpilib_interface;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;

/**
 * This sets the goal speeds and is a foundational component of the drive subsystem. It allows us to
 * set the goal speed with either field-centric or robot-centric control modes.
 */
public interface DriveTemplate extends Subsystem {
    /**
     * This allows us to set the goal speeds
     *
     * @param goalSpeeds This allows us to control the goal speed
     * @param fieldCentric This determines whether or not we are using a field centric control mode
     */
    public void setGoalSpeeds(ChassisSpeeds goalSpeeds, boolean fieldCentric);
}
