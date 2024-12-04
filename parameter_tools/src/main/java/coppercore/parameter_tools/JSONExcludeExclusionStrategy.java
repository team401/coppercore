package coppercore.parameter_tools;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class JSONExcludeExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes field) {
        return (field.getAnnotation(JSONExclude.class) != null);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
