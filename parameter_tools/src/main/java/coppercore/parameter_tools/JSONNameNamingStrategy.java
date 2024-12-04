package coppercore.parameter_tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import java.lang.reflect.Field;

public class JSONNameNamingStrategy implements FieldNamingStrategy {

    private FieldNamingPolicy policy = FieldNamingPolicy.IDENTITY;

    public JSONNameNamingStrategy() {}

    public JSONNameNamingStrategy(FieldNamingPolicy policy) {
        this.policy = policy;
    }

    @Override
    public String translateName(Field field) {
        JSONName annotation = field.getAnnotation(JSONName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return policy.translateName(field);
    }
}
