package coppercore.parameter_tools.json.helpers;

import java.util.HashMap;

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

//TODO: Make sure ever class that needs a JSON wrapper has one of json measures

/**
 * A utility class for converting between Java classes and their corresponding JSON wrapper classes.
 */
public class JSONConverter {
    /** A map of classes to their corresponding JSONObject wrapper classes. */
    public static final HashMap<Class<?>, Class<? extends JSONObject<?>>> jsonMap = new HashMap<>();

    static {
        jsonMap.put(Translation2d.class, JSONTranslation2d.class);
        jsonMap.put(Rotation2d.class, JSONRotation2d.class);
        jsonMap.put(Pose2d.class, JSONPose2d.class);

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

        jsonMap.put(Per.class, JSONPer.class);
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
     * This method look up the class to see if their is a corresponding JSONObject wrapper.
     *
     * @param <T> The type of the class to look for.
     * @param clazz The class of the wrapper to look for.
     * @return the JSONObject wrapper.
     * @throws ClassCastException If wrapper can not be found
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends JSONObject<T>> convert(Class<T> clazz) {
        try {
            return (Class<? extends JSONObject<T>>) jsonMap.get(clazz);
        } catch (ClassCastException e) {
            System.out.println("Error");
            System.err.println("No JsonObject for " + clazz.getName());
            throw e;
        }
    }

    /**
     * Checks if a class has a registered JSONObject wrapper.
     *
     * @param <T> The type of the class to check.
     * @param clazz The class to check for a wrapper.
     * @return if the class has a registered JSONObject wrapper.
     */
    public static <T> boolean has(Class<T> clazz) {
        return jsonMap.containsKey(clazz);
    }
}
