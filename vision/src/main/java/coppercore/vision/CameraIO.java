package coppercore.vision;

import org.littletonrobotics.junction.AutoLog;
import org.photonvision.targeting.PhotonPipelineResult;

public interface CameraIO {
    @AutoLog
    public static class CameraInputs {
        public boolean isConnected;

        /**
         * The last set of unread results from photonvision that wasn't empty. This is purely for
         * logging purposes (so that logs aren't empty most of the time)
         */
        public PhotonPipelineResult[] latestResults;

        /**
         * The results of calling getAllUnreadResults() These can be processed once per call of
         * updateInputs without any issues with duplicates.
         */
        public PhotonPipelineResult[] unreadResults;

        /** The timestamp of the latest measurement result, calculated by photonvision */
        public double latestTimestampSeconds;
    }

    public default void updateInputs(CameraInputs inputs) {}
}
