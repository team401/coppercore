package coppercore.parameter_tools.json.adapters;

import coppercore.parameter_tools.json.JSONObject;
import edu.wpi.first.math.geometry.Translation2d;

public class JSONTranslation2d extends JSONObject<Translation2d> {
    double x;
    double y;

    public JSONTranslation2d(Translation2d translation) {
        super(translation);
        x = translation.getX();
        y = translation.getY();
    }

    @Override
    public Translation2d toJava() {
        return new Translation2d(x, y);
    }
}
