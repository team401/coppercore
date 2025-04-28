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
 * A command to control a drivetrain using joysticks as input.
 *
 * <p>This command squares the magnitude of the inputs for more precise low-speed control.
 */
public class DriveWithJoysticks extends Command {
    private final DriveTemplate drive;
    private final Supplier<Double> driveXSupplier;
    private final Supplier<Double> driveYSupplier;
    private final Supplier<Double> rotationSupplier;
    private double maxLinearVelocity;
    private double maxAngularVelocity;
    private final double joystickDeadband;

    /**
     * Initialize a DriveWithJoysticks command using CommandJoysticks for input.
     *
     * <p>If using a controller that does not support CommandJoystick, there is an alternate
     * initializer that accepts suppliers.
     *
     * @param drive The drive subsystem supplied by the robot project.
     * @param leftJoystick The left (translation/strafe) joystick.
     * @param rightJoystick The right (steer) joystick.
     * @param maxLinearVelocity Maximum driving velocity, which will be commanded when joystick is
     *     fully deflected. In m/s.
     * @param maxAngularVelocity Maximum steering velocity, which will be commanded when steer
     *     joystick is fully deflected. In rad/s.
     * @param joystickDeadband Deadband to apply to the joystick inputs.
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

    /**
     * Initialize a DriveWithJoysticks command using suppliers for input.
     *
     * <p>If using a controller that supports CommandJoystick, there is an alternate initializer
     * that accepts CommandJoystick objects.
     *
     * @param drive The Drive subsystem supplied by the robot project.
     * @param driveXSupplier A double supplier supplying the X axis of the strafe. joystick. This is
     *     the left/right drive control as viewed from the top of the joystick, NOT the X axis in
     *     robot coordinates.
     * @param driveYSupplier A double supplier supplying the Y axis of the strafe joystick. This is
     *     the up/down drive control as viewed from the top of the joystick, NOT the Y axis in robot
     *     coordinates.
     * @param rotationSupplier A double supplier supplying the steer axis input.
     * @param maxLinearVelocity Maximum driving velocity, which will be commanded when joystick is
     *     fully deflected. In m/s.
     * @param maxAngularVelocity Maximum steering velocity, which will be commanded when steer
     *     joystick is fully deflected. In rad/s.
     * @param joystickDeadband Deadband to apply to the joystick inputs.
     */
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
        omega = (omega - joystickDeadband) / (1 - joystickDeadband);
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
     * @return Translation2d with directions of velocity
     */
    public Translation2d getLinearVelocity(double x, double y) {
        double[] deadbands = Deadband.twoAxisDeadband(x, y, joystickDeadband);

        double xDeadband = (deadbands[0] - joystickDeadband) / (1 - joystickDeadband);
        double yDeadband = (deadbands[1] - joystickDeadband) / (1 - joystickDeadband);
        double magnitude = Math.hypot(xDeadband, yDeadband);

        /*
         * joystick x/y is opposite of field x/y
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

    /**
     * Update the maximum linear and angular velocities.
     *
     * @param maxLinearVelocity The new maximum allowed linear velocity in m/s.
     * @param maxAngularVelocity The new maximum allowed angular velocity in rad/s.
     */
    public void setMaxSpeeds(double maxLinearVelocity, double maxAngularVelocity) {
        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
    }
}
