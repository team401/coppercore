package coppercore.parameter_tools.json.adapters.measure;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.measure.Per;

public class JSONPer extends JSONObject<Per> {
    Measure dividend;
    Measure divisor;

    public JSONPer(Per measure) {
        super(measure);
        PerUnit baseUnit = measure.unit();
        dividend = baseUnit.numerator().of(measure.magnitude());
        divisor = baseUnit.denominator().of(1.0);
    }

    @Override
    public Per toJava() {
        return Per.ofBaseUnits(
                dividend.magnitude(), PerUnit.combine(dividend.unit(), divisor.unit()));
    }
}
