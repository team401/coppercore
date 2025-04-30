package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class JSONPose2d extends JSONObject<Pose2d> {
    Rotation2d rotation;
    Translation2d translation;

    public JSONPose2d(Pose2d pose) {
        super(pose);
        rotation = pose.getRotation();
        translation = pose.getTranslation();
    }

    @Override
    public Pose2d toJava() {
        return new Pose2d(translation, rotation);
    }
}
