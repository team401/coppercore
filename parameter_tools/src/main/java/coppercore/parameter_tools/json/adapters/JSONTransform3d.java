package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.lang.reflect.Constructor;

/** A JSON representation of a Transform3d object. */
public class JSONTransform3d extends JSONObject<Transform3d> {
    Rotation3d rotation;
    Translation3d translation;

    /**
     * Default constructor for JSON serialization.
     *
     * @param transform The Transform3d object to convert to JSON.
     */
    public JSONTransform3d(Transform3d transform) {
        super(transform);
        rotation = transform.getRotation();
        translation = transform.getTranslation();
    }

    /** {@inheritDoc} */
    @Override
    public Transform3d toJava() {
        return new Transform3d(translation, rotation);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     * @throws NoSuchMethodException if the wrapper constructor is missing
     */
    public static Constructor<JSONTransform3d> getConstructor() throws NoSuchMethodException {
        return JSONTransform3d.class.getConstructor(Transform3d.class);
    }
}
