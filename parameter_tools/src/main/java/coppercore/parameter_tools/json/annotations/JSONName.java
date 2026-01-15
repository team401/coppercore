package coppercore.parameter_tools.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to have custom name for a field. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JSONName {
    /**
     * Custom name for the field
     *
     * @return name for the field
     */
    String value() default "No name given";
}
