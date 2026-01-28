package coppercore.wpilib_interface.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import coppercore.parameter_tools.json.JSONSyncConfigBuilder;

/**
 * Container and lookup facade for a collection of Controller instances used by the WPILib
 * interface.
 *
 * <p>This class centralizes access to controllers and their control elements (buttons, axes, POVs).
 * It provides lookup operations by index, by physical port, and by logical command name. Typical
 * usage is to construct a Controllers instance with the set of Controller objects to be polled and
 * then use the provided getters to bind application commands to the appropriate control elements.
 *
 * <h2>Backing collection semantics</h2>
 *
 * <ul>
 *   <li>The controllers are stored in a private {@code List<Controller>} field that acts as the
 *       backing collection for all lookup operations.
 *   <li>The no-argument constructor delegates to a constructor that receives a new {@code
 *       ArrayList}, so callers that need to mutate the collection can supply a mutable {@code List}
 *       via the parameterized constructor or replace the field through any exposed mutator (if
 *       provided).
 *   <li>If an immutable list (for example {@code List.of(...)}) is supplied as the backing list,
 *       any attempt to modify that list (outside this class) will throw an {@code
 *       UnsupportedOperationException}.
 *   <li>Concurrent modification of the backing list by multiple threads is not managed by this
 *       class; external synchronization or use of a thread-safe collection is required for safe
 *       concurrent access.
 * </ul>
 *
 * <h3>Lookup semantics and exceptions</h3>
 *
 * <ul>
 *   <li>{@link #getControllerByIndex(int)} delegates to the backing list's {@code get(int)} and
 *       will throw {@link IndexOutOfBoundsException} for invalid indices.
 *   <li>{@link #getControllerByPort(int)} iterates controllers and throws {@link RuntimeException}
 *       when no controller with the requested physical port is found.
 *   <li>Command-to-element lookups (generic {@code getInterface(String)} and typed accessors)
 *       search controllers in insertion order and return the first matching element. If no match is
 *       found, a {@link RuntimeException} is thrown.
 *   <li>Typed accessors ({@code getButton}, {@code getAxis}, {@code getPOV}) perform unchecked
 *       casts from the discovered control element; a {@link ClassCastException} may be thrown if a
 *       controller reports a matching command under a different element type than expected.
 * </ul>
 *
 * <h3>Nullability</h3>
 *
 * <ul>
 *   <li>Behavior for {@code null} arguments (for example a {@code null} command) is governed by the
 *       underlying Controller implementations; callers should avoid passing {@code null} unless
 *       those implementations explicitly support it.
 * </ul>
 *
 * <h3>Thread-safety</h3>
 *
 * <p>This class is not thread-safe. If multiple threads will read or modify the controllers
 * collection or perform lookups concurrently, callers must provide external synchronization or use
 * a concurrent collection as the backing list.
 *
 * <h3>Example</h3>
 *
 * <p>Typical usage will look like:
 *
 * <pre>{@code
 * // construct with a mutable list of controllers
 * Controllers controllers = new Controllers(new ArrayList<>(/* controllers * /));
 * Controller.Button shoot = controllers.getButton("shoot");
 * }</pre>
 *
 * Implementation Note: Implementations that wrap this class should take care to expose only the
 * intended mutation and lifecycle operations (registration, deregistration, cleanup) and avoid
 * exposing the backing list directly unless copy/defensive measures are taken.
 */
public class Controllers {

    /**
     * The collection of Controller instances managed by this class.
     *
     * <p>This field contains the controllers that the WPILib interface will poll and forward input
     * from. It is initialized to an empty, immutable list (created with List.of()). Because the
     * list is immutable, callers that need to change the set of controllers should replace this
     * field with a new List instance (for example via a setter or builder) rather than attempting
     * in-place modification, which would throw UnsupportedOperationException.
     *
     * <p>Keep this field private to ensure controller lifecycle and access semantics are enforced
     * by the owning class (e.g., registration, deregistration, and resource cleanup).
     */
    private List<Controller> controllers = List.of();

