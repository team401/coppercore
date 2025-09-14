package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Translation2d;
import java.lang.reflect.Constructor;

/**
 * A JSON adapter for the Translation2d class, enabling serialization and deserialization
 *
 * @author avidraccoon
 */
public class JSONTranslation2d extends JSONObject<Translation2d> {
    double x;
    double y;

    /**
     * Default constructor for JSON deserialization
     *
     * @param translation the Translation2d object to serialize
     */
    public JSONTranslation2d(Translation2d translation) {
        super(translation);
        x = translation.getX();
        y = translation.getY();
    }

    @Override
    public Translation2d toJava() {
        return new Translation2d(x, y);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONTranslation2d> getConstructor() throws NoSuchMethodException {
        return JSONTranslation2d.class.getConstructor(Translation2d.class);
    }
}
