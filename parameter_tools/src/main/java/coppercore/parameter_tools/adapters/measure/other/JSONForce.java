package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Force;

public class JSONForce extends JSONObject<Force> {
    double value;
    String unit;

    public JSONForce(Force measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Force toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
