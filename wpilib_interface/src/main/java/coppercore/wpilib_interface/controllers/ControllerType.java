package coppercore.wpilib_interface.controllers;

import java.util.HashMap;

// Maybe add more info to this later to allow defining more complex controller types
// And maybe define custom axis/button values here too guarantee compatibility
/**
 * Represents a type of input controller (gamepad, joystick, etc.) and provides a simple registry
 * for looking up controller type definitions by name.
 *
 * <p>A ControllerType encapsulates human-readable metadata and shorthand mappings for controller
 * controls:
 *
 * <ul>
 *   <li>{@code name} — a display name for the controller type.
 *   <li>{@code AxisShorthands} — a mapping from shorthand axis names (e.g. "x", "leftX") to their
 *       integer axis indices.
 *   <li>{@code ButtonShorthands} — a mapping from shorthand button names (e.g. "a", "trigger") to
 *       their integer button indices.
 *   <li>{@code POVShorthands} — a mapping for POV/d-pad shorthand names to their indices.
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * // Create and register a custom controller type
 * ControllerType myType = ControllerType.createSimpleControllerType("myPad")
 *     .addButtonShorthand("fire", 1)
 *     .addAxisShorthand("steer", 0);
 * ControllerType.registerControllerType("myPad", myType);
 *
 * // Lookup a registered controller type
 * ControllerType t = ControllerType.getControllerType("myPad");
 * int fireButton = t.ButtonShorthands.get("fire");
 * </pre>
 *
 * Thread-safety and mutability:
 *
 * <ul>
 *   <li>The registry (static {@code controllerTypes}) is a plain {@code HashMap} and is not
 *       thread-safe. Concurrent registration or lookup from multiple threads may cause race
 *       conditions. If concurrent access is required, replace the registry with a {@code
 *       ConcurrentHashMap} or otherwise synchronize access.
 *   <li>Instances of ControllerType are mutable: shorthand maps can be modified after creation via
 *       {@code addButtonShorthand}, {@code addAxisShorthand}, and {@code addPOVShorthand}. If
 *       immutability is desired, consider wrapping or copying the maps before exposing them.
 * </ul>
 *
 * Error handling:
 *
 * <ul>
 *   <li>{@link #getControllerType(String)} throws a {@link RuntimeException} when the requested
 *       name is not registered. Callers should handle or avoid this by checking registration first
 *       or using a registration policy that ensures expected names are present at startup.
 * </ul>
 *
 * Design notes:
 *
 * <ul>
 *   <li>The constructor is {@code protected} to encourage construction through the provided factory
 *       {@link #createSimpleControllerType(String)} or custom subclasses.
 *   <li>The static initializer registers several common controller types ("joystick", "xbox",
 *       "ps4", "ps5", "stadia") with typical shorthand mappings. These serve as examples and
 *       defaults and can be augmented or overridden by the application.
 * </ul>
 */
public class ControllerType {

    /**
     * Registry mapping string identifiers to ControllerType instances.
     *
     * <p>This static map serves as a central lookup for available controller types, where the key
     * is a unique string (for example, a controller name or ID) and the value is the corresponding
     * ControllerType object. The map is initialized empty and is intended to be populated at
     * application startup or when new controller types are registered.
     *
     * <p>Note: this field is not thread-safe. If controller registration or lookup may occur from
     * multiple threads, consider replacing the map with a ConcurrentHashMap or otherwise
     * synchronizing access to avoid race conditions.
     */
    private static HashMap<String, ControllerType> controllerTypes = new HashMap<>();

    /**
     * Human-readable display name for this controller type.
     *
     * <p>Used for user interfaces, logging, and debugging to identify the specific kind of
     * controller. May be null or empty if no name has been provided.
     */
    public String name;

    /**
     * Maps shorthand axis names to their corresponding integer axis indices.
     *
     * <p>Keys are short, human-readable axis identifiers (for example "X", "Y", "LeftTrigger",
     * etc.). Values are the integer axis IDs used by the underlying controller/joystick API.
     *
     * <p>The map is mutable and intended to be populated at initialization or updated at runtime to
     * reflect controller-specific mappings. Callers should treat the map as potentially absent of a
     * given key and perform appropriate null or containsKey checks before use.
     *
     * <p>Thread-safety is not guaranteed by this field; if the map may be read or modified
     * concurrently, synchronize access externally or use a concurrent map implementation.
     */
    public HashMap<String, Integer> AxisShorthands;

