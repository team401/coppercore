package coppercore.parameter_tools.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a JSON type with subtypes for polymorphic deserialization. {@link
 * JsonSubtype} should be used to define the subtypes. Use will need to use should be used to
 * register this with the to use.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonType {

    /**
     * Name of the type field in the JSON object
     *
     * @return name of the field
     */
    String property();

    /**
     * Array of subtypes for the annotated type
     *
     * @return Array of subtypes
     */
    JsonSubtype[] subtypes();
}