    /**
     * Creates a new Controllers instance initialized with an empty collection of controllers.
     *
     * <p>This no-argument constructor delegates to the parameterized constructor, supplying a newly
     * created, empty ArrayList as the initial list. Use this constructor when you need an empty
     * Controllers object that will be populated later.
     *
     * @see #Controllers(java.util.List)
     */
    public Controllers() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new Controllers container that holds the supplied list of Controller instances. The
     * provided list is used directly as the backing collection for this object; callers should
     * avoid modifying the list concurrently.
     *
     * @param controllers the list of Controller objects to be managed by this instance
     */
    public Controllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     * Get controller by index
     *
     * @param index The index of the controller
     * @return The controller at the given index
     */
    public Controller getControllerByIndex(int index) {
        return controllers.get(index);
    }

    /**
     * Get controller by port
     *
     * @param port The port of the controller
     * @return The controller at the given port
     */
    public Controller getControllerByPort(int port) {
        for (Controller controller : controllers) {
            if (controller.port == port) {
                return controller;
            }
        }
        throw new RuntimeException("No controller found on port: " + port);
    }

    /**
     * Find and return the first matching control element for a given command.
     *
     * @param pred Predicate to apply to (Controller, Command)
     * @param getter Function to get the element from the controller
     * @param command The command to get the interface for
     * @return The control element
     */
    private Controller.ControlElement findFirstMatchingControlElement(
            BiFunction<Controller, String, Boolean> matchPred,
            BiFunction<Controller, String, Controller.ControlElement> getter,
            String command) {
        for (Controller controller : controllers) {
            if (matchPred.apply(controller, command)) {
                return getter.apply(controller, command);
            }
        }
        throw new RuntimeException("No control element found for command: " + command);
    }

    /**
     * Get controller element by command.
     *
     * @param command The command to get the element for
     * @return The first matching controller element.
     */
    public Controller.ControlElement getInterface(String command) {
        return findFirstMatchingControlElement(
                (controller, cmd) -> controller.hasControlElement(cmd),
                (controller, cmd) -> controller.getControlElement(cmd),
                command);
    }

    /**
     * Get button by command.
     *
     * @param command The command to get the button for
     * @return The first matching button
     */
    public Controller.Button getButton(String command) {
        return (Controller.Button)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasButton(cmd),
                        (controller, cmd) -> controller.getButton(cmd),
                        command);
    }

    /**
     * Get axis by command
     *
     * @param command The command to get the axis for
     * @return The first matching axis.
     */
    public Controller.Axis getAxis(String command) {
        return (Controller.Axis)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasAxis(cmd),
                        (controller, cmd) -> controller.getAxis(cmd),
                        command);
    }

    /**
     * Get POV by command
     *
     * @param command The command to get the POV for
     * @return The first matching POV.
     */
    public Controller.POV getPOV(String command) {
        return (Controller.POV)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasPOV(cmd),
                        (controller, cmd) -> controller.getPOV(cmd),
                        command);
    }

    /**
     * Configures the provided JSONSyncConfigBuilder with the polymorphic adapters
     * required for controller-related types.
     *
     * This method registers adapters for Controller.ControlElement and
     * Controller.LowLevelControlElement so that instances of those types can be
     * correctly serialized and deserialized by the configured JSON sync system.
     *
     * The supplied builder is modified in-place and returned to allow fluent use.
     *
     * @param builder the JSONSyncConfigBuilder to configure; must not be {@code null}
     * @return the same {@code JSONSyncConfigBuilder} instance after configuration
     * @throws NullPointerException if {@code builder} is {@code null}
     */
    public static JSONSyncConfigBuilder applyControllerConfigToBuilder(JSONSyncConfigBuilder builder) {
        return builder
            .setUpPolymorphAdapter(Controller.ControlElement.class)
            .setUpPolymorphAdapter(Controller.LowLevelControlElement.class);
    }

    /**
     * Creates and returns a JSONSyncConfigBuilder pre-configured with controller settings.
     *
     * <p>This method instantiates a new {@code JSONSyncConfigBuilder} and applies the
     * controller-specific configuration to it via {@code applyControllerConfigToBuilder(...)}.
     * The returned builder is ready for further customization by the caller prior to building
     * the final configuration.</p>
     *
     * @return a new {@code JSONSyncConfigBuilder} with controller configuration applied; never {@code null}
     */
    public static JSONSyncConfigBuilder getControllerJsonSyncConfigBuilder() {
        return applyControllerConfigToBuilder(new JSONSyncConfigBuilder());
    }
}
