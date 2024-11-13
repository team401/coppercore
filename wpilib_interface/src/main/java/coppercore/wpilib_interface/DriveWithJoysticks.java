package coppercore.wpilib_interface;

import coppercore.math.Deadband;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;

public class DriveWithJoysticks extends Command {
    private DriveTemplate drive;
    private CommandJoystick leftJoystick;
    private CommandJoystick rightJoystick;
    private double maxLinearVelocity;
    private double maxAngularVelocity;

    public DriveWithJoysticks(
            DriveTemplate drive, CommandJoystick leftJoystick, CommandJoystick rightJoystick, double maxLinearVelocity, double maxAngularVelocity) {
        this.drive = drive;
        this.leftJoystick = leftJoystick;
        this.rightJoystick = rightJoystick;
        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
        
        addRequirements(this.drive);
    }

    @Override
    public void execute() {
        Translation2d linearSpeeds = getLinearVelocity(leftJoystick.getX(), leftJoystick.getY());

        double omega = Deadband.oneAxisDeadband(rightJoystick.getX(), 0.1);
        omega = Math.copySign(omega * omega, omega);

        drive.setGoalSpeeds(new ChassisSpeeds(linearSpeeds.getX() * maxLinearVelocity, linearSpeeds.getY() * maxLinearVelocity, omega * maxAngularVelocity), true);
    }

    public Translation2d getLinearVelocity(double x, double y) {
        double deadband = Deadband.oneAxisDeadband(Math.hypot(x, y), 0.1);

        Rotation2d direction = new Rotation2d(x, y);
        double squaredMagnitude = deadband * deadband;

        Translation2d linearVelocity =
                new Pose2d(new Translation2d(), direction)
                        .transformBy(new Transform2d(squaredMagnitude, 0.0, new Rotation2d()))
                        .getTranslation();

        return linearVelocity;
    }
}
