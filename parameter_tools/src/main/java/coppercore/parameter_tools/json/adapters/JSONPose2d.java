package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import java.lang.reflect.Constructor;

/** A JSON representation of a Pose2d object. */
public class JSONPose2d extends JSONObject<Pose2d> {
    Rotation2d rotation;
    Translation2d translation;

    /**
     * Default constructor for JSON deserialization.
     *
     * @param pose The Pose2d object to convert to JSON.
     */
    public JSONPose2d(Pose2d pose) {
        super(pose);
        rotation = pose.getRotation();
        translation = pose.getTranslation();
    }

    @Override
    public Pose2d toJava() {
        return new Pose2d(translation, rotation);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONPose2d> getConstructor() throws NoSuchMethodException {
        return JSONPose2d.class.getConstructor(Pose2d.class);
    }
}
