package coppercore.parameter_tools.adapters.units.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.Units;


public class JSONTemperature extends JSONObject<Temperature> {
    double value;
    String unit;

    public JSONTemperature(Temperature measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Temperature toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
