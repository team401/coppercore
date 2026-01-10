package coppercore.parameter_tools.json.adapters.measure;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.function.Function;

import coppercore.parameter_tools.json.helpers.JSONObject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Unit;
import edu.wpi.first.units.Units;

/** A JSON representation of a Measure object. */
@SuppressWarnings("rawtypes")
public class JSONMeasure extends JSONObject<Measure> {
    public static HashMap<String, Function<Double, Measure>> unitMap = new HashMap<>();

    public static void registerUnit(String name, Function<Double, Measure> constructor) {
        unitMap.put(name, constructor);
    }

    public static void registerUnit(Unit unit) {
        registerUnit(unit.name(), unit::of);
    }

    public static void registerUnit(Unit unit, String... names) {
        registerUnit(unit);
        for (String name : names) {
            registerUnit(name, unit::of);
        }
    }

    double value = 0.0;
    String unit = "";

    /**
     * Default constructor for JSON serialization.
     *
     * @param measure The Measure object to convert to JSON.
     */
    public JSONMeasure(Measure measure) {
        super(measure);
        value = measure.magnitude();
        unit = measure.unit().name();
        if (unitMap.containsKey(unit) == false) {
            System.out.println("Warning: Unit not registered in map: " + unit);
            System.out.println(
                    """
                -------------------------------------------------------------------------
                To register it, call one of the following methods:
                JSONMeasure.registerUnit(<YOUR UNIT>);
                JSONMeasure.registerUnit(<YOUR UNIT>, "CustomName1", "CustomName2", ...);
                JSONMeasure.registerUnit("CustomName", <Converter to your unit>);

                Note: The unit must be registered before deserializing or serializing any
                Measure of the Unit. You only need to register each unit once, typically
                should be done in a static context.
            """);
        }
    }

