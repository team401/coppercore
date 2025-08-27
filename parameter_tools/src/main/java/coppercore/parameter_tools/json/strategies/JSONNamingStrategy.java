package coppercore.parameter_tools.json.strategies;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import coppercore.parameter_tools.json.JSONSyncConfig;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONName;
import java.lang.reflect.Field;

/**
 * This class implements a naming strategy for JSON serialization and deserialization using Gson. It
 * allows for custom field naming through the use of the @JSONName annotation. If the annotation is
 * not present, it falls back to a specified FieldNamingPolicy.
 */
public class JSONNamingStrategy implements FieldNamingStrategy {

    protected FieldNamingPolicy policy = FieldNamingPolicy.IDENTITY;
    protected JSONSyncConfig config = new JSONSyncConfigBuilder().build();

    /**
     * Default constructor for JSONNamingStrategy, its fall back policy will be {@link
     * FieldNamingPolicy#IDENTITY}
     */
    public JSONNamingStrategy() {}

    /**
     * Constructor for JSONNamingStrategy.
     *
     * @param policy the fall back policy
     * @param config the JSONSyncConfig to use for serialization and deserialization
     */
    public JSONNamingStrategy(FieldNamingPolicy policy, JSONSyncConfig config) {
        this.policy = policy;
        this.config = config;
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
