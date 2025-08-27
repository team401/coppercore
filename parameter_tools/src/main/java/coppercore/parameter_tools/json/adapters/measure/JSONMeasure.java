package coppercore.parameter_tools.json.adapters.measure;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Units;

/** A JSON representation of a Measure object. */
@SuppressWarnings("rawtypes")
public class JSONMeasure extends JSONObject<Measure> {
    double value = 0.0;
    String unit = "";

    /**
     * Default constructor for JSON deserialization.
     *
     * @param measure The Measure object to convert to JSON.
     */
    public JSONMeasure(Measure measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
    }

    @Override
    public Measure toJava() {
        switch (unit) {
            case "Meter":
                return Units.Meter.of(value);
            case "Millimeter":
                return Units.Millimeter.of(value);
            case "Centimeter":
                return Units.Centimeter.of(value);
            case "Inch":
                return Units.Inch.of(value);
            case "Foot":
                return Units.Foot.of(value);
            case "Second":
                return Units.Second.of(value);
            case "Millisecond":
                return Units.Millisecond.of(value);
            case "Microsecond":
                return Units.Microsecond.of(value);
            case "Minute":
                return Units.Minute.of(value);
            case "Radian":
                return Units.Radian.of(value);
            case "Revolution":
                return Units.Revolution.of(value);
            case "Rotation":
                return Units.Rotation.of(value);
            case "Degree":
                return Units.Degree.of(value);
            case "MetersPerSecond":
                return Units.MetersPerSecond.of(value);
            case "FeetPerSecond":
                return Units.FeetPerSecond.of(value);
            case "InchesPerSecond":
                return Units.InchesPerSecond.of(value);
            case "RevolutionsPerSecond":
                return Units.RevolutionsPerSecond.of(value);
            case "RotationsPerSecond":
                return Units.RotationsPerSecond.of(value);
            case "RPM":
                return Units.RPM.of(value);
            case "RadiansPerSecond":
                return Units.RadiansPerSecond.of(value);
            case "DegreesPerSecond":
                return Units.DegreesPerSecond.of(value);
            case "Hertz":
                return Units.Hertz.of(value);
            case "Millihertz":
                return Units.Millihertz.of(value);
            case "MetersPerSecondPerSecond":
                return Units.MetersPerSecondPerSecond.of(value);
            case "FeetPerSecondPerSecond":
                return Units.FeetPerSecondPerSecond.of(value);
            case "RotationsPerSecondPerSecond":
                return Units.RotationsPerSecondPerSecond.of(value);
            case "RadiansPerSecondPerSecond":
                return Units.RadiansPerSecondPerSecond.of(value);
            case "DegreesPerSecondPerSecond":
                return Units.DegreesPerSecondPerSecond.of(value);
            case "Gs":
                return Units.Gs.of(value);
            case "Kilogram":
                return Units.Kilogram.of(value);
            case "Gram":
                return Units.Gram.of(value);
            case "Pound":
                return Units.Pound.of(value);
            case "Ounce":
                return Units.Ounce.of(value);
            case "Newton":
                return Units.Newton.of(value);
            case "PoundForce":
                return Units.PoundForce.of(value);
            case "OunceForce":
                return Units.OunceForce.of(value);
            case "NewtonMeter":
                return Units.NewtonMeter.of(value);
            case "PoundFoot":
                return Units.PoundFoot.of(value);
            case "PoundInch":
                return Units.PoundInch.of(value);
            case "OunceInch":
                return Units.OunceInch.of(value);
            case "KilogramMetersPerSecond":
                return Units.KilogramMetersPerSecond.of(value);
            case "KilogramMetersSquaredPerSecond":
                return Units.KilogramMetersSquaredPerSecond.of(value);
            case "KilogramSquareMeters":
                return Units.KilogramSquareMeters.of(value);
            case "Volt":
                return Units.Volt.of(value);
            case "Millivolt":
                return Units.Millivolt.of(value);
            case "Amp":
                return Units.Amp.of(value);
            case "Milliamp":
                return Units.Milliamp.of(value);
            case "Ohm":
                return Units.Ohm.of(value);
            case "KiloOhm":
                return Units.KiloOhm.of(value);
            case "MilliOhm":
                return Units.MilliOhm.of(value);
            case "Joule":
                return Units.Joule.of(value);
            case "Millijoule":
                return Units.Millijoule.of(value);
            case "Kilojoule":
                return Units.Kilojoule.of(value);
            case "Watt":
                return Units.Watt.of(value);
            case "Milliwatt":
                return Units.Milliwatt.of(value);
            case "Horsepower":
                return Units.Horsepower.of(value);
            case "Kelvin":
                return Units.Kelvin.of(value);
            case "Celsius":
                return Units.Celsius.of(value);
            case "Fahrenheit":
                return Units.Fahrenheit.of(value);
            case "VoltsPerMeterPerSecond":
                return Units.VoltsPerMeterPerSecond.of(value);
            case "VoltsPerMeterPerSecondSquared":
                return Units.VoltsPerMeterPerSecondSquared.of(value);
            case "VoltsPerRadianPerSecond":
                return Units.VoltsPerRadianPerSecond.of(value);
            case "VoltsPerRadianPerSecondSquared":
                return Units.VoltsPerRadianPerSecondSquared.of(value);
            default:
                throw new RuntimeException(unit + " does not exist");
        }
    }
}
