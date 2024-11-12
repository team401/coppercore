package coppercore.wpi_interface;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

public interface DriveTemplate {
    public void setGoalSpeeds(ChassisSpeeds goalSpeeds, boolean fieldCentric);
}
