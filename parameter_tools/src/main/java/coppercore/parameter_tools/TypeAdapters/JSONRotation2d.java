package coppercore.parameter_tools.TypeAdapters;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.math.geometry.Rotation2d;

public class JSONRotation2d extends JSONObject<Object> {
    double radians;

    public JSONRotation2d(Rotation2d rotation) {
        super(rotation);
        radians = rotation.getRadians();
    }

    @Override
    public Rotation2d toJava() {
        return new Rotation2d(radians);
    }
}
