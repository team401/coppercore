package coppercore.parameter_tools.json.strategies;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import coppercore.parameter_tools.json.annotations.JSONExclude;

public class JSONExcludeExclusionStrategy implements ExclusionStrategy {
    /** Determines if gson should skip this field. */
    @Override
    public boolean shouldSkipField(FieldAttributes field) {
        return (field.getAnnotation(JSONExclude.class) != null);
    }

    /** Determines if gson should skip this class. */
    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
