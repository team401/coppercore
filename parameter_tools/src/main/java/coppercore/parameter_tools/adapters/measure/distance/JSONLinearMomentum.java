package coppercore.parameter_tools.adapters.measure.distance;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.LinearMomentum;
import edu.wpi.first.units.Units;

public class JSONLinearMomentum extends JSONObject<LinearMomentum> {
    double value;
    String unit;

    public JSONLinearMomentum(LinearMomentum measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public LinearMomentum toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
