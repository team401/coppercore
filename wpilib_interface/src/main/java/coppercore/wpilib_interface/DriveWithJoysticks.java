package coppercore.wpilib_interface;

import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RadiansPerSecond;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import java.util.function.Supplier;

/**
 * The DriveWithJoysticks command controls a holonomic drivetrain using the joysticks or suppliers
 * provided. It translates from the joystick to field coordinates, applies joystick deadbands, and
 * applies a configurable exponent to input magnitudes for more precise control.
 */
public class DriveWithJoysticks extends Command {
    private final DriveTemplate drive;
    private final Supplier<Double> driveXSupplier;
    private final Supplier<Double> driveYSupplier;
    private final Supplier<Double> rotationSupplier;
    private LinearVelocity maxLinearVelocity;
    private AngularVelocity maxAngularVelocity;
    private final double joystickDeadband;
    private final double magnitudeExponent;

    /**
     * Create a new DriveWithJoysticks command using CommandJoystick objects.
     *
     * <p>An alternate constructor exists for non-CommandJoystick teams that simply takes a Double
     * Supplier for each joystick input.
     *
     * @param drive The drive subsystem supplied by the robot project.
     * @param leftJoystick The left (translation/strafe) joystick.
     * @param rightJoystick The right (steer) joystick.
     * @param maxLinearVelocity Maximum driving velocity, which will be commanded when joystick is
     *     fully deflected.
     * @param maxAngularVelocity Maximum steering velocity, which will be commanded when steer
     *     joystick is fully deflected.
     * @param joystickDeadband Deadband to apply to the joystick inputs, as a fraction (0.0 to 1.0).
     *     This value is applied in both directions from zero (e.g. a deadband of 0.17 means that
     *     inputs from -0.17 to 0.17 are ignored).
     * @param magnitudeExponent The exponent that all joystick inputs will be raised to. Must be
     *     positive. For example, a (post-deadband) magnitude of 0.5 with a magnitudeExponent of 2.0
     *     would result in a command of 0.25 * max velocity.
     */
    public DriveWithJoysticks(
            DriveTemplate drive,
            CommandJoystick leftJoystick,
            CommandJoystick rightJoystick,
            LinearVelocity maxLinearVelocity,
            AngularVelocity maxAngularVelocity,
            double joystickDeadband,
            double magnitudeExponent) {
        this(
                drive,
                leftJoystick::getX,
                leftJoystick::getY,
                rightJoystick::getX,
                maxLinearVelocity,
                maxAngularVelocity,
                joystickDeadband,
                magnitudeExponent);
    }

    /**
     * Create a new DriveWithJoysticks command using suppliers for joystick inputs.
     *
     * <p>An alternate constructor exists for CommandJoystick teams that uses a CommandJoystick for
     * each stick, rather than suppliers.
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
     *     fully deflected.
     * @param maxAngularVelocity Maximum steering velocity, which will be commanded when steer
     *     joystick is fully deflected.
     * @param joystickDeadband Deadband to apply to the joystick inputs, as a fraction (0.0 to 1.0).
     *     This value is applied in both directions from zero (e.g. a deadband of 0.17 means that
     *     inputs from -0.17 to 0.17 are ignored).
     * @param magnitudeExponent The exponent that all joystick inputs will be raised to. Must be
     *     positive. For example, a (post-deadband) magnitude of 0.5 with a magnitudeExponent of 2.0
     *     would result in a command of 0.25 * max velocity.
     */
    public DriveWithJoysticks(
            DriveTemplate drive,
            Supplier<Double> driveXSupplier,
            Supplier<Double> driveYSupplier,
            Supplier<Double> rotationSupplier,
            LinearVelocity maxLinearVelocity,
            AngularVelocity maxAngularVelocity,
            double joystickDeadband,
            double magnitudeExponent) {

        this.drive = drive;

        this.driveXSupplier = driveXSupplier;
        this.driveYSupplier = driveYSupplier;

        this.rotationSupplier = rotationSupplier;

        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;

        if (joystickDeadband < 0.0 || 1.0 < joystickDeadband) {
            throw new IllegalArgumentException(
                    "Joystick deadband must be between 0 and 1 but was " + joystickDeadband);
        }
        this.joystickDeadband = joystickDeadband;

        if (magnitudeExponent <= 0.0) {
            throw new IllegalArgumentException(
                    "Magnitude Exponent must be positive but was " + magnitudeExponent);
        }
        this.magnitudeExponent = magnitudeExponent;

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

        double omega = MathUtil.applyDeadband(-rightJoystickX, joystickDeadband);
        omega = MathUtil.copyDirectionPow(omega, magnitudeExponent);

        ChassisSpeeds speeds =
                new ChassisSpeeds(
                        linearSpeeds.getX() * maxLinearVelocity.in(MetersPerSecond),
                        linearSpeeds.getY() * maxLinearVelocity.in(MetersPerSecond),
                        omega * maxAngularVelocity.in(RadiansPerSecond));

        drive.setGoalSpeeds(speeds, true);
    }

    /**
     * calculates a Translation2d representing linear speeds with magnitude raised to the
     * magnitudeExponent power
     *
     * @param x represents the x value of velocity
     * @param y represents the y value of velocity
     * @return Translation2d with directions of velocity
     */
    private Translation2d getLinearVelocity(double x, double y) {
        Vector<N2> deadbandedTranslation =
                MathUtil.applyDeadband(VecBuilder.fill(x, y), joystickDeadband);

        /*
         * joystick x/y is opposite of field x/y
         * therefore, x and y must be flipped for proper rotation of pose
         * BEWARE: not flipping will cause forward on joystick to drive right on field
         */
        Rotation2d direction = new Rotation2d(Math.atan2(x, y));
        double curvedMagnitude =
                MathUtil.copyDirectionPow(deadbandedTranslation.norm(), magnitudeExponent);

        Translation2d linearVelocity =
                new Pose2d(new Translation2d(), direction)
                        .transformBy(new Transform2d(curvedMagnitude, 0.0, new Rotation2d()))
                        .getTranslation();

        return linearVelocity;
    }

    /**
     * Update the maximum linear and angular velocities.
     *
     * @param maxLinearVelocity The new maximum allowed linear velocity
     * @param maxAngularVelocity The new maximum allowed angular velocity
     */
    public void setMaxSpeeds(LinearVelocity maxLinearVelocity, AngularVelocity maxAngularVelocity) {
        this.maxLinearVelocity = maxLinearVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
    }
}
