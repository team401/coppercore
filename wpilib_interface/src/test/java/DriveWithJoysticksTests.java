import coppercore.wpilib_interface.DriveWithJoysticks;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The DriveWithJoysticksTests class tests the functionality of DriveWithJoysticks to make sure it's
 * passing through joystick values and correctly obeying and normalizing joystick inputs.
 */
public class DriveWithJoysticksTests {
    // Track sticks here for use with suppliers in each test.
    private double leftStickX = 0.0;
    private double leftStickY = 0.0;
    private double rightStickX = 0.0;

    /**
     * Helper method to easily assert that the dummy drivetrain is still in a field centric request.
     */
    private void assertFieldCentric(DummyDriveTemplate dummyDrive) {
        Assertions.assertTrue(
                dummyDrive.getLastFieldCentricValue(),
                "DriveWithJoysticks should always be requesting field centric speeds.");
    }

    private static double SPEED_EPSILON = 1e-6;

    /**
     * Assert that each field of a given set of ChassisSpeds is within SPEED_EPSILON of an expected
     * value.
     *
     * <p>Using AssertEquals seems to try pointer comparison and fail, so this method is required
     * instead.
     *
     * @param speeds The ChassisSpeeds to test
     * @param expectedVx The expected vx in m/s
     * @param expectedVx The expected vy in m/s
     * @param expectedOmega The expected omega (angular velocity) in rad/s
     * @param message The message to pass to each assertEquals call
     */
    private void assertChassisSpeeds(
            ChassisSpeeds speeds,
            double expectedVx,
            double expectedVy,
            double expectedOmega,
            String message) {
        Assertions.assertEquals(expectedVx, speeds.vxMetersPerSecond, SPEED_EPSILON, message);
        Assertions.assertEquals(expectedVy, speeds.vyMetersPerSecond, SPEED_EPSILON, message);
        Assertions.assertEquals(
                expectedOmega, speeds.omegaRadiansPerSecond, SPEED_EPSILON, message);
    }

    @Test
    public void testNoDeadbands() {
        var dummyDrive = new DummyDriveTemplate();
        var command =
                new DriveWithJoysticks(
                        dummyDrive,
                        () -> leftStickX,
                        () -> leftStickY,
                        () -> rightStickX,
                        1.0,
                        Math.PI,
                        0.0);

        command.initialize();

        command.execute();

        Assertions.assertEquals(
                new ChassisSpeeds(),
                dummyDrive.getLastGoalSpeeds(),
                "Zeros on joysticks should result in zero chassis speeds.");
        assertFieldCentric(dummyDrive);

        leftStickX = 0.5;
        command.execute();

        assertChassisSpeeds(
                dummyDrive.getLastGoalSpeeds(),
                0.0,
                -0.25,
                0.0,
                "Moving left stick x to 0.5 should result in a y velocity of -(0.5^2) (moving left"
                    + " stick to the right = translation to the right from driver POV = negative y"
                    + " in field coordinates).");
        assertFieldCentric(dummyDrive);

        leftStickX = 0.0;
        leftStickY = -0.5;
        command.execute();

        assertChassisSpeeds(
                dummyDrive.getLastGoalSpeeds(),
                0.25,
                0.0,
                0.0,
                "Moving left stick y to 0.5 should result in an x velocity of (0.5^2) (moving left"
                        + " stick up = translation forward from driver POV = positive x"
                        + " in field coordinates).");
        assertFieldCentric(dummyDrive);

        // For circular sticks, these are the max values when in the corner of the range of motion
        leftStickX = Math.sqrt(2) / 2.0;
        leftStickY = Math.sqrt(2) / 2.0;
        rightStickX = 1.0;
        command.execute();

        assertChassisSpeeds(
                dummyDrive.getLastGoalSpeeds(),
                -Math.sqrt(2.0) / 2.0,
                -Math.sqrt(2.0) / 2.0,
                -Math.PI,
                "Moving all 3 sticks to bottom + right should result in sqrt(2)/2 velocity in the"
                    + " negative direction for both translation axes and -pi angular velocity.");

        assertFieldCentric(dummyDrive);
    }
}
