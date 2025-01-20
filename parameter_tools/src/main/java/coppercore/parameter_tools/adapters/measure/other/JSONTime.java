package coppercore.parameter_tools.adapters.measure.other;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Time;

public class JSONTime extends JSONObject<Time> {
    double value;
    String unit;

    public JSONTime(Time measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Time toJava() {
        switch (unit) {
            case "Second":
                return Units.Second.of(value);
            case "Millisecond":
                return Units.Milliseconds.of(value);
            case "Microsecond":
                return Units.Microseconds.of(value);
            case "Minute":
                return Units.Minutes.of(value);
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
