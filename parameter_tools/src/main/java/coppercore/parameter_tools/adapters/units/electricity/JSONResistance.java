package coppercore.parameter_tools.adapters.units.electricity;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Resistance;
import edu.wpi.first.units.Units;


public class JSONResistance extends JSONObject<Resistance> {
    double value;
    String unit;

    public JSONResistance(Resistance measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Resistance toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
