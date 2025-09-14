package coppercore.parameter_tools.json.helpers;

import coppercore.parameter_tools.json.adapters.JSONPose2d;
import coppercore.parameter_tools.json.adapters.JSONRotation2d;
import coppercore.parameter_tools.json.adapters.JSONTranslation2d;
import coppercore.parameter_tools.json.adapters.measure.JSONMeasure;
import coppercore.parameter_tools.json.adapters.measure.JSONPer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularMomentum;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Energy;
import edu.wpi.first.units.measure.Force;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.LinearAcceleration;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Mass;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Power;
import edu.wpi.first.units.measure.Resistance;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.units.measure.Torque;
import edu.wpi.first.units.measure.Voltage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

// TODO: Make sure ever class that needs a JSON wrapper has one of json measures

/**
 * A utility class for converting between Java classes and their corresponding JSON wrapper classes.
 */
public class JSONConverter {
    /** A map of classes to their corresponding JSONObject wrapper classes. */
    public static final HashMap<Class<?>, Class<? extends JSONObject<?>>> jsonMap = new HashMap<>();

    public static final List<Function<Class<?>, Class<? extends JSONObject<?>>>> advancedJsonMap =
            new ArrayList<>();

    static {
        JSONConverter.addConversion(Translation2d.class, JSONTranslation2d.class);
        JSONConverter.addConversion(Rotation2d.class, JSONRotation2d.class);
        JSONConverter.addConversion(Pose2d.class, JSONPose2d.class);

        jsonMap.put(Distance.class, JSONMeasure.class);
        jsonMap.put(Torque.class, JSONMeasure.class);
        jsonMap.put(Temperature.class, JSONMeasure.class);
        jsonMap.put(Mass.class, JSONMeasure.class);
        jsonMap.put(Force.class, JSONMeasure.class);
        jsonMap.put(Current.class, JSONMeasure.class);
        jsonMap.put(Power.class, JSONMeasure.class);
        jsonMap.put(Energy.class, JSONMeasure.class);
        jsonMap.put(Voltage.class, JSONMeasure.class);
        jsonMap.put(Resistance.class, JSONMeasure.class);
        jsonMap.put(AngularMomentum.class, JSONMeasure.class);
        jsonMap.put(AngularAcceleration.class, JSONMeasure.class);
        jsonMap.put(AngularVelocity.class, JSONMeasure.class);
        jsonMap.put(LinearAcceleration.class, JSONMeasure.class);
        jsonMap.put(LinearVelocity.class, JSONMeasure.class);
        jsonMap.put(Angle.class, JSONMeasure.class);
        jsonMap.put(Time.class, JSONMeasure.class);
        jsonMap.put(MomentOfInertia.class, JSONMeasure.class);
        jsonMap.put(Measure.class, JSONMeasure.class);
        jsonMap.put(Frequency.class, JSONMeasure.class);

        JSONConverter.addConversion(Per.class, JSONPer.class);
    }

    /**
     * Registers a class and its corresponding JSONObject wrapper.
     *
     * @param clazz The class to register.
     * @param jsonClazz The corresponding JSONObject wrapper class.
     */
    public static void addConversion(Class<?> clazz, Class<? extends JSONObject<?>> jsonClazz) {
        jsonMap.put(clazz, jsonClazz);
    }

    /**
     * Registers a function that can provide advanced conversions for classes that may not have a
     * direct mapping.
     */
    public static void addAdvancedConversion(
            Function<Class<?>, Class<? extends JSONObject<?>>> func) {
        advancedJsonMap.add(func);
    }

    /**
     * This method look up the class to see if their is a corresponding JSONObject wrapper.
     *
     * @param <T> The type of the class to look for.
     * @param clazz The class of the wrapper to look for.
     * @return the JSONObject wrapper.
     * @throws ClassCastException If wrapper can not be found
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends JSONObject<T>> convert(Class<T> clazz)
            throws ConversionException {
        try {
            if (jsonMap.containsKey(clazz)) {
                return (Class<? extends JSONObject<T>>) jsonMap.get(clazz);
            }
            return JSONConverter.tryAdvancedConversions(clazz);
        } catch (ClassCastException e) {

            throw new ConversionException("No JsonObject for " + clazz.getName(), e);
        }
    }

    /**
     * Tries to find advanced conversions for classes that may not have a direct mapping.
     *
     * @param <T> The type of the class to look for.
     * @param clazz The class of the wrapper to look for.
     * @return the JSONObject wrapper, or null if none found.
     */
    public static <T> Class<? extends JSONObject<T>> tryAdvancedConversions(Class<T> clazz) {
        for (Function<Class<?>, Class<? extends JSONObject<?>>> func : advancedJsonMap) {
            @SuppressWarnings("unchecked")
            Class<? extends JSONObject<T>> result =
                    (Class<? extends JSONObject<T>>) func.apply(clazz);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /** An exception thrown when a conversion cannot be performed. */
    public static class ConversionException extends Exception {
        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
