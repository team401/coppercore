package coppercore.vision;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import org.photonvision.PhotonCamera;
import org.photonvision.simulation.SimCameraProperties;

public record CameraParams(
        PhotonCamera camera, Transform3d robotToCamera, SimCameraProperties simCameraProp) {
    public static class CameraParamBuilder {
        String cameraName;
        Transform3d robotToCamera;

        SimCameraProperties simCameraProp = new SimCameraProperties();

        public CameraParamBuilder withCameraName(String cameraName) {
            this.cameraName = cameraName;

            return this;
        }

        public CameraParamBuilder withRobotToCamera(Transform3d robotToCamera) {
            this.robotToCamera = robotToCamera;

            return this;
        }

        public CameraParamBuilder withCalibration(int resX, int resY, Rotation2d diagFOV) {
            simCameraProp.setCalibration(resX, resY, diagFOV);

            return this;
        }

        public CameraParamBuilder withCalibError(
                float averageErrorPixels, float errorStdDevPixels) {
            simCameraProp.setCalibError(averageErrorPixels, errorStdDevPixels);

            return this;
        }

        public CameraParamBuilder withFPS(int fps) {
            simCameraProp.setFPS(fps);

            return this;
        }

        public CameraParamBuilder withLatency(double averageLatencyMs, double latencyStdDevMs) {
            simCameraProp.setAvgLatencyMs(averageLatencyMs);
            simCameraProp.setLatencyStdDevMs(latencyStdDevMs);

            return this;
        }

        public CameraParams build() {
            return new CameraParams(new PhotonCamera(cameraName), robotToCamera, simCameraProp);
        }
    }
}
