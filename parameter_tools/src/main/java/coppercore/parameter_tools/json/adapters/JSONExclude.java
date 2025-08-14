package coppercore.parameter_tools.json.adapters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to exclude fields from being serialized or deserialized in JSON.
 *
 * <p>When applied to a field, the field will be ignored during both the serialization and
 * deserialization processes. Useful for marking fields that should not be part of the JSON
 * representation.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * public class Example {
 *     private String includedField;
 *
 *     @JSONExclude
 *     private String excludedField;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JSONExclude {}
