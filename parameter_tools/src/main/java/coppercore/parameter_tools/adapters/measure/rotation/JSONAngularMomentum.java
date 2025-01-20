package coppercore.parameter_tools.adapters.measure.rotation;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.AngularMomentum;
import edu.wpi.first.units.Units;

public class JSONAngularMomentum extends JSONObject<AngularMomentum> {
    double value;
    String unit;

    public JSONAngularMomentum(AngularMomentum measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public AngularMomentum toJava() {
        switch (unit){
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
