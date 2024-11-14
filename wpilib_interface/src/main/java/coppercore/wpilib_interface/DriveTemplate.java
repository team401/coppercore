package coppercore.wpilib_interface;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;

public interface DriveTemplate extends Subsystem {
    public void setGoalSpeeds(ChassisSpeeds goalSpeeds, boolean fieldCentric);
}
