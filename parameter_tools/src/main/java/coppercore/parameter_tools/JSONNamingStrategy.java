package coppercore.parameter_tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import java.lang.reflect.Field;

public class JSONNamingStrategy implements FieldNamingStrategy {

    private FieldNamingPolicy policy = FieldNamingPolicy.IDENTITY;

    public JSONNamingStrategy() {}

    public JSONNamingStrategy(FieldNamingPolicy policy) {
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
        if (annotation != null) {
            return annotation.value();
        }
        return policy.translateName(field);
    }
}
