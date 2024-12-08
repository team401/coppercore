package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;
import org.photonvision.simulation.VisionSystemSim;

/**
 * CameraContainerSim is used for handling cameras in a sim. It simulates the camera's interactions
 * with the robot's state and the field's layout.
 */
public class CameraContainerSim implements CameraContainer {

    private VisionSystemSim visionSim = new VisionSystemSim("main");
    private List<Camera> cameras = new ArrayList<>();
    private Supplier<SwerveModuleState[]> getModuleStates;
    private Pose2d latestOdometryPose;
    private SwerveModulePosition[] lastModulePositions =
            new SwerveModulePosition[] {
                new SwerveModulePosition(),
                new SwerveModulePosition(),
                new SwerveModulePosition(),
                new SwerveModulePosition()
            };
    private Timer dtTimer = new Timer();
    private SwerveDriveKinematics kinematics;

    /**
     * Initializes a CameraContainerSim with camera parameters and field layout.
     *
     * @param params A list of camera parameters for the simulation.
     * @param layout The layout of the AprilTags in the simulation.
     * @param initialPose The initial pose of the robot.
     * @param kinematics The kinematics for the robot's swerve drive.
     * @param getModuleStates A supplier for the current swerve module states.
     */
    public CameraContainerSim(
            List<CameraParams> params,
            AprilTagFieldLayout layout,
            Pose2d initialPose,
            SwerveDriveKinematics kinematics,
            Supplier<SwerveModuleState[]> getModuleStates) {
        this.getModuleStates = getModuleStates;
        visionSim.addAprilTags(layout);
        this.kinematics = kinematics;
        latestOdometryPose =
                new Pose2d(initialPose.getX(), initialPose.getY(), initialPose.getRotation());

        for (CameraParams param : params) {
            cameras.add(
                    new Camera(
                            param,
                            CameraIOPhoton.fromSimCameraParams(param, layout, visionSim, true)));
        }
    }

    /**
     * Gets the list of cameras in the simulation.
     *
     * @return A list of cameras.
     */
    @Override
    public List<Camera> getCameras() {
        return cameras;
    }

    /**
     * Updates the odometry and camera states for the simulation. Updates both the simulated robot
     * pose and camera measurements.
     */
    @Override
    public void update() {
        updateOdometry();
        visionSim.update(latestOdometryPose);
        SmartDashboard.putData("PhotonSimField", visionSim.getDebugField());

        for (Camera camera : cameras) {
            camera.update();
        }
    }

    /** Updates the robot's odometry based on the latest swerve module states. */
    private void updateOdometry() {
        SwerveModuleState[] states = getModuleStates.get();
        double dt = dtTimer.get();
        dtTimer.reset();
        dtTimer.start();

        SwerveModulePosition[] deltas = new SwerveModulePosition[4];
        for (int i = 0; i < states.length; i++) {
            deltas[i] =
                    new SwerveModulePosition(
                            states[i].speedMetersPerSecond * dt
                                    - lastModulePositions[i].distanceMeters,
                            states[i].angle.minus(lastModulePositions[i].angle));
        }

        Twist2d twist = kinematics.toTwist2d(deltas);
        latestOdometryPose = latestOdometryPose.exp(twist);

        Logger.recordOutput("Vision/GroundTruth", latestOdometryPose);
    }
}
