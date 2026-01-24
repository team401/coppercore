package coppercore.parameter_tools.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Javadoc written by Copilot based on user description

/**
 * Indicates that the annotated method should be invoked immediately after an object has been
 * deserialized from JSON.
 *
 * <p>Purpose
 *
 * <ul>
 *   <li>Perform any per-instance initialization, validation, or computation of derived fields that
 *       must run after JSON field population.
 * </ul>
 *
 * <p>Contract (common expectations)
 *
 * <ul>
 *   <li>Apply to at most one method per class.
 *   <li>The method is typically an instance method (not static), takes no parameters, and returns
 *       void.
 * </ul>
 *
 * <p>Behavior
 *
 * <ul>
 *   <li>The method is called for each deserialized instance right after its fields are populated
 *       (not when the class is loaded).
 *   <li>Allows for custom logic that depends on the fully populated state of the object.
 * </ul>
 *
 * <p>Example Usage
 *
 * <ul>
 *   <li>Annotate a method that computes derived fields based on JSON-loaded data. - Use for
 *       validation checks that depend on multiple fields being set.
 *   <li>Initialize transient fields that are not part of the JSON representation.
 *   <li>Set up internal data structures that rely on the loaded state.
 *   <li>Perform logging or debugging actions after loading.
 *   <li>Integrate with other systems that need to be notified of the new state.
 *   <li>Ensure that any invariants required by the class are maintained after loading.
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterJsonLoad {}
