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
import edu.wpi.first.units.measure.Per;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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

        // All measure are wrapped by the JSONMeasure class except Per
        // Per is handled separately below because it needs its own wrapper
        // And cant be handled by the generic measure wrapper
        JSONConverter.addAdvancedConversion(
                (Class<?> clazz) -> {
                    if (Measure.class.isAssignableFrom(clazz)) {
                        return JSONMeasure.class;
                    }
                    return null;
                });

        // Filter out Per from the rest of the measures
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
     * Also used for complex rules that can not be easily represented in a map.
     * These functions are tried after the basic map look up fails.
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
            Class<?> advancedClazz = JSONConverter.tryAdvancedConversions(clazz);
            if (advancedClazz != null) {
                return (Class<? extends JSONObject<T>>) advancedClazz;
            }
        } catch (ClassCastException e) {
            throw new ConversionException("No JsonObject for " + clazz.getName(), e);
        }
        throw new ConversionException("No JsonObject for " + clazz.getName());
    }

    /**
     * Tries to find advanced conversions for classes that may not have a direct mapping.
     * Or for complex rules that can not be easily represented in a map.
     * Advanced conversions are tried after the basic map look up fails.
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
