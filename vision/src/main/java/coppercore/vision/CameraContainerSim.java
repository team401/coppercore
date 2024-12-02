package coppercore.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
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

    SwerveDriveKinematics kinematics;

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
                new Pose2d(
                        initialPose.getX(),
                        initialPose.getY(),
                        Rotation2d.fromRadians(initialPose.getRotation().getRadians()));

        for (CameraParams param : params) {
            cameras.add(
                    new Camera(
                            param,
                            CameraIOPhoton.fromSimCameraParams(param, layout, visionSim, true)));
        }
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void update() {
        updateOdometry();
        visionSim.update(latestOdometryPose);
        SmartDashboard.putData("PhotonSimField", visionSim.getDebugField());

        for (Camera camera : cameras) {
            camera.update();
        }
    }

    private void updateOdometry() {
        SwerveModulePosition[] deltas = new SwerveModulePosition[4];
        SwerveModuleState[] states = getModuleStates.get();

        double dt = dtTimer.get();
        dtTimer.reset();
        dtTimer.start();

        for (int i = 0; i < states.length; i++) {
            deltas[i] =
                    new SwerveModulePosition(
                            states[i].speedMetersPerSecond * dt
                                    - lastModulePositions[i].distanceMeters,
                            Rotation2d.fromRadians(
                                    states[i]
                                            .angle
                                            .minus(lastModulePositions[i].angle)
                                            .getRadians()));
        }

        Twist2d twist = kinematics.toTwist2d(deltas);
        latestOdometryPose = latestOdometryPose.exp(twist);

        Logger.recordOutput("Vision/GroundTruth", latestOdometryPose);
    }
}
