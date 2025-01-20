package coppercore.parameter_tools.TypeAdapters;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.Units;


public class JSONAngle extends JSONObject<Angle> {
    double value;
    String unit;

    public JSONAngle(Angle measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Angle toJava() {
        switch (unit){
            case "Radian":
                return Units.Radian.of(value);
            case "Revolution":
                return Units.Revolution.of(value);
            case "Rotation":
                return Units.Rotation.of(value);
            case "Degree":
                return Units.Degree.of(value);
            default:
                throw new RuntimeException(unit+" does not exist");
        }
    }
}
