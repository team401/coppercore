package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.units.Units;

public class JSONMomentOfInertia extends JSONObject<MomentOfInertia> {
    double value;
    String unit;

    public JSONMomentOfInertia(MomentOfInertia measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public MomentOfInertia toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
