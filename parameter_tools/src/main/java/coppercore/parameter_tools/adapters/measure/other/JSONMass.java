package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Mass;

public class JSONMass extends JSONObject<Mass> {
    double value;
    String unit;

    public JSONMass(Mass measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Mass toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
