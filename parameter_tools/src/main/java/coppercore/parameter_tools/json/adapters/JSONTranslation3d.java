package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Translation3d;
import java.lang.reflect.Constructor;

/**
 * A JSON adapter for the Translation3d class, enabling serialization and deserialization
 *
 * @author avidraccoon
 */
public class JSONTranslation3d extends JSONObject<Translation3d> {
    double x;
    double y;
    double z;

    /**
     * Default constructor for JSON serialization
     *
     * @param translation the Translation3d object to serialize
     */
    public JSONTranslation3d(Translation3d translation) {
        super(translation);
        x = translation.getX();
        y = translation.getY();
        z = translation.getZ();
    }

    @Override
    public Translation3d toJava() {
        return new Translation3d(x, y, z);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONTranslation3d> getConstructor() throws NoSuchMethodException {
        return JSONTranslation3d.class.getConstructor(Translation3d.class);
    }
}
