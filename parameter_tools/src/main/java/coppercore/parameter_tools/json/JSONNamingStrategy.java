package coppercore.parameter_tools.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import edu.wpi.first.units.Measure;
import java.lang.reflect.Field;
import java.lang.Throwable;

public class JSONNamingStrategy implements FieldNamingStrategy {

    private FieldNamingPolicy policy = FieldNamingPolicy.IDENTITY;
    private JSONSyncConfig config = new JSONSyncConfigBuilder().build();

    public JSONNamingStrategy() {}

    public JSONNamingStrategy(FieldNamingPolicy policy, JSONSyncConfig config) {
        this.policy = policy;
    }

    /**
     * This method returns the name of the field given according to the policy.
     *
     * @return Name
     */
    @Override
    public String translateName(Field field) {
        JSONName annotation = field.getAnnotation(JSONName.class);
        Class<?> type = field.getType();
        if (config.primitiveChecking()) {

            if (type.isPrimitive()
                    && !(Measure.class.isAssignableFrom(field.getDeclaringClass())
                            && field.getDeclaringClass().isRecord())) {
                if (config.primitiveCheckPrintAlert()) {
                    System.out.println(
                        "You used primitive: "
                                    + "Class: "
                                    + field.getDeclaringClass().getName()
                                    + " Field name: "
                                    + field.getName()
                                    + " Type: "
                                    + type.getName()
                                    + " GenericType: "
                                    + field.getGenericType().getTypeName()
                                    + " SuperClass: "
                                    + field.getDeclaringClass().getSuperclass().getName());
                    (new Throwable()).printStackTrace(System.out);
                }
                if (config.primitiveCheckCrash()) {
                    throw new RuntimeException(
                            "You used primitive: "
                                    + "Class: "
                                    + field.getDeclaringClass().getName()
                                    + " Field name: "
                                    + field.getName()
                                    + " Type: "
                                    + type.getName()
                                    + " GenericType: "
                                    + field.getGenericType().getTypeName()
                                    + " SuperClass: "
                                    + field.getDeclaringClass().getSuperclass().getName());
                }
            }
        }
        if (annotation != null) {
            return annotation.value();
        }
        return policy.translateName(field);
    }
}
