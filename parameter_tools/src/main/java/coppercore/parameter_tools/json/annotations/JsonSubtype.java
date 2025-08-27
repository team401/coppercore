package coppercore.parameter_tools.json.annotations;

/**
 * Annotation to define a subtype for use with {@link JsonType} for polymorphic deserialization. Use
 * will need to use JSONSyncConfigBuilder should be used to register this with the JSONSyncConfig to
 * use.
 */
public @interface JsonSubtype {

    /**
     * Class of the subtype
     *
     * @return class of the type
     */
    Class<?> clazz();

    /**
     * Name of the subtype as it appears in the JSON'
     *
     * @return name of the subtype
     */
    String name();
}
