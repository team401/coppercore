package coppercore.parameter_tools.json.adapters;

import java.lang.reflect.Constructor;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Rotation2d;

/** A JSON representation of a Rotation2d object. */
public class JSONRotation2d extends JSONObject<Object> {
    double radians;

    /**
     * Default constructor for JSON serialization.
     *
     * @param rotation The Rotation2d object to convert to JSON.
     */
    public JSONRotation2d(Rotation2d rotation) {
        super(rotation);
        radians = rotation.getRadians();
    }

    @Override
    public Rotation2d toJava() {
        return new Rotation2d(radians);
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONRotation2d> getConstructor() throws NoSuchMethodException {
        return JSONRotation2d.class.getConstructor(Rotation2d.class);
    }
}
