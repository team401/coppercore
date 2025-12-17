package coppercore.parameter_tools.json.annotations;

public @interface JsonSubtype {
    Class<?> clazz();

    String name();
}
