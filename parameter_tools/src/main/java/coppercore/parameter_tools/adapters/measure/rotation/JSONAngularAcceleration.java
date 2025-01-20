package coppercore.parameter_tools.adapters.measure.rotation;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.AngularAcceleration;

public class JSONAngularAcceleration extends JSONObject<AngularAcceleration> {
    double value;
    String unit;

    public JSONAngularAcceleration(AngularAcceleration measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public AngularAcceleration toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
