package coppercore.parameter_tools.adapters.measure.distance;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.LinearAcceleration;

public class JSONLinearAcceleration extends JSONObject<LinearAcceleration> {
    double value;
    String unit;

    public JSONLinearAcceleration(LinearAcceleration measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public LinearAcceleration toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
