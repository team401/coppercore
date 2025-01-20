package coppercore.parameter_tools.adapters.measure.electricity;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Voltage;

public class JSONVoltage extends JSONObject<Voltage> {
    double value;
    String unit;

    public JSONVoltage(Voltage measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Voltage toJava() {
        switch (unit) {
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