    /**
     * Maps button shorthand names to their corresponding integer button codes.
     *
     * <p>Each entry associates a concise, human-readable shorthand (for example "A", "B", "LB",
     * "RT") with the numeric identifier used by the underlying input system. This map is intended
     * to be populated during controller initialization and queried by input handling logic to
     * resolve shorthand labels into button IDs.
     *
     * <p>The map is mutable; callers should not assume immutability. If concurrent access is
     * possible, external synchronization should be used.
     *
     * <p>Keys are expected to be non-null shorthand strings (case-sensitive). Values are the
     * integer button codes; null values are not expected.
     *
     * @since 1.0
     * @see java.util.HashMap
     */
    public HashMap<String, Integer> ButtonShorthands;

    /**
     * A mapping of human-readable POV (point-of-view / D-pad) shorthand names to their integer
     * identifiers.
     *
     * <p>Keys are shorthand strings (for example "UP", "DOWN", "LEFT", "RIGHT" or any
     * project-defined label) and values are the integer codes used by the underlying input system
     * to identify that POV direction. Keys are treated as case-sensitive unless normalized by the
     * surrounding code.
     *
     * <p>This field is mutable and modifications will affect all references to the map. The map
     * instance may be null until the class is initialized; callers should check for null or use
     * defensive copying if they need thread-safety or immutability. To expose an unmodifiable view,
     * wrap this map with Collections.unmodifiableMap(...).
     *
     * <p>Usage notes:
     *
     * <ul>
     *   <li>Check containsKey(key) or handle null results from get(key) when retrieving values.
     *   <li>Synchronize externally if the map is accessed concurrently from multiple threads.
     *   <li>Document and standardize any expected shorthand keys to avoid mismatches across code.
     * </ul>
     */
    public HashMap<String, Integer> POVShorthands;

    /**
     * Protected constructor
     *
     * @param name The name of the controller type
     */
    protected ControllerType(String name) {
        this.name = name;
        this.AxisShorthands = new HashMap<>();
        this.ButtonShorthands = new HashMap<>();
        this.POVShorthands = new HashMap<>();
    }

    /**
     * Register a controller type with a simple constructor function
     *
     * @param name The name of the controller type
     */
    public static void registerControllerType(String name) {
        registerControllerType(name, ControllerType.createSimpleControllerType(name));
    }

    /**
     * Register a controller type
     *
     * @param name The name of the controller type
     * @param type The controller type
     */
    public static void registerControllerType(String name, ControllerType type) {
        controllerTypes.put(name, type);
    }

    /**
     * Get a registered controller type
     *
     * @param name The name of the controller type
     * @return The controller type
     */
    public static ControllerType getControllerType(String name) {
        ControllerType type = controllerTypes.get(name);
        if (type == null) {
            throw new RuntimeException("Controller type not registered: " + name);
        }
        return type;
    }

    /**
     * Create a simple controller type with no shorthands
     *
     * @param name The name of the controller type
     * @return The controller type
     */
    public static ControllerType createSimpleControllerType(String name) {
        return new ControllerType(name);
    }

    /**
     * Add a button shorthand
     *
     * @param shorthand The shorthand name
     * @param id The button id
     * @return The controller type
     */
    public ControllerType addButtonShorthand(String shorthand, int id) {
        ButtonShorthands.put(shorthand, id);
        return this;
    }

    /**
     * Add an axis shorthand
     *
     * @param shorthand The shorthand name
     * @param id The axis id
     * @return The controller type
     */
    public ControllerType addAxisShorthand(String shorthand, int id) {
        AxisShorthands.put(shorthand, id);
        return this;
    }

    /**
     * Add a POV shorthand
     *
     * @param shorthand The shorthand name
     * @param id The POV id
     * @return The controller type
     */
    public ControllerType addPOVShorthand(String shorthand, int id) {
        POVShorthands.put(shorthand, id);
        return this;
    }

