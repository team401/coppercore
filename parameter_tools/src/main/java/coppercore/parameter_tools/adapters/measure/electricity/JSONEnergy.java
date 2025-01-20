package coppercore.parameter_tools.adapters.measure.electricity;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Energy;
import edu.wpi.first.units.Units;

public class JSONEnergy extends JSONObject<Energy> {
    double value;
    String unit;

    public JSONEnergy(Energy measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Energy toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
