package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.MomentOfInertia;

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
        switch (unit) {
            case "KilogramSquareMeters":
                return Units.KilogramSquareMeters.of(value);
            default:
                throw new RuntimeException(
                        unit
                                + " does not exist {For this one I have no idea if I did the units"
                                + " properly}");
        }
    }
}
