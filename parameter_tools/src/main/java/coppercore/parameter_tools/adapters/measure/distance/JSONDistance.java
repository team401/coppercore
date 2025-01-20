package coppercore.parameter_tools.adapters.measure.distance;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;

public class JSONDistance extends JSONObject<Distance> {
    double value;
    String unit;

    public JSONDistance(Distance distance) {
        super(distance);
        value = distance.magnitude();
        unit = distance.unit().name();
    }

    @Override
    public Distance toJava() {
        switch (unit) {
            case "Meter":
                return Units.Meters.of(value);
            case "Millimeter":
                return Units.Millimeters.of(value);
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
