package coppercore.wpilib_interface.linear_drive;

import coppercore.controls.state_machine.state.PeriodicStateInterface;
import coppercore.wpilib_interface.DriveTemplate;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;

public abstract class LinearDriveStateBase implements PeriodicStateInterface {
    private Pose2d currentPose;
    private Pose2d goalPose;

    private double linearDriveErrorMargin = 0.25;

    private ProfiledPIDController driveController;

    private PIDController headingController;

    private Double kDriveToPointPhase2Distance = 1.0;
    private Double kDriveToPointPhase2Angle = Math.toRadians(45.0);

    private Double kDriveToPointEndVelocity = 0.0;

    private DriveTemplate drive;

    public record LinearDriveConfigs {};

    protected LinearDriveStateBase(
            DriveTemplate drive,
            double linearDriveErrorMargin,
            double kDriveToPointPhase2Distance,
            double kDriveToPointPhase2AngleRads,
            double kDriveToPointEndVelocity) {
        this.drive = drive;

        this.linearDriveErrorMargin = linearDriveErrorMargin;
        this.kDriveToPointPhase2Distance = kDriveToPointPhase2Distance;
        this.kDriveToPointPhase2Angle = kDriveToPointPhase2AngleRads;
        this.kDriveToPointEndVelocity = kDriveToPointEndVelocity;
    }

    private boolean hasRunPhase1 = false;
    private boolean hasEnteredPhase2 = false;

    private double getAngleBetweenVectors(Translation2d u, Translation2d v) {
        double u_norm = u.getNorm();
        double v_norm = v.getNorm();
        double dot_product = u.toVector().dot(v.toVector());

        // Avoid cases that break acos() and return 0.0 in these cases.
        if (u_norm * v_norm < 1e-10 || dot_product > (u_norm * v_norm)) {
            return 0.0;
        }
        return Math.acos(dot_product / (u_norm * v_norm));
    }

    private Pose2d findPhase1Pose(Pose2d phase2Pose) {
        // Project the goal pose backwards to find the phase 1 goal pose.
        double phase1OffsetX =
                kDriveToPointPhase2Distance
                        * Math.cos(phase2Pose.getRotation().getRadians() + Math.PI);
        double phase1OffsetY =
                kDriveToPointPhase2Distance
                        * Math.sin(phase2Pose.getRotation().getRadians() + Math.PI);
        Pose2d phase1Pose =
                new Pose2d(
                        phase2Pose.getX() + phase1OffsetX,
                        phase2Pose.getY() + phase1OffsetY,
                        phase2Pose.getRotation());

        return phase1Pose;
    }

    abstract double getDriveToPointEndVelocity();

    @Override
    public void periodic() {

        Pose2d phase1Pose = findPhase1Pose(goalPose);

        // Get distances to goal positions.
        double distanceToGoal = currentPose.getTranslation().getDistance(goalPose.getTranslation());
        double distanceToPhase1Pose =
                currentPose.getTranslation().getDistance(phase1Pose.getTranslation());

        // Find if the robot is within the slice area to transition to phase 2.
        Translation2d phase1PoseToGoal =
                goalPose.getTranslation().minus(phase1Pose.getTranslation());
        Translation2d robotToGoal = goalPose.getTranslation().minus(currentPose.getTranslation());
        double angleToTarget = getAngleBetweenVectors(robotToGoal, phase1PoseToGoal);

        // Determine which pose to aim at. However, always use the distance to the final pose to
        // compute
        // the target speed.
        Pose2d currentGoalPose = phase1Pose;
        // Use whichever distance we're going to so that we slow down on the way to phase 1 pose
        double distanceToCurrentGoal = distanceToPhase1Pose;
        double endVelocityGoal = 0.0; // Try to reach 0 velocity in phase 1
        if (hasEnteredPhase2
                || distanceToGoal < kDriveToPointPhase2Distance
                || Math.abs(angleToTarget) < kDriveToPointPhase2Angle) {
            currentGoalPose = goalPose;

            endVelocityGoal = kDriveToPointEndVelocity;

            if (!hasEnteredPhase2) {
                hasEnteredPhase2 = true;

                if (hasRunPhase1) {
                    // Bumplessing
                    State lastState = driveController.getSetpoint();
                    double lastVelocity = lastState.velocity;
                    double lastError = distanceToCurrentGoal - lastState.position;
                    double adjustedPosition = distanceToGoal - lastError;
                    driveController.reset(new State(adjustedPosition, lastVelocity));

                    System.out.println("Switched to phase 2 with bumplessing");
                } else {
                    driveController.reset(new State(distanceToGoal, 0.0));

                    System.out.println("Switched to phase 2 instantly");
                }

                // drive.disableReefCenterAlignment();
            }

            // This has to be here because distanceToCurrentGoal is used for PID reset adjustment
            // meme
            distanceToCurrentGoal = distanceToGoal;
        } else {
            hasRunPhase1 = true;
        }

        if (distanceToGoal < JsonConstants.drivetrainConstants.otfWarmupDistance
                && ScoringSubsystem.getInstance() != null) {
            ScoringSubsystem.getInstance().fireTrigger(ScoringTrigger.StartWarmup);
        } else if (distanceToGoal < JsonConstants.drivetrainConstants.otfFarWarmupDistance
                && ScoringSubsystem.getInstance() != null) {
            ScoringSubsystem.getInstance().fireTrigger(ScoringTrigger.StartFarWarmup);
        }

        // We only exit this state when the phase 2 pose has been achieved.
        if (distanceToGoal < linearDriveErrorMargin) {
            return;
        }

        // HEADING
        double headingError = currentPose.getRotation().minus(goalPose.getRotation()).getRadians();
        double headingVelocity =
                headingController.calculate(
                        currentPose.getRotation().getRadians(),
                        goalPose.getRotation().getRadians());

        double driveVelocityScalar =
                driveController.calculate(distanceToCurrentGoal, new State(0.0, endVelocityGoal));
        Translation2d driveVelocity =
                new Pose2d(
                                0.0,
                                0.0,
                                currentPose
                                        .getTranslation()
                                        .minus(currentGoalPose.getTranslation())
                                        .getAngle())
                        .transformBy(new Transform2d(driveVelocityScalar, 0.0, new Rotation2d()))
                        .getTranslation();
    }
}
