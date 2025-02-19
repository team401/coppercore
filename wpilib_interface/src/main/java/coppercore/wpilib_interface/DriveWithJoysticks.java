package coppercore.wpilib_interface;

import coppercore.math.Deadband;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import java.util.function.Supplier;

/**
 * This class allows us to be able to drive with the controller joysticks and get the linear
 * velocity along with controlling it. It also allows us to get the goal speeds.
 */
public class DriveWithJoysticks extends Command {
    private final DriveTemplate drive;
    private final Supplier<Double> driveXSupplier;
    private final Supplier<Double> driveYSupplier;
    private final Supplier<Double> rotationSupplier;
    private final double maxLinearVelocity;
    private final double maxAngularVelocity;
    private final double joystickDeadband;

    /**
     * This defines a couple of variables that are to be used to help drive with the joysticks.
     *
     * @param drive This is the drive subsystem supplied by the robot project
     * @param leftJoystick This controls the left joystick
     * @param rightJoystick This controls the right joystick
     * @param maxLinearVelocity This sets the maximum linear velocity
     * @param maxAngularVelocity This sets the maximum angular velocity
     * @param joystickDeadband This sets the deadband of the joysticks
     */
    public DriveWithJoysticks(
            DriveTemplate drive,
            CommandJoystick leftJoystick,
            CommandJoystick rightJoystick,
            double maxLinearVelocity,
            double maxAngularVelocity,
            double joystickDeadband) {
        this.drive = drive;
        this.driveXSupplier = () -> leftJoystick.getX();
        this.driveYSupplier = () -> leftJoystick.getY();
        this.rotationSupplier = () -> rightJoystick.getX();

        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
        this.joystickDeadband = joystickDeadband;

        addRequirements(this.drive);
    }

    public DriveWithJoysticks(
            DriveTemplate drive,
            Supplier<Double> driveXSupplier,
            Supplier<Double> driveYSupplier,
            Supplier<Double> rotationSupplier,
            double maxLinearVelocity,
            double maxAngularVelocity,
            double joystickDeadband) {

        this.drive = drive;

        this.driveXSupplier = driveXSupplier;
        this.driveYSupplier = driveYSupplier;

        this.rotationSupplier = rotationSupplier;

        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
        this.joystickDeadband = joystickDeadband;

        addRequirements(this.drive);
    }

    /**
     * This gets the linear velocity for the robot, sets the goal speeds, and gets the angular
     * velocity for the robot. Overall, it executes what the controller is doing.
     */
    @Override
    public void execute() {
        // clamp inputs between 0 and 1 to prevent crazy speeds
        double leftJoystickX = MathUtil.clamp(driveXSupplier.get(), -1, 1);
        double leftJoystickY = MathUtil.clamp(driveYSupplier.get(), -1, 1);
        double rightJoystickX = MathUtil.clamp(rotationSupplier.get(), -1, 1);

        Translation2d linearSpeeds = getLinearVelocity(-leftJoystickX, -leftJoystickY);

        double omega = Deadband.oneAxisDeadband(-rightJoystickX, joystickDeadband);
        omega = Math.copySign(omega * omega, omega);

        ChassisSpeeds speeds =
                new ChassisSpeeds(
                        linearSpeeds.getX() * maxLinearVelocity,
                        linearSpeeds.getY() * maxLinearVelocity,
                        omega * maxAngularVelocity);

        drive.setGoalSpeeds(speeds, true);
    }

    /**
     * calculates a translation with squared magnitude
     * 
     * @param x represents the x value of velocity
     * @param y represents the y value of velocity
     * 
     *  @return Translation2d with directions of velocity
     */
    public Translation2d getLinearVelocity(double x, double y) {
        double[] deadbands = Deadband.twoAxisDeadband(x, y, joystickDeadband);

        double xDeadband = deadbands[0];
        double yDeadband = deadbands[1];
        double magnitude = Math.hypot(xDeadband, yDeadband);

        /* joystick x/y is opposite of field x/y
         * therefore, x and y must be flipped for proper rotation of pose
         * BEWARE: not flipping will cause forward on joystick to drive right on field
         */
        Rotation2d direction = new Rotation2d(Math.atan2(x, y));
        double squaredMagnitude = magnitude * magnitude;

        Translation2d linearVelocity =
                new Pose2d(new Translation2d(), direction)
                        .transformBy(new Transform2d(squaredMagnitude, 0.0, new Rotation2d()))
                        .getTranslation();

        return linearVelocity;
    }
}
