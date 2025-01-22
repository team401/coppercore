package coppercore.parameter_tools.json;

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
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Energy;
import edu.wpi.first.units.measure.Force;
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

import java.util.HashMap;

public class JSONConverter {
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
        jsonMap.put(LinearAcceleration.class, JSONMeasure.class);
        jsonMap.put(LinearVelocity.class, JSONMeasure.class);
        jsonMap.put(Angle.class, JSONMeasure.class);
        jsonMap.put(Time.class, JSONMeasure.class);
        jsonMap.put(MomentOfInertia.class, JSONMeasure.class);
        jsonMap.put(Measure.class, JSONMeasure.class);
        jsonMap.put(Per.class, JSONPer.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends JSONObject<T>> convert(Class<T> clazz) {
        System.out.println(clazz);
        try {
            return (Class<? extends JSONObject<T>>) jsonMap.get(clazz);
        } catch (ClassCastException e) {
            System.out.println("Error");
            System.err.println("No JsonObject for " + clazz.getName());
            throw e;
        }
    }

    public static <T> boolean has(Class<T> clazz) {
        return jsonMap.containsKey(clazz);
    }
}
