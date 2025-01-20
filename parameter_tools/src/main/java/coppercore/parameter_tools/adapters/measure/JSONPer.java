package coppercore.parameter_tools.adapters.measure;

import coppercore.parameter_tools.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Time;

public class JSONPer extends JSONObject<Per> {
    JSONMeasure dividend;
    JSONMeasure divisor;

    public JSONPer(Per measure) {
        super(measure);
        PerUnit baseUnit = measure.unit();
        Measure dividend = baseUnit.numerator().of(measure.magnitude());
        Measure divisor = baseUnit.denominator().of(1.0);
        
    }

    @Override
    public Per toJava() {
        return Per.ofBaseUnits(
                dividend.toJava().magnitude(), PerUnit.combine(dividend.toJava().unit(), divisor.toJava().unit()));
    }
}
