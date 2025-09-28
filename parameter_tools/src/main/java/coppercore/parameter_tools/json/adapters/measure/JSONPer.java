package coppercore.parameter_tools.json.adapters.measure;

import java.lang.reflect.Constructor;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.measure.Per;

/** A JSON representation of a Per object. */
public class JSONPer extends JSONObject<Per> {
    Measure dividend;
    Measure divisor;

    /**
     * Default constructor for JSON serialization.
     *
     * @param measure The Per object to convert to JSON.
     */
    public JSONPer(Per measure) {
        super(measure);
        PerUnit baseUnit = measure.unit();
        dividend = baseUnit.numerator().of(measure.magnitude());
        divisor = baseUnit.denominator().of(1.0);
    }

    @Override
    public Per toJava() {
        return Per.ofRelativeUnits(
                dividend.magnitude(), PerUnit.combine(dividend.unit(), divisor.unit()));
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONPer> getConstructor() throws NoSuchMethodException {
        return JSONPer.class.getConstructor(Per.class);
    }
}
