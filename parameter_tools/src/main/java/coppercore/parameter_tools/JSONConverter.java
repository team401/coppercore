package coppercore.parameter_tools;

import coppercore.parameter_tools.adapters.JSONPose2d;
import coppercore.parameter_tools.adapters.JSONRotation2d;
import coppercore.parameter_tools.adapters.JSONTranslation2d;
import coppercore.parameter_tools.adapters.measure.JSONPer;
import coppercore.parameter_tools.adapters.measure.other.JSONTime;
import coppercore.parameter_tools.adapters.measure.rotation.JSONAngle;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Time;
import java.util.HashMap;

public class JSONConverter {
    public static final HashMap<Class<?>, Class<? extends JSONObject<?>>> jsonMap = new HashMap<>();

    static {
        jsonMap.put(Translation2d.class, JSONTranslation2d.class);
        jsonMap.put(Rotation2d.class, JSONRotation2d.class);
        jsonMap.put(Pose2d.class, JSONPose2d.class);
        jsonMap.put(Time.class, JSONTime.class);
        jsonMap.put(Angle.class, JSONAngle.class);
        jsonMap.put(Per.class, JSONPer.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends JSONObject<T>> convert(Class<T> clazz) {
        try {
            return (Class<? extends JSONObject<T>>) jsonMap.get(clazz);
        } catch (ClassCastException e) {
            System.err.println("No JsonObject for " + clazz.getName());
            throw e;
        }
    }

    public static <T> boolean has(Class<T> clazz) {
        return jsonMap.containsKey(clazz);
    }
}
