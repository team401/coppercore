package coppercore.wpilib_interface;

import org.wpilib.command2.Subsystem;
import org.wpilib.math.kinematics.ChassisVelocities;

/**
 * This sets the goal speeds and is a foundational component of the drive subsystem. It allows us to
 * set the goal speed with either field-centric or robot-centric control modes.
 */
public interface DriveTemplate extends Subsystem {
    /**
     * This allows us to set the goal speeds
     *
     * @param goalVelocities This allows us to control the goal velocity
     * @param fieldCentric This determines whether or not we are using a field centric control mode
     */
    public void setGoalVelocities(ChassisVelocities goalVelocities, boolean fieldCentric);
}
