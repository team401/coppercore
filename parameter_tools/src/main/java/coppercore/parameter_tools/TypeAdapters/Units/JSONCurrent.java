package coppercore.parameter_tools.TypeAdapters.Units;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.Units;


public class JSONCurrent extends JSONObject<Current> {
    double value;
    String unit;

    public JSONCurrent(Current measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Current toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
