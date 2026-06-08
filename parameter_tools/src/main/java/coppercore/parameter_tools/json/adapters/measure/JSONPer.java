package coppercore.parameter_tools.json.adapters.measure;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.Unit;
import edu.wpi.first.units.measure.Per;
import java.lang.reflect.Constructor;

/** A JSON representation of a Per object. */
public class JSONPer<Dividend extends Unit, Divisor extends Unit>
        extends JSONObject<Per<Dividend, Divisor>> {
    Measure<Dividend> dividend;
    Measure<Divisor> divisor;

    /**
     * Default constructor for JSON serialization.
     *
     * @param measure The Per object to convert to JSON.
     */
    @SuppressWarnings("unchecked")
    public JSONPer(Per<Dividend, Divisor> measure) {
        super(measure);
        var baseUnit = measure.unit();
        dividend = (Measure<Dividend>) baseUnit.numerator().of(measure.magnitude());
        divisor = (Measure<Divisor>) baseUnit.denominator().of(1.0);
    }

    @Override
    public Per<Dividend, Divisor> toJava() {
        return Per.ofRelativeUnits(
                dividend.magnitude(), PerUnit.combine(dividend.unit(), divisor.unit()));
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Constructor<JSONPer<? extends Unit, ? extends Unit>> getConstructor()
            throws NoSuchMethodException {
        return (Constructor) JSONPer.class.getConstructor(Per.class);
    }
}
