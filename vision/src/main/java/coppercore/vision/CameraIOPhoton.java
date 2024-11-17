package coppercore.vision;

import coppercore.vision.CameraIO.CameraInputs;
import java.util.List;
import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;

public class CameraIOPhoton implements CameraIO {
    PhotonCamera camera;

    public CameraIOPhoton(CameraParams cameraParams) {
        camera = cameraParams.camera();
    }

    @Override
    public void updateInputs(CameraInputs inputs) {
        inputs.isConnected = camera.isConnected();
        List<PhotonPipelineResult> results = camera.getAllUnreadResults();
        if (!results.isEmpty()) {
            inputs.latestResults =
                    camera.getAllUnreadResults().toArray(new PhotonPipelineResult[0]);
            inputs.latestTimestampSeconds = results.get(results.size() - 1).getTimestampSeconds();
        }
    }
}
