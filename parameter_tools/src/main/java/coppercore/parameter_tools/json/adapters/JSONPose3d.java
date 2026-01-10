package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.lang.reflect.Constructor;

/** A JSON representation of a Pose3d object. */
public class JSONPose3d extends JSONObject<Pose3d> {
    Rotation3d rotation;
    Translation3d translation;

    /**
     * Default constructor for JSON serialization.
     *
     * @param pose The Pose3d object to convert to JSON.
     */
    public JSONPose3d(Pose3d pose) {
        super(pose);
        rotation = pose.getRotation();
        translation = pose.getTranslation();
    }

    @Override
    public Pose3d toJava() {
        return new Pose3d(translation, rotation);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONPose3d> getConstructor() throws NoSuchMethodException {
        return JSONPose3d.class.getConstructor(Pose3d.class);
    }
}
