package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.math.geometry.Rotation2d;

/** A JSON representation of a Rotation2d object. */
public class JSONRotation2d extends JSONObject<Object> {
    double radians;

    /**
     * Default constructor for JSON deserialization.
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
}
