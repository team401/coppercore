package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Torque;

public class JSONTorque extends JSONObject<Torque> {
    double value;
    String unit;

    public JSONTorque(Torque measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Torque toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