    // TODO: Add pov shorthands to some controller types below if needed
    static {
        registerControllerType(
                "joystick",
                ControllerType.createSimpleControllerType("joystick")
                        // Buttons
                        .addButtonShorthand("trigger", 0)
                        .addButtonShorthand("top", 1)
                        // Axes
                        .addAxisShorthand("x", 0)
                        .addAxisShorthand("y", 1)
                        .addAxisShorthand("z", 2)
                        .addAxisShorthand("twist", 3)
                        .addAxisShorthand("throttle", 4));
        registerControllerType(
                "xbox",
                ControllerType.createSimpleControllerType("xbox")
                        // Buttons
                        .addButtonShorthand("a", 1)
                        .addButtonShorthand("b", 2)
                        .addButtonShorthand("x", 3)
                        .addButtonShorthand("y", 4)
                        .addButtonShorthand("leftBumper", 5)
                        .addButtonShorthand("rightBumper", 6)
                        .addButtonShorthand("back", 7)
                        .addButtonShorthand("start", 8)
                        .addButtonShorthand("leftStick", 9)
                        .addButtonShorthand("rightStick", 10)
                        // Axes
                        .addAxisShorthand("leftX", 0)
                        .addAxisShorthand("leftY", 1)
                        .addAxisShorthand("rightX", 4)
                        .addAxisShorthand("rightY", 5)
                        .addAxisShorthand("leftTrigger", 2)
                        .addAxisShorthand("rightTrigger", 3));
        registerControllerType(
                "ps4",
                ControllerType.createSimpleControllerType("ps4")
                        // Buttons
                        .addButtonShorthand("square", 1)
                        .addButtonShorthand("cross", 2)
                        .addButtonShorthand("circle", 3)
                        .addButtonShorthand("triangle", 4)
                        .addButtonShorthand("L1", 5)
                        .addButtonShorthand("R1", 6)
                        .addButtonShorthand("L2", 7)
                        .addButtonShorthand("R2", 8)
                        .addButtonShorthand("share", 9)
                        .addButtonShorthand("options", 10)
                        .addButtonShorthand("L3", 11)
                        .addButtonShorthand("R3", 12)
                        .addButtonShorthand("PS", 13)
                        .addButtonShorthand("touchpad", 14)
                        // Axes
                        .addAxisShorthand("leftX", 0)
                        .addAxisShorthand("leftY", 1)
                        .addAxisShorthand("rightX", 2)
                        .addAxisShorthand("rightY", 3)
                        .addAxisShorthand("L2", 4)
                        .addAxisShorthand("R2", 5));
        registerControllerType(
                "ps5",
                ControllerType.createSimpleControllerType("ps5")
                        // Buttons
                        .addButtonShorthand("square", 1)
                        .addButtonShorthand("cross", 2)
                        .addButtonShorthand("circle", 3)
                        .addButtonShorthand("triangle", 4)
                        .addButtonShorthand("L1", 5)
                        .addButtonShorthand("R1", 6)
                        .addButtonShorthand("L2", 7)
                        .addButtonShorthand("R2", 8)
                        .addButtonShorthand("create", 9)
                        .addButtonShorthand("options", 10)
                        .addButtonShorthand("L3", 11)
                        .addButtonShorthand("R3", 12)
                        .addButtonShorthand("PS", 13)
                        .addButtonShorthand("touchpad", 14)
                        // Axes
                        .addAxisShorthand("leftX", 0)
                        .addAxisShorthand("leftY", 1)
                        .addAxisShorthand("rightX", 2)
                        .addAxisShorthand("rightY", 3)
                        .addAxisShorthand("L2", 4)
                        .addAxisShorthand("R2", 5));
        registerControllerType(
                "stadia",
                ControllerType.createSimpleControllerType("stadia")
                        // Buttons
                        .addButtonShorthand("a", 1)
                        .addButtonShorthand("b", 2)
                        .addButtonShorthand("x", 3)
                        .addButtonShorthand("y", 4)
                        .addButtonShorthand("leftBumper", 5)
                        .addButtonShorthand("rightBumper", 6)
                        .addButtonShorthand("leftStick", 7)
                        .addButtonShorthand("rightStick", 8)
                        .addButtonShorthand("ellipses", 9)
                        .addButtonShorthand("hamburger", 10)
                        .addButtonShorthand("stadia", 11)
                        .addButtonShorthand("rightTrigger", 12)
                        .addButtonShorthand("leftTrigger", 13)
                        .addButtonShorthand("google", 14)
                        .addButtonShorthand("frame", 15)
                        // Axes
                        .addAxisShorthand("leftX", 0)
                        .addAxisShorthand("leftY", 1)
                        .addAxisShorthand("rightX", 3)
                        .addAxisShorthand("rightY", 4));
    }
}