    // Written with help from CoPilot
    static {
        // ### Register all units in Units class ###

        // ## Baseless Units ##
        JSONMeasure.registerUnit(Units.Value);
        JSONMeasure.registerUnit(Units.Percent);

        // ## Distance Units ##
        // # Metric #
        JSONMeasure.registerUnit(Units.Meter, "Meter", "Meters");
        JSONMeasure.registerUnit(Units.Millimeter, "Millimeter", "Millimeters");
        JSONMeasure.registerUnit(Units.Centimeter, "Centimeter", "Centimeters");
        // # Imperial #
        JSONMeasure.registerUnit(Units.Inch, "Inch", "Inches");
        JSONMeasure.registerUnit(Units.Foot, "Foot", "Feet");

        // ## Time Units ##
        JSONMeasure.registerUnit(Units.Second, "Second", "Seconds");
        JSONMeasure.registerUnit(Units.Millisecond, "Millisecond", "Milliseconds");
        JSONMeasure.registerUnit(Units.Microsecond, "Microsecond", "Microseconds");
        JSONMeasure.registerUnit(Units.Minute, "Minute", "Minutes");

        // ## Angle Units ##
        JSONMeasure.registerUnit(Units.Radian, "Radian", "Radians");
        JSONMeasure.registerUnit(Units.Revolution, "Revolution", "Revolutions");
        JSONMeasure.registerUnit(Units.Rotation, "Rotation", "Rotations");
        JSONMeasure.registerUnit(Units.Degree, "Degree", "Degrees");

        // ## Velocity Units ##
        // # Linear Velocity #
        JSONMeasure.registerUnit(Units.MetersPerSecond, "MetersPerSecond", "Meters/Second", "m/s");
        JSONMeasure.registerUnit(Units.FeetPerSecond, "FeetPerSecond", "Feet/Second", "ft/s");
        JSONMeasure.registerUnit(Units.InchesPerSecond, "InchesPerSecond", "Inches/Second", "in/s");
        // # Angular Velocity #
        JSONMeasure.registerUnit(
                Units.RevolutionsPerSecond, "RevolutionsPerSecond", "Revolutions/Second", "rev/s");
        JSONMeasure.registerUnit(
                Units.RotationsPerSecond, "RotationsPerSecond", "Rotations/Second", "rot/s");
        JSONMeasure.registerUnit(
                Units.RPM, "RPM", "RevolutionsPerMinute", "Revolutions/Minute", "rev/min", "r/min");
        JSONMeasure.registerUnit(
                Units.RadiansPerSecond, "RadiansPerSecond", "Radians/Second", "rad/s");
        JSONMeasure.registerUnit(
                Units.DegreesPerSecond, "DegreesPerSecond", "Degrees/Second", "deg/s");

        // ## Frequency Units ##
        JSONMeasure.registerUnit(Units.Hertz, "Hertz", "Hertz", "Hz");
        JSONMeasure.registerUnit(Units.Millihertz, "Millihertz", "Millihertz", "mHz");

        // ## Acceleration Units ##
        // # Linear Acceleration #
        JSONMeasure.registerUnit(
                Units.MetersPerSecondPerSecond,
                "MetersPerSecondPerSecond",
                "Meters Per Second Squared",
                "Meters/Second^2",
                "m/s²",
                "m/s^2");
        JSONMeasure.registerUnit(
                Units.FeetPerSecondPerSecond,
                "FeetPerSecondPerSecond",
                "Feet Per Second Squared",
                "Feet/Second^2",
                "ft/s²",
                "ft/s^2");
        // # Angular Acceleration #
        JSONMeasure.registerUnit(
                Units.RotationsPerSecondPerSecond,
                "RotationsPerSecondPerSecond",
                "Rotations Per Second Squared",
                "Rotations/Second^2",
                "rot/s²",
                "rot/s^2");
        JSONMeasure.registerUnit(
                Units.RadiansPerSecondPerSecond,
                "RadiansPerSecondPerSecond",
                "Radians Per Second Squared",
                "Radians/Second^2",
                "rad/s²",
                "rad/s^2");
        JSONMeasure.registerUnit(
                Units.DegreesPerSecondPerSecond,
                "DegreesPerSecondPerSecond",
                "Degrees Per Second Squared",
                "Degrees/Second^2",
                "deg/s²",
                "deg/s^2");
        // # Gs #
        JSONMeasure.registerUnit(Units.Gs, "Gs", "g", "G");

        // ## Mass Units ##
        // # Metric #
        JSONMeasure.registerUnit(Units.Kilogram, "Kilogram", "Kilograms", "kg", "kgs");
        JSONMeasure.registerUnit(Units.Gram, "Gram", "Grams", "g", "gs");
        // # Imperial #
        JSONMeasure.registerUnit(Units.Pound, "Pound", "Pounds", "lb", "lbs");
        JSONMeasure.registerUnit(Units.Ounce, "Ounce", "Ounces", "oz", "ozs");

        // ## Force Units ##
        JSONMeasure.registerUnit(Units.Newton, "Newton", "Newtons", "N");
        JSONMeasure.registerUnit(Units.PoundForce, "PoundForce", "lbf");
        JSONMeasure.registerUnit(Units.OunceForce, "OunceForce", "ozf");

        // ## Torque Units ##
        JSONMeasure.registerUnit(Units.NewtonMeter, "NewtonMeter", "NewtonMeters");
        JSONMeasure.registerUnit(Units.PoundFoot, "PoundFoot", "PoundFeet");
        JSONMeasure.registerUnit(Units.PoundInch, "PoundInch", "PoundInches");
        JSONMeasure.registerUnit(Units.OunceInch, "OunceInch", "OunceInches");

        // ## Momentum##
        // Linear Momentum
        JSONMeasure.registerUnit(
                Units.KilogramMetersPerSecond, "KilogramMetersPerSecond", "KilogramMeters/Second");
        // Angular Momentum
        JSONMeasure.registerUnit(
                Units.KilogramMetersSquaredPerSecond,
                "KilogramMetersSquaredPerSecond",
                "KilogramMeters^2/Second",
                "KilogramMetersSquared/Second");

        // ## Moment of Inertia ##
        JSONMeasure.registerUnit(
                Units.KilogramSquareMeters,
                "KilogramSquareMeters",
                "KilogramMeters^2",
                "KilogramMetersSquared");

        // ## Electric Units ##
        // # Voltage #
        JSONMeasure.registerUnit(Units.Volt, "Volt", "Volts", "V");
        JSONMeasure.registerUnit(Units.Millivolt, "Millivolt", "Millivolts", "mV");
        // # Current #
        JSONMeasure.registerUnit(Units.Amp, "Amp", "Amps", "A");
        JSONMeasure.registerUnit(Units.Milliamp, "Milliamp", "Milliamps", "mA");
        // # Resistance #
        JSONMeasure.registerUnit(Units.Ohm, "Ohm", "Ohms", "Ω");
        JSONMeasure.registerUnit(Units.KiloOhm, "KiloOhm", "KiloOhms", "kΩ");
        JSONMeasure.registerUnit(Units.MilliOhm, "MilliOhm", "MilliOhms", "mΩ");
        // # Energy #
        JSONMeasure.registerUnit(Units.Joule, "Joule", "Joules", "J");
        JSONMeasure.registerUnit(Units.Millijoule, "Millijoule", "Millijoules", "mJ");
        JSONMeasure.registerUnit(Units.Kilojoule, "Kilojoule", "Kilojoules", "kJ");
        // # Power #
        JSONMeasure.registerUnit(Units.Watt, "Watt", "Watts", "W");
        JSONMeasure.registerUnit(Units.Milliwatt, "Milliwatt", "Milliwatts", "mW");
        JSONMeasure.registerUnit(Units.Horsepower, "Horsepower", "HP", "hp");

        // ## Temperature Units ##
        JSONMeasure.registerUnit(Units.Kelvin, "Kelvin", "Kelvins", "K");
        JSONMeasure.registerUnit(Units.Celsius, "Celsius", "°C", "C");
        JSONMeasure.registerUnit(Units.Fahrenheit, "Fahrenheit", "°F", "F");

        // ## Feedforward Units ##
        JSONMeasure.registerUnit(
                Units.VoltsPerMeterPerSecond,
                "V/(m/s)");
        JSONMeasure.registerUnit(
                Units.VoltsPerMeterPerSecondSquared,
                "V/(m/s²)",
                "V/(m/s^2)");
        JSONMeasure.registerUnit(
                Units.VoltsPerRadianPerSecond,
                "V/(rad/s)");
        JSONMeasure.registerUnit(
                Units.VoltsPerRadianPerSecondSquared,
                "V/(rad/s²)",
                "V/(rad/s^2)");
    }

    @Override
    public Measure toJava() {
        if (unitMap.containsKey(unit)) {
            return unitMap.get(unit).apply(value);
        } else {
            throw new RuntimeException("Unit not registered in map: " + unit);
        }
    }

    /**
     * Gets the constructor of the json wrapper
     *
     * @return the json wrapper constructor
     */
    public static Constructor<JSONMeasure> getConstructor() throws NoSuchMethodException {
        return JSONMeasure.class.getConstructor(Measure.class);
    }
}
