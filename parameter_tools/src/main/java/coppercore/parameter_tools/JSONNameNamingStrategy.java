package coppercore.parameter_tools;

import java.lang.reflect.Field;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;

public class JSONNameNamingStrategy implements FieldNamingStrategy {

    private FieldNamingPolicy policy = FieldNamingPolicy.IDENTITY;

    public JSONNameNamingStrategy() {}
    
    public JSONNameNamingStrategy(FieldNamingPolicy policy) {
        this.policy = policy;
    }

    /**
     * This method returns the name of the field given according to the policy.
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
