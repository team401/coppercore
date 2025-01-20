package coppercore.parameter_tools.adapters.measure.electricity;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Power;
import edu.wpi.first.units.Units;

public class JSONPower extends JSONObject<Power> {
    double value;
    String unit;

    public JSONPower(Power measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Power toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
