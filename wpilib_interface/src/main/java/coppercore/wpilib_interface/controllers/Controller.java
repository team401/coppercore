package coppercore.wpilib_interface.controllers;

import coppercore.parameter_tools.json.annotations.JSONExclude;
import coppercore.parameter_tools.json.annotations.JSONName;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import coppercore.parameter_tools.json.helpers.JSONConverter;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

// Some Javadoc written by Copilot based on user description

/**
 * Controller class
 *
 * <p>This represents a physical controller and its control elements. It includes methods to get the
 * control elements. Each element has methods to get the value (current state) of the control
 * element. Also includes raw interfaces that directly interact with the hardware.
 *
 * <p>Gets input from WPILib's DriverStation class
 */
/**
 * High-level representation of a human interface controller and its mapped control elements.
 *
 * <p>This class encapsulates a physical controller (joystick/gamepad) connected to the driver
 * station and a collection of logical control elements (buttons, axes, POVs) that are exposed to
 * the rest of the codebase by command names. A Controller is responsible for:
 *
 * <ul>
 *   <li>Storing the hardware port and controller type.
 *   <li>Holding shorthand-to-id mappings for buttons, axes and POVs (used when resolving
 *       human-readable identifiers such as "A" or "left_x" to numeric hardware IDs).
 *   <li>Maintaining a registry of low-level hardware bindings (LowLevelControlElement) and
 *       higher-level ControlElement wrappers (Button, Axis, POV) keyed by command name.
 *   <li>Initializing all low-level and high-level elements so they become ready to read values from
 *       the DriverStation and to participate in the command scheduler loop.
 * </ul>
 *
 * Usage and lifecycle
 *
 * <ol>
 *   <li>A Controller is typically created by a JSON-to-Java loader (e.g.
 *       ControllerJsonRepresentation) which populates fields and shorthand maps.
 *   <li>One or more ControlElement instances (which wrap LowLevelControlElement instances) are
 *       registered with this Controller using addControlElementForCommand(...).
 *   <li>Once the Controller is fully configured and assigned a hardware port,
 *       initializeControlElements() should be called. This resolves textual IDs to numeric IDs,
 *       assigns the controller port into each low-level element, and initializes any button
 *       Triggers so they are wired into the CommandScheduler default button loop.
 *   <li>After initialization the rest of the robot code can obtain Suppliers or primitive suppliers
 *       (for example via ControlElement#getSupplier or getPrimitiveSupplier, or
 *       Button#getIsPressedSupplier) to poll values or attach commands.
 * </ol>
 *
 * Value transformation and behavior
 *
 * <p>ControlElements support configurable inversion, clamping or linear remapping between arbitrary
 * input and output ranges. Low-level elements expose raw hardware reads (via DriverStation) and
 * provide helpers for deadband handling (axes) and POV semantics. Buttons additionally provide
 * thresholding, hysteresis, and optional toggle behavior; toggled state is persisted in the Button
 * instance and updated when the configured press threshold is crossed.
 *
 * <p>Threading and performance
 *
 * <p>Reading control values ultimately queries the DriverStation and is expected to be called from
 * robot-periodic contexts (main loop or default button loop). Calls are intended to be inexpensive,
 * but any heavy processing should be avoided in tight control loops. The Controller and its element
 * maps are not inherently thread-safe with respect to concurrent modification; construct and
 * initialize controllers on robot startup and avoid mutating them from multiple threads.
 *
 * <p>Error handling
 *
 * <p>During initialization low-level elements resolve their numeric IDs from shorthand maps or by
 * parsing the configured string ID. If resolution fails (for example the shorthand is missing and
 * the string is not an integer) initialization will throw a RuntimeException; callers should ensure
 * JSON/configuration correctness or catch and surface initialization errors during robot setup.
 *
 * <p>Interactions with other systems
 *
 * <ul>
 *   <li>Control elements create Trigger instances wired to CommandScheduler#getDefaultButtonLoop()
 *       when Buttons are initialized.
 *   <li>LowLevelControlElement implementations call DriverStation API methods to obtain current
 *       hardware state.
 * </ul>
 *
 * Convenience accessors
 *
 * <p>Controller exposes typed lookup helpers (hasButton/getButton, hasAxis/getAxis, hasPOV/getPOV)
 * and a generic hasControlElement/getControlElement API for programs that treat controls
 * polymorphically.
 *
 * <p>See Also:
 *
 * <ul>
 *   <li>Controller.LowLevelControlElement — raw hardware binding and value preparation helpers
 *   <li>Controller.ControlElement, Controller.Button, Controller.Axis, Controller.POV —
 *       higher-level wrappers providing application-facing behavior (thresholds, toggles,
 *       deadbands, mapping)
 * </ul>
 */
public class Controller {

    // These fields are initialized in ControllerJsonRepresentation.toJava
    /**
     * Port index identifying which hardware/controller port this Controller is bound to.
     *
     * <p>Uses -1 as a sentinel value to indicate that the port has not been assigned
     * (uninitialized). When assigned, the value should be a non-negative integer corresponding to a
     * valid hardware mapping or driver-station port index.
     *
     * <p>Consumers should validate the port value before using it (e.g. port >= 0) and set it
     * during initialization to avoid relying on the sentinel value.
     */
    int port = -1;

    /**
     * The type of controller associated with this object.
     *
     * <p>Identifies the specific ControllerType (for example, XBOX, PLAYSTATION, GENERIC, etc.)
     * used to interpret button/axis mappings and controller-specific behavior. This field is
     * consulted when translating hardware input into logical commands and when applying any
     * controller-dependent processing.
     *
     * <p>May be null if no controller type has been set.
     *
     * @see ControllerType
     */
    ControllerType controllerType;

    /**
     * Maps human-friendly button shorthand names to their integer identifiers used by the
     * controller.
     *
     * <p>For example: "A" -> 1, "B" -> 2, "TRIGGER" -> 0. The map is initially null and is expected
     * to be populated during controller initialization or configuration loading.
     *
     * <p>Contract and usage notes: - Keys: non-null String shorthand (case conventions should be
     * documented/normalized by the initializer). - Values: Integer button indices/IDs as expected
     * by the underlying WPILib interface. - The field may be mutated after initialization; callers
     * should not assume immutability. - If this field remains null, callers must initialize it
     * before use to avoid NullPointerException. - If accessed concurrently, ensure external
     * synchronization or replace with a concurrent map implementation (e.g., ConcurrentHashMap).
     */
    HashMap<String, Integer> buttonShorthands = null;

    /**
     * Maps human-readable axis names to their numeric axis IDs for the controller.
     *
     * <p>Example entries: "LX" -> 0, "LY" -> 1, "RX" -> 2, etc. This allows code to refer to axes
     * by name instead of hard-coded integers.
     *
     * <p>Notes: - Keys are non-null axis name strings; values are non-null Integer axis indices. -
     * The field is initialized to null and should be populated during controller setup. - Not
     * thread-safe: synchronize externally if accessed from multiple threads.
     *
     * @see java.util.HashMap
     */
    HashMap<String, Integer> axisShorthands = null;

    /**
     * Mapping of POV (hat/D-pad) shorthand identifiers to their corresponding integer angle values.
     *
     * <p>This map is used to translate human-readable or shorthand direction names (for example
     * "up", "down", "left", "right" or abbreviations like "u", "d") into the numeric POV values
     * expected by WPILib (typically degrees such as 0, 90, 180, 270).
     *
     * <p>May be null until the controller is initialized; callers should check for null or ensure
     * initialization before use. The map is mutable and intended to be configured at setup time.
     * Access is not synchronized—if the controller is accessed from multiple threads, external
     * synchronization is required.
     */
    HashMap<String, Integer> povShorthands = null;

    /**
     * Maps controller element identifiers to their corresponding ControlElement instances.
     *
     * <p>Keys are String identifiers (for example button or axis names) used to look up elements.
     * Values are the ControlElement objects that represent logical inputs of the controller.
     *
     * <p>The map is initialized empty and is intended to be managed through the class's
     * registration and lookup methods rather than accessed directly. Note that HashMap is not
     * thread-safe; synchronize externally if this field may be accessed from multiple threads.
     */
    private HashMap<String, ControlElement> controllerElements = new HashMap<>();

    /**
     * Maps button identifiers to their associated Button objects for this controller.
     *
     * <p>Each entry associates a unique String key (typically a button name or ID) with a Button
     * instance that encapsulates that button's state and behavior.
     *
     * <p>This map is used to register, retrieve, and manage the buttons exposed by the controller
     * implementation. Note that HashMap is not thread-safe; if buttons may be accessed or modified
     * from multiple threads concurrently, external synchronization is required.
     */
    HashMap<String, Button> buttons = new HashMap<>();

    /**
     * Maps axis identifiers to their corresponding Axis objects.
     *
     * <p>Each entry associates a String key (the axis name or identifier) with an Axis instance
     * that represents an analog axis on the controller. The map is initialized empty and is
     * intended to be populated during controller setup so callers can look up axes by name.
     *
     * <p>Note: The map is mutable. If it may be accessed concurrently, callers should perform
     * appropriate synchronization or use a concurrent map implementation.
     */
    HashMap<String, Axis> axes = new HashMap<>();

    /**
     * Maps POV (point-of-view / D-pad) identifiers to their corresponding POV objects.
     *
     * <p>The map stores the set of POV inputs associated with this controller, keyed by a string
     * identifier for each POV (for example a name or index). It is initialized empty and intended
     * to be populated and updated as POVs are discovered or their states change.
     *
     * <p>Clients may use standard Map operations (get/put/remove) to query and modify entries. Note
     * that this is a plain HashMap and therefore not thread-safe; synchronize externally if
     * concurrent access from multiple threads is possible.
     */
    HashMap<String, POV> povs = new HashMap<>();

    /** Static initializer to register JSON conversion for Controller class. */
    static {
        JSONConverter.addConversion(Controller.class, ControllerJsonRepresentation.class);
    }

    /**
     * Returns the port number associated with this controller.
     *
     * @return the controller's hardware port index
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the type of the controller
     *
     * @return The controller type
     */
    public ControllerType getControllerType() {
        return controllerType;
    }

    /**
     * Check if the controller has a control element for the given command
     *
     * @param command The command to check
     * @return True if the controller has the element, false otherwise
     */
    public boolean hasControlElement(String command) {
        return controllerElements.containsKey(command);
    }

    /**
     * Get the control element for the given command
     *
     * @param command The command to get the control element for
     * @return The controller element.
     */
    public ControlElement getControlElement(String command) {
        return controllerElements.get(command);
    }

    /**
     * Check if the controller has a button for the given command
     *
     * @param command The command to check
     * @return True if the controller has the button, false otherwise
     */
    public boolean hasButton(String command) {
        return buttons.containsKey(command);
    }

    /**
     * Get the button for the given command
     *
     * @param command The command to get the button for
     * @return The button
     */
    public Button getButton(String command) {
        return buttons.get(command);
    }

    /**
     * Check if the controller has an axis for the given command
     *
     * @param command The command to check
     * @return True if the controller has the axis, false otherwise
     */
    public boolean hasAxis(String command) {
        return axes.containsKey(command);
    }

    /**
     * Get the axis for the given command
     *
     * @param command The command to get the axis for
     * @return The axis
     */
    public Axis getAxis(String command) {
        return axes.get(command);
    }

    /**
     * Check if the controller has a POV for the given command
     *
     * @param command The command to check
     * @return True if the controller has the POV, false otherwise
     */
    public boolean hasPOV(String command) {
        return povs.containsKey(command);
    }

    /**
     * Get the POV for the given command
     *
     * @param command The command to get the POV for
     * @return The POV
     */
    public POV getPOV(String command) {
        return povs.get(command);
    }

    /**
     * Add a control element to this controller.
     *
     * @param controlElement to be added.
     */
    void addControlElementForCommand(ControlElement controlElement) {
        controllerElements.put(controlElement.command, controlElement);
    }

    private static double applyInversion(
            double value, boolean inverted, double minValue, double maxValue) {
        return (inverted) ? maxValue + minValue - value : value;
    }

    private static double adjustRange(
            double value, double oldMin, double oldMax, double newMin, double newMax) {
        // Ensure value is inside old range
        double clampedValue = MathUtil.clamp(value, newMin, newMax);
        // Calculate normalized value in old range
        double t = (clampedValue - oldMin) / (oldMax - oldMin);
        // Map the normalized value to the new range
        return t * (newMax - newMin) + newMin;
    }

    /**
     * Low-level abstraction for a single physical control on a human interface device (for example:
     * a button, an axis, or a POV / hat switch).
     *
     * <p>This abstract class represents the raw binding between a JSON-configured controller
     * descriptor and the WPILib DriverStation input APIs. Subclasses must implement {@link
     * #getValue()} to return the current numeric reading for the underlying hardware input. The
     * class provides common configuration fields and helpers used by all low-level control types:
     *
     * <ul>
     *   <li>elementType — discriminator used for JSON polymorphic deserialization.
     *   <li>stringID — textual identifier from configuration (shorthand or integer string).
     *   <li>inverted, minValue, maxValue, clampValue — output-range and inversion configuration.
     *   <li>id, port — resolved numeric id and the controller port (populated by {@link
     *       #initialize}).
     * </ul>
     *
     * <p>Provided utilities:
     *
     * <ul>
     *   <li>{@link #fixRange(double,double,double)} — either clamp or linearly map an input value
     *       from a source range into the configured output range depending on {@code clampValue}.
     *   <li>{@link #prepareValue(double,double,double)} — apply input-range clamping/mapping and
     *       then inversion according to this instance's configuration (convenience around {@link
     *       #fixRange} and {@link Controller#applyInversion(double,boolean,double,double)}).
     *   <li>{@link #initialize(Controller)} — resolve numeric id and set the controller port using
     *       shorthand maps or by parsing {@code stringID}; will throw a {@link RuntimeException} if
     *       resolution fails.
     * </ul>
     *
     * <p>Usage notes:
     *
     * <ul>
     *   <li>Subclasses should call {@link #prepareValue} when converting raw inputs from
     *       DriverStation to the configured output range (and to apply inversion).
     *   <li>The {@code minValue} and {@code maxValue} fields may be {@code null} in JSON; callers
     *       frequently rely on subclass constructors to set sensible defaults (for example axes to
     *       [-1,1], buttons to [0,1], POVs to [-1,360]).
     *   <li>{@link #initialize(Controller)} must be invoked prior to reading values so that {@code
     *       id} and {@code port} are populated.
     * </ul>
     *
     * <p>Threading: instances are typically read from the main robot loop and are not synchronized;
     * construct and initialize on startup and avoid concurrent mutation at runtime.
     *
     * @see Controller#applyInversion(double, boolean, double, double)
     * @see Controller#adjustRange(double, double, double, double, double)
     */
    @JsonType(
            property = "elementType",
            subtypes = {
                @JsonSubtype(clazz = LowLevelButton.class, name = "button"),
                @JsonSubtype(clazz = LowLevelAxis.class, name = "axis"),
                @JsonSubtype(clazz = LowLevelPOV.class, name = "pov"),
            })
    public abstract static class LowLevelControlElement {
        /**
         * A short, stable identifier describing this controller element's type or category.
         *
         * <p>Used to determine how the element should be interpreted, processed, or rendered at
         * runtime and during (de)serialization. Typical values include simple names such as {@code
         * "button"}, {@code "axis"}, {@code "pov"} or {@code "joystick"}, or a fully-qualified
         * class name when a concrete implementation must be referenced.
         *
         * <p>Keep values stable if they are persisted or communicated between components. May be
         * {@code null} if the type is unspecified.
         */
        public String elementType;

        /**
         * Unique string identifier for this controller.
         *
         * <p>This field is mapped to the JSON property "id" (via @JSONName("id")) and is used
         * during serialization and deserialization to identify the controller instance. It is
         * expected to be a non-empty string when present; a null value indicates that no identifier
         * has been assigned.
         */
        @JSONName("id")
        public String stringID;

        /**
         * Whether the controller's output/direction is inverted.
         *
         * <p>When {@code true}, the controller input (for example an axis or motor direction) is
         * inverted. When {@code false} (the default), the input is used normally.
         */
        public Boolean inverted = false;

        /**
         * The inclusive lower bound for this controller's value.
         *
         * <p>When set, values produced or accepted by the controller should not be less than this
         * value. If this field is null, no minimum bound is enforced.
         *
         * <p>Nullable: may be null to indicate "no minimum".
         */
        public Double minValue;

        /**
         * The upper bound for values produced or accepted by this controller.
         *
         * <p>This is a nullable Double; a null value indicates that no explicit maximum is
         * enforced. Typically this represents a normalized, unitless limit (for example 1.0 for
         * joystick outputs), but the exact interpretation depends on the controller's
         * configuration.
         *
         * <p>Implementations may clamp or scale output values to this maximum. Because this field
         * is an object type, callers should handle the null case and avoid unboxing without a null
         * check.
         */
        public Double maxValue;

        /**
         * When true, enables clamping of values produced by this controller to its valid range.
         * Clamped values will be constrained to the controller's configured minimum and maximum
         * bounds (for example, a typical joystick-style output range). Defaults to false.
         *
         * <p>Note: this is a Boolean wrapper; callers should handle potential null values if the
         * field may be unset.
         */
        public Boolean clampValue = false;

        /**
         * Internal unique identifier for this controller instance.
         *
         * <p>This field is used internally to distinguish controller objects (for example when
         * tracking registrations or mapping to hardware). It is not part of the public serialized
         * state and is intentionally excluded from JSON serialization/deserialization via
         * {@code @JSONExclude}.
         *
         * <p>Do not rely on this value for persistent identity across application restarts; it is
         * intended for in-memory use only.
         */
        @JSONExclude protected int id;

        /**
         * The port index identifying which physical or virtual port this controller instance is
         * attached to. Intended to be a non-negative integer used when mapping controller
         * inputs/outputs to hardware. This field is protected (accessible to subclasses) and is
         * annotated with @JSONExclude so that runtime-specific port assignments are not included in
         * JSON serialization.
         */
        @JSONExclude protected int port;

        /**
         * Adjusts an input value to the controller's configured output range.
         *
         * <p>If the controller is configured to clamp values (the {@code clampValue} flag), the
         * input {@code value} is constrained to lie within the controller's {@code minValue} and
         * {@code maxValue} bounds using a clamping operation. Otherwise, the input {@code value} is
         * linearly mapped from the source range defined by {@code oldMin}..{@code oldMax} into the
         * target range defined by {@code minValue}..{@code maxValue}.
         *
         * @param value the input value to be adjusted
         * @param oldMin the lower bound of the input (source) range
         * @param oldMax the upper bound of the input (source) range
         * @return the adjusted value either clamped to [minValue, maxValue] or scaled into that
         *     range
         */
        protected double fixRange(double value, double oldMin, double oldMax) {
            return (clampValue)
                    ? MathUtil.clamp(value, minValue, maxValue)
                    : adjustRange(value, oldMin, oldMax, minValue, maxValue);
        }

        /**
         * Prepare a raw input value for use by the controller.
         *
         * <p>The method first constrains the supplied {@code value} to the provided input range
         * [{@code oldMin}, {@code oldMax}] (via {@code fixRange}), then applies the controller's
         * inversion and output-range transformation (via {@code applyInversion}) using the instance
         * configuration for {@code inverted}, {@code minValue}, and {@code maxValue}.
         *
         * @param value the raw input value to normalize and transform
         * @param oldMin the lower bound of the original input range (inclusive)
         * @param oldMax the upper bound of the original input range (inclusive)
         * @return the value clamped to the input range and then mapped/possibly inverted into the
         *     controller's configured output range
         * @see #fixRange(double, double, double)
         * @see #applyInversion(double, boolean, double, double)
         */
        protected double prepareValue(double value, double oldMin, double oldMax) {
            return applyInversion(fixRange(value, oldMin, oldMax), inverted, minValue, maxValue);
        }

        /**
         * Initialize this controller-binding instance using the provided Controller.
         *
         * <p>This method performs two main actions:
         *
         * <ol>
         *   <li>Sets this.port to the value returned by controller.getPort().
         *   <li>Resolves a numeric ID for this instance from the instance field {@code stringID}:
         *       <ul>
         *         <li>If {@code elementType} equals {@code "button"}, {@code "axis"} or {@code
         *             "pov"}, the method first attempts to look up the numeric ID in the
         *             corresponding shorthand map on the provided {@code controller} ({@code
         *             buttonShorthands}, {@code axisShorthands} or {@code povShorthands}).
         *         <li>If no mapping is found, the method attempts to parse {@code stringID} as an
         *             integer using {@link Integer#parseInt(String)}.
         *       </ul>
         *   <li>When a numeric ID is successfully resolved, it is assigned to this instance's
         *       {@code id} field.
         * </ol>
         *
         * <p>Note: {@code elementType} and {@code stringID} are expected to be fields of the
         * containing object; they are consulted during resolution.
         *
         * @param controller the Controller used to obtain the port and any shorthand mappings; must
         *     not be {@code null}
         * @throws RuntimeException if {@code stringID} cannot be resolved to an integer via the
         *     controller's shorthand maps or by parsing
         */
        public void initialize(Controller controller) {
            this.port = controller.getPort();

            Integer resolvedID = null;
            switch (elementType) {
                case "button":
                    resolvedID = controller.buttonShorthands.get(stringID);
                    break;
                case "axis":
                    resolvedID = controller.axisShorthands.get(stringID);
                    break;
                case "pov":
                    resolvedID = controller.povShorthands.get(stringID);
                    break;
            }
            if (resolvedID == null) {
                try {
                    resolvedID = Integer.parseInt(stringID);
                } catch (NumberFormatException e) {
                    throw new RuntimeException(
                            "Could not resolve ID for controller interface with string ID: "
                                    + stringID);
                }
            }
            this.id = resolvedID;
        }

        /**
         * Returns the current numeric value represented by this controller.
         *
         * <p>The exact meaning, units, and valid range of the returned value are
         * implementation-dependent. Concrete subclasses should document whether the value
         * represents, for example, a joystick axis (commonly in the range -1.0 to 1.0), a sensor
         * measurement, a computed control output, or another quantity.
         *
         * <p>Implementations should aim for this method to be side-effect free and inexpensive to
         * call, as it may be invoked frequently by control loops, logging, or telemetry systems.
         *
         * @return the current value of this controller as a double
         */
        public abstract double getValue();
    }

    /**
     * Low-level control element representing a digital button on a joystick or controller.
     *
     * <p>This class maps a digital press/release state to a numeric value suitable for the
     * controller framework. By default the elementType is set to "button" and the numeric range is
     * initialized to [0.0, 1.0], reflecting the binary nature of a button (released = 0.0, pressed
     * = 1.0).
     *
     * <p>Instances are thin wrappers around the underlying hardware input and do not cache state;
     * callers should invoke {@code getValue()} from an appropriate control context (for example the
     * main robot loop) to obtain fresh readings.
     *
     * @see LowLevelControlElement
     */
    public static class LowLevelButton extends LowLevelControlElement {

        /**
         * Constructs a LowLevelButton instance.
         *
         * <p>Initializes the elementType to "button". If the numeric range bounds are not already
         * set, this constructor assigns default values to the wrapper fields: - minValue is set to
         * 0.0 if it is null - maxValue is set to 1.0 if it is null
         *
         * <p>These defaults provide a canonical minimum and maximum for the button's value,
         * ensuring downstream code can rely on a defined range for normalization or validation.
         */
        public LowLevelButton() {
            elementType = "button";
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 1.0;
        }

        /**
         * Returns the current numeric value for this controller input.
         *
         * <p>This method queries the DriverStation for the digital state of the button identified
         * by this controller's port and id. The boolean pressed state is converted to a numeric
         * value (1.0 when pressed, 0.0 when not pressed) and then passed to {@code
         * prepareValue(double, double, double)} with a dead zone of 0.0 and a maximum of 1.0. Any
         * normalization, filtering, or clamping done by {@code prepareValue} will be applied to the
         * result.
         *
         * <p>Note: This reads hardware state from the DriverStation; call it from appropriate robot
         * control contexts (for example the main control loop) and avoid calling it excessively
         * from non-real-time threads if {@code prepareValue} is expensive.
         *
         * @return a normalized value in the range [0.0, 1.0]: 1.0 if the underlying button is
         *     pressed, 0.0 if not, after applying {@code prepareValue}.
         * @see edu.wpi.first.wpilibj.DriverStation#getStickButton(int, int)
         * @see #prepareValue(double, double, double)
         */
        public double getValue() {
            boolean pressed = DriverStation.getStickButton(port, id);
            return prepareValue((pressed) ? 1.0 : 0.0, 0.0, 1.0);
        }
    }

    /**
     * Low-level representation of a joystick/controller axis.
     *
     * <p>This class models an individual analog axis on a controller and provides common
     * preprocessing features used by higher-level input handling:
     *
     * <ul>
     *   <li>Configurable deadband to suppress small input noise.
     *   <li>Optional remapping of values outside the deadband so the remaining input range fills
     *       the full output range.
     *   <li>Integration with the parent {@code LowLevelControlElement} for value preparation
     *       (scaling/clamping) and metadata such as {@code port}, {@code id}, {@code minValue} and
     *       {@code maxValue}.
     * </ul>
     *
     * <p>Typical usage: read the raw axis from the DriverStation, apply the configured deadband
     * (and optional remapping), then prepare the final value (scale and clamp) for downstream
     * logic.
     *
     * <p>Fields:
     *
     * <ul>
     *   <li>{@code public Double deadband} — Fractional dead zone size. Expected in [0.0, 1.0).
     *       Values less than or equal to 0.0 disable the deadband. Values approaching or equal to
     *       1.0 are invalid for remapping (would cause division by zero).
     *   <li>{@code public Boolean remapDeadband} — When {@code false} inputs inside the deadband
     *       are clipped to {@code 0.0} and values outside are passed through unchanged. When {@code
     *       true} inputs outside the deadband are linearly rescaled so the range {@code [deadband,
     *       1.0]} maps to {@code [0.0, 1.0]} (sign preserved).
     * </ul>
     *
     * Behavior notes:
     *
     * <ul>
     *   <li>The constructor sets {@code elementType = "axis"} and ensures default {@code minValue =
     *       -1.0} and {@code maxValue = 1.0} when they are otherwise unset.
     *   <li>{@link #applyDeadband(double)} implements the deadband/remapping semantics. If
     *       remapping is enabled and {@code deadband >= 1.0} a division-by-zero will occur; callers
     *       should ensure deadband remains in a safe range.
     *   <li>{@link #getValue()} reads the raw axis (via {@code DriverStation.getStickAxis(port,
     *       id)}), applies the deadband, then forwards the result to {@code prepareValue(...)} so
     *       the final output lies within {@code [minValue, maxValue]}.
     * </ul>
     *
     * <p>Implementation Note: Inputs are normally expected in the range [-1.0, 1.0]. The
     * deadband/remap logic preserves the sign of the input. When {@code remapDeadband} is enabled
     * the non-zero outputs are expanded to occupy the full dynamic range outside the deadband
     * using: sign(value) * ((|value| - deadband) / (1.0 - deadband)).
     *
     * @see LowLevelControlElement
     * @see DriverStation#getStickAxis(int, int)
     */
    public static class LowLevelAxis extends LowLevelControlElement {
        /**
         * Deadband threshold applied to controller input values.
         *
         * <p>Any input whose absolute value is less than this threshold should be treated as zero.
         * This filters out small unintended joystick or sensor noise.
         *
         * <p>Expected range is 0.0 (no deadband) to 1.0 (maximum deadband); default is 0.0. Inputs
         * are assumed to be normalized (e.g., in the range [-1.0, 1.0]).
         *
         * <p>Note: this field is a Double and may be null; callers should handle null appropriately
         * (for example, treating null as 0.0).
         */
        public Double deadband = 0.0;

        /**
         * If true, remap controller input after applying the deadband.
         *
         * <p>When enabled, small input values within the configured deadband are treated as zero
         * and the remaining input range outside the deadband is rescaled to preserve full-scale
         * responsiveness. This reduces unintentional drift around the center while maintaining
         * correct maximum output.
         *
         * <p>When disabled, inputs inside the deadband are set to zero but the values outside the
         * deadband are not rescaled.
         *
         * <p>Default: true.
         */
        public Boolean remapDeadband = true;

        /**
         * Constructs a LowLevelAxis and ensures sensible default values.
         *
         * <p>This constructor identifies the object as an axis by setting the elementType field to
         * "axis". It also guarantees that the axis bounds are initialized: if minValue is null it
         * will be set to -1.0, and if maxValue is null it will be set to 1.0. Existing non-null
         * bounds are left unchanged.
         */
        public LowLevelAxis() {
            elementType = "axis";
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 1.0;
        }

        /**
         * Applies the configured deadband to a joystick/controller input value.
         *
         * <p>Behavior depends on the instance flags and thresholds:
         *
         * <ul>
         *   <li>If {@code remapDeadband} is {@code false}, the method simply clips small inputs:
         *       returns {@code 0.0} when {@code Math.abs(value) < deadband}, otherwise returns the
         *       original {@code value} unchanged.
         *   <li>If {@code remapDeadband} is {@code true}, inputs inside the deadband are mapped to
         *       {@code 0.0}, while inputs outside the deadband are linearly rescaled so that the
         *       remaining range {@code [deadband, 1.0]} is remapped to {@code [0.0, 1.0]}. The
         *       original sign of {@code value} is preserved.
         * </ul>
         *
         * <p>Example (when remapping enabled): adjustedValue = (|value| - deadband) / (1.0 -
         * deadband); result = sign(value) * adjustedValue
         *
         * @param value the input value to process (commonly in the range -1.0 to 1.0)
         * @return the value after applying the deadband and optional remapping. The return value
         *     will be {@code 0.0} for inputs within the deadband, and otherwise will preserve the
         *     sign of the original input. When remapping is enabled, non-zero outputs are scaled to
         *     occupy the full output range outside the deadband.
         *     <p>Implementation note: This method relies on the instance fields {@code deadband}
         *     (expected in the range [0.0, 1.0)) and {@code remapDeadband}. If {@code deadband} is
         *     equal to or greater than {@code 1.0}, remapping will cause a division by zero; such
         *     values are not supported and should be avoided or validated elsewhere.
         */
        protected double applyDeadband(double value) {
            if (remapDeadband == false) {
                return (Math.abs(value) < deadband) ? 0.0 : value;
            }
            return MathUtil.applyDeadband(value, deadband);
        }

        /**
         * Returns the current processed axis value for this controller.
         *
         * <p>This method reads the raw axis value from the DriverStation using this controller's
         * configured port and axis id, applies the configured deadband to ignore small inputs, and
         * then prepares the value (for example scaling or clamping) into the configured
         * minimum/maximum range via {@code prepareValue}.
         *
         * <p>The raw axis value is typically in the range [-1.0, 1.0]; the returned value will be
         * constrained to [minValue, maxValue] after preparation.
         *
         * @return the processed axis value after deadband filtering and range preparation
         */
        public double getValue() {
            double value = DriverStation.getStickAxis(port, id);
            return prepareValue(applyDeadband(value), minValue, maxValue);
        }
    }

    /**
     * Low-level control element representing a joystick POV (Point-Of-View or "hat") switch,
     * commonly exposed to users as the controller "D‑pad" (directional pad).
     *
     * <p>This concrete LowLevelControlElement provides access to a joystick's POV direction as
     * reported by the DriverStation. It configures the element type to "pov" and establishes
     * sensible default value bounds for POV angles:
     *
     * <ul>
     *   <li>minValue: -1.0 — used to represent "no direction pressed"
     *   <li>maxValue: 360.0 — maximum angle in degrees
     * </ul>
     *
     * <p>Behavior:
     *
     * <ul>
     *   <li>When queried, the element obtains the raw POV reading via
     *       DriverStation.getStickPOV(port, id).
     *   <li>The raw value is expressed in degrees (0–360) or -1 when the POV (D‑pad) is not
     *       pressed.
     *   <li>The raw reading is passed to prepareValue(...) so the returned value is
     *       normalized/clamped according to the configured min/max for this element.
     *   <li>This implementation supports diagonal directions; the DriverStation may return
     *       intermediate compass angles such as 45, 135, 225, 315 to indicate diagonal presses in
     *       addition to the primary 0/90/180/270 directions.
     * </ul>
     *
     * <p>Notes for consumers:
     *
     * <ul>
     *   <li>"port" refers to the joystick port/index and "id" refers to the POV index on that
     *       joystick (inherited from LowLevelControlElement).
     *   <li>Callers should expect -1 to indicate no active POV direction; otherwise the returned
     *       value is the angle in degrees (0 = up, 45 = up-right, 90 = right, 135 = down-right, 180
     *       = down, 225 = down-left, 270 = left, 315 = up-left).
     * </ul>
     *
     * @see DriverStation#getStickPOV(int, int)
     */
    public static class LowLevelPOV extends LowLevelControlElement {

        /**
         * Constructs a LowLevelPOV and establishes sensible defaults for POV elements.
         *
         * <p>This constructor sets the element type to "pov". If minValue or maxValue have not been
         * initialized (are null), it assigns default bounds for the POV hat: a minimum of -1.0
         * (commonly used to indicate "no direction" / not pressed) and a maximum of 360.0
         * (degrees).
         *
         * <p>These defaults provide consistent range semantics for consumers that expect POV values
         * in degrees and a sentinel value when the POV is inactive.
         */
        public LowLevelPOV() {
            elementType = "pov";
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 360.0;
        }

        /**
         * Retrieves and returns the prepared POV (point-of-view / hat) value for this controller.
         *
         * <p>This method reads the raw POV value from the DriverStation for the joystick identified
         * by this controller's port and id (DriverStation.getStickPOV(port, id)) and passes that
         * raw value into prepareValue with -1 and 360 as sentinel bounds. In the underlying API the
         * raw POV is typically -1 when the hat is in a neutral position (not pressed) or an angle
         * in degrees (0–360) when a direction is pressed. The returned value is the result of
         * prepareValue applied to that raw input, and therefore reflects whichever normalization,
         * clamping, or mapping logic prepareValue implements for the sentinel and angle range.
         *
         * @return the POV value as a double after processing by prepareValue; raw inputs are -1 for
         *     neutral/not-pressed or an angle in degrees (0–360) which are converted according to
         *     prepareValue's semantics
         * @see edu.wpi.first.wpilibj.DriverStation#getStickPOV(int, int)
         */
        public double getValue() {
            return prepareValue(DriverStation.getStickPOV(port, id), -1, 360);
        }
    }

    /**
     * Abstract base class representing a configurable control element that maps a low-level
     * controller input to a processed numeric value or command.
     *
     * <p>This class is intended to be extended by concrete control element types (for example,
     * Button, Axis, and POV). Instances are configured via JSON and support polymorphic
     * deserialization using the "commandType" discriminator (subtypes: "button", "axis", "pov").
     *
     * <p>Responsibilities:
     *
     * <ul>
     *   <li>Hold configuration for mapping and transforming a low-level input
     *       (LowLevelControlElement) into a higher-level numeric value.
     *   <li>Provide lifecycle support to initialize the underlying low-level element with a
     *       Controller instance.
     *   <li>Expose the processed value through abstract getValue(), and convenience Supplier
     *       accessors.
     * </ul>
     *
     * <p>Key fields (configuration):
     *
     * <ul>
     *   <li>{@code lowLevelControlElement} (JSON name "humanControlElement") — the underlying input
     *       source that supplies raw values and range metadata.
     *   <li>{@code command} — a logical name or identifier for the command associated with this
     *       element.
     *   <li>{@code commandType} — JSON discriminator for the concrete control element type.
     *   <li>{@code inverted} — when true, the resulting value is inverted (default: false).
     *   <li>{@code minValue}, {@code maxValue} — configured target range for the element. If null,
     *       the low-level element's range is used.
     *   <li>{@code clampValue} — when true, the value is clamped to [minValue, maxValue] rather
     *       than being scaled/adjusted (default: false).
     * </ul>
     *
     * <p>Notable protected helpers:
     *
     * <ul>
     *   <li>{@code fixRange(double)} — either clamps the given value into the configured range or
     *       adjusts (scales) it from the low-level element's range into the configured target
     *       range, depending on {@code clampValue}.
     *   <li>{@code getPreparedValue()} — obtains the raw value from the low-level element, applies
     *       range adjustment/clamping and inversion, and returns the processed result.
     * </ul>
     *
     * <p>Primary methods to implement and use:
     *
     * <ul>
     *   <li>{@code public abstract double getValue()} — subclasses must return the final value that
     *       represents this control element. Implementations will typically call {@code
     *       getPreparedValue()} and/or include additional processing logic.
     *   <li>{@code public void initialize(Controller controller)} — initializes the underlying
     *       low-level element with the given Controller. Subclasses should call
     *       super.initialize(controller) if they override this method.
     *   <li>{@code public Supplier<Double> getSupplier()} and {@code public DoubleSupplier
     *       getPrimitiveSupplier()} — convenience accessors that return suppliers bound to {@code
     *       getValue()} for use with APIs that accept suppliers or primitive double suppliers.
     * </ul>
     *
     * <p>Usage notes:
     *
     * <ul>
     *   <li>When configuring via JSON, supply the "commandType" discriminator and the nested
     *       "humanControlElement" configuration to properly construct and wire the underlying
     *       input.
     *   <li>Null {@code minValue}/{@code maxValue} are interpreted to mean "use the low-level
     *       element's range".
     * </ul>
     *
     * @see LowLevelControlElement
     * @see MathUtil
     */
    @JsonType(
            property = "commandType",
            subtypes = {
                @JsonSubtype(clazz = Button.class, name = "button"),
                @JsonSubtype(clazz = Axis.class, name = "axis"),
                @JsonSubtype(clazz = POV.class, name = "pov"),
            })
    public abstract static class ControlElement {
        @JSONName("humanControlElement")
        LowLevelControlElement lowLevelControlElement;

        /**
         * Logical command identifier for this control element.
         *
         * <p>A short, human-readable key that names the action or command represented by this
         * control (for example "shoot", "drive_forward", "intake_toggle"). This string is used as:
         *
         * <ul>
         *   <li>a lookup key in the Controller's element maps (buttons/axes/povs)
         *   <li>a stable identifier in JSON configuration and persistence
         *   <li>a label for telemetry, logging and command bindings
         * </ul>
         *
         * <p>Guidelines and semantics:
         *
         * <ul>
         *   <li>Must be non-null and ideally unique within a single Controller instance.
         *   <li>Prefer a concise, machine-friendly format (e.g. lower_snake_case, camelCase or
         *       kebab-case).
         *   <li>Avoid leading/trailing whitespace; do not rely on this field for localization.
         *   <li>Changes to this value will affect configuration lookups and persisted mappings.
         * </ul>
         *
         * <p>Example: "arm_extend", "climb_toggle", "left_x". The field is intended for
         * identification only and does not change runtime behavior of the element itself.
         */
        public String command;

        /**
         * Identifier for the type of command this controller represents.
         *
         * <p>This string describes how input associated with the controller should be interpreted
         * (for example: "button", "axis", "trigger", "dpad", etc.) and is used to select the
         * appropriate handling or routing logic.
         *
         * <p>Values should correspond to the set of supported command types in the application.
         * Consumers should treat this value as meaningful only when it is non-null and matches an
         * expected command type identifier.
         */
        public String commandType;

        /**
         * When true, the element's output is inverted within its configured [minValue, maxValue]
         * range by mapping a value to (minValue + maxValue - value). This preserves the configured
         * output range and works for asymmetric ranges (for example [-1,1], [0,1], or any custom
         * min/max). It is NOT a simple sign flip; it mirrors the value around the midpoint of the
         * configured range.
         *
         * <p>When false, values are passed through unchanged.
         */
        public Boolean inverted = false;

        /**
         * Configured minimum of the element's output range.
         *
         * <p>If null, this indicates "use the low-level element's minimum" or that a subclass will
         * supply a sensible default (for example Axis -> -1.0, Button -> 0.0, POV -> -1.0).
         */
        public Double minValue;

        /**
         * Configured maximum of the element's output range.
         *
         * <p>If null, this indicates "use the low-level element's maximum" or that a subclass will
         * supply a sensible default (for example Axis -> 1.0, Button -> 1.0, POV -> 360.0).
         */
        public Double maxValue;

        /**
         * Determines how values are adjusted into the configured range.
         *
         * <p>If true, values produced or consumed by this controller are constrained to the
         * controller's valid range (for example, clamping to -1.0..1.0). When false, values sre
         * scaled or adjusted from the low-level element's range into the configured range
         *
         * <p>Default value is false.
         */
        public Boolean clampValue = false;

        /**
         * Constrains or maps a raw input value into this controller's configured output range.
         *
         * <p>If {@code clampValue} is {@code true}, the input is clamped to the inclusive range
         * {@code [minValue, maxValue]} using {@code MathUtil.clamp}. Otherwise the input is
         * linearly mapped from the low-level control element's range {@code
         * [lowLevelControlElement.minValue, lowLevelControlElement.maxValue]} into the controller's
         * target range {@code [minValue, maxValue]} using {@code adjustRange}.
         *
         * @param value the raw input value to be constrained or mapped
         * @return the value coerced into this controller's range (either clamped or adjusted)
         */
        protected double fixRange(double value) {
            return (clampValue)
                    ? MathUtil.clamp(value, minValue, maxValue)
                    : adjustRange(
                            value,
                            lowLevelControlElement.minValue,
                            lowLevelControlElement.maxValue,
                            minValue,
                            maxValue);
        }

        /**
         * Returns the prepared control value ready for higher-level use.
         *
         * <p>The method obtains the raw value from the low-level control element, corrects it with
         * fixRange(...), and then applies optional inversion and scaling via applyInversion(...),
         * using the inverted flag and the configured minValue and maxValue bounds.
         *
         * @return the processed control value after range correction and optional inversion
         *     (constrained to the configured min/max)
         */
        protected double getPreparedValue() {
            return applyInversion(
                    fixRange(lowLevelControlElement.getValue()), inverted, minValue, maxValue);
        }

        /**
         * Returns the current value produced by this controller.
         *
         * <p>Implementations should provide a double representing the current state of the
         * controller (for example an axis position, trigger level, or a computed control signal).
         * The precise meaning, scaling and valid range of the returned value are defined by the
         * concrete implementation.
         *
         * @return the current controller value
         */
        public abstract double getValue();

        /**
         * Initializes this controller by delegating to the encapsulated low-level control element.
         *
         * <p>The provided {@code controller} is forwarded to {@code
         * lowLevelControlElement.initialize(controller)}, allowing the low-level element to perform
         * any necessary setup or resource allocation required for subsequent control operations.
         *
         * @param controller the Controller instance used to initialize the low-level element; must
         *     not be null
         * @throws NullPointerException if {@code controller} is null
         */
        public void initialize(Controller controller) {
            lowLevelControlElement.initialize(controller);
        }

        /**
         * Returns a Supplier that, when invoked, retrieves the current numeric value from this
         * controller.
         *
         * <p>The returned {@code Supplier<Double>} delegates to {@link #getValue()} each time it is
         * called, so it always reflects the controller's most recent value rather than caching a
         * previous result.
         *
         * <p>Note: the supplier object itself is non-null; however, thread-safety and any side
         * effects depend on the implementation of {@link #getValue()}.
         *
         * @return a non-null {@code Supplier<Double>} that obtains the current value by calling
         *     {@link #getValue()}
         */
        public Supplier<Double> getSupplier() {
            return this::getValue;
        }

        /**
         * Returns a DoubleSupplier that supplies the current primitive double value from this
         * controller.
         *
         * <p>The supplier delegates to {@link #getValue()}, providing the value as a primitive
         * double (avoiding boxing) each time it is invoked.
         *
         * @return a {@link java.util.function.DoubleSupplier} that invokes {@link #getValue()} to
         *     obtain the current value
         */
        public DoubleSupplier getPrimitiveSupplier() {
            return this::getValue;
        }
    }

    /**
     * A configurable "button" ControlElement that interprets a numeric input as a boolean pressed
     * state, with optional thresholding, hysteresis and toggle behavior.
     *
     * <p>Summary
     *
     * <ul>
     *   <li>Takes a numeric input (from the underlying LowLevelControlElement) and decides whether
     *       it is "pressed" according to a configurable threshold and optional range/hysteresis.
     *   <li>Supports toggle mode where each rising edge flips a latched boolean state instead of
     *       reflecting the instantaneous input.
     *   <li>Provides suppliers and a WPILib Trigger for integration with command bindings.
     * </ul>
     *
     * <p>Configuration and semantics
     *
     * <ul>
     *   <li>threshold: Central activation value. If the prepared input lies inside the computed
     *       acceptance window, the button is considered pressed.
     *   <li>thresholdRange (nullable): If non-null, the acceptance window is [threshold -
     *       thresholdRange/2, threshold + thresholdRange/2]. If null, the window defaults to
     *       [threshold, maxValue] (with optional hysteresis adjustment).
     *   <li>hysteresis (nullable): When non-null, hysteresis modifies the computed lower/upper
     *       bounds based on the previous raw pressed state (lastState). This reduces chatter near
     *       the boundary: when lastState==true the window is expanded, and when lastState==false
     *       the window is contracted.
     *   <li>isToggle: When true, a rising edge {@code (pressed && !lastState)} flips the internal
     *       latched state isToggled. When false, the reported pressed state simply reflects the
     *       threshold test result.
     * </ul>
     *
     * <p>Behavioral notes
     *
     * <ul>
     *   <li>Applying threshold/hysteresis: testThreshold(double) computes inclusive bounds and
     *       returns true if value is within them. When thresholdRange is null the upper bound
     *       defaults to maxValue (and may be enlarged by hysteresis to avoid missing brief
     *       activations).
     *   <li>Toggle semantics: applyToggle(boolean) updates lastState and flips isToggled on a
     *       rising edge when isToggle==true. lastState always tracks the most recent raw pressed
     *       signal and is used for both toggle edge detection and hysteresis decisions.
     *   <li>Threading: instances are not synchronized. Concurrent reads are fine, but concurrent
     *       mutations (for example toggling configuration fields or calling initialize while
     *       reading) must be externally synchronized.
     * </ul>
     *
     * <p>Lifecycle
     *
     * <ul>
     *   <li>The constructor ensures minValue defaults to 0.0 and maxValue defaults to 1.0 if they
     *       are unset, guaranteeing a usable numeric range.
     *   <li>initialize(Controller) delegates to the superclass to initialize the low-level element
     *       and then creates a Trigger on the CommandScheduler default button loop that polls
     *       this::isPressed.
     * </ul>
     *
     * <p>Provided helper methods (high level)
     *
     * <ul>
     *   <li>testThreshold(value) — returns whether the supplied value falls inside the computed
     *       acceptance window (considering thresholdRange and hysteresis).
     *   <li>applyToggle(pressed) — applies toggle behavior when enabled and updates internal state
     *       for edge detection.
     *   <li>isPressed() — obtains the prepared numeric input, clamps to [minValue,maxValue], runs
     *       the threshold test and returns the (possibly toggled) boolean pressed state.
     *   <li>getValue() — returns maxValue when pressed, otherwise minValue.
     * </ul>
     *
     * <p>Example usage
     *
     * <pre>
     *   // JSON-configured: threshold = 0.5, thresholdRange = 0.2, isToggle = true
     *   // A short push above ~0.4 will toggle the latched state; hysteresis can be used to tune
     *   // robustness around the boundary.
     * </pre>
     *
     * <p>See also: ControlElement.getPreparedValue() for how the numeric input is produced.
     */
    public static class Button extends ControlElement {
        /**
         * Deadband threshold for controller inputs.
         *
         * <p>Inputs with absolute value less than this threshold are treated as zero and should be
         * ignored by input processing. Default value is 0.5.
         *
         * <p>Expected to be used with controller input values on the same scale (commonly -1.0 to
         * 1.0). Adjust as needed to filter out small/intentional noise.
         */
        public Double threshold = 0.5;

        /**
         * Optional tolerance or hysteresis width applied around the controller's primary threshold.
         *
         * <p>This nullable Double represents an additional range (radius) around a configured
         * threshold value. When non-null, values within this range are treated as inside the
         * threshold band, allowing for tolerance or hysteresis in threshold comparisons. When null,
         * no additional range is applied and comparisons should be strict.
         *
         * <p>Expected usage: - Set to a non-negative Double to enable a threshold band. - Leave as
         * null to indicate "no range configured".
         *
         * <p>Note: Negative values are not meaningful and should be avoided.
         */
        public Double thresholdRange = null;

        /**
         * Hysteresis threshold for the controller.
         *
         * <p>When non-null, changes smaller than this absolute threshold (i.e. within +/-
         * hysteresis) are considered insignificant and will not cause the controller to change its
         * output or to consider the setpoint reached. This is useful for preventing rapid
         * oscillation when the measured value hovers near the setpoint.
         *
         * <p>The value uses the same units and scale as the controller's inputs/outputs. A null
         * value disables hysteresis (default). Callers should provide a non-negative value when
         * enabling hysteresis; negative values are considered invalid.
         */
        public Double hysteresis = null;

        /**
         * If true, this controller input is treated as a toggle: each activation flips the logical
         * state rather than directly mirroring the raw input. When false, the control behaves as a
         * momentary (non-latching) input and simply reflects the current input state.
         *
         * <p>Default: false
         */
        public Boolean isToggle = false;

        @JSONExclude private boolean isToggled = false;
        @JSONExclude private boolean lastState = false;
        @JSONExclude private Trigger trigger;

        /**
         * Determines whether the supplied value lies within the controller's computed threshold
         * window.
         *
         * <p>The window (lowerBound..upperBound) is computed as follows: - If {@code
         * thresholdRange} is non-null, the window is centered on {@code threshold} with half-width
         * {@code thresholdRange / 2.0}: lowerBound = threshold - thresholdRange/2.0 upperBound =
         * threshold + thresholdRange/2.0 - If {@code thresholdRange} is null, the lower bound is
         * {@code threshold} and the upper bound defaults to {@code maxValue}. In this case, if
         * {@code hysteresis} is non-null the upper bound is first increased by {@code hysteresis}
         * to help tolerate a jump over the threshold.
         *
         * <p>If {@code hysteresis} is non-null, an additional adjustment is applied using the
         * current {@code lastState}: - A multiplier of +1.0 is used when {@code lastState} is true,
         * and -1.0 when false. - The lower bound is adjusted by subtracting {@code hysteresis *
         * multiplier}, and the upper bound is adjusted by adding {@code hysteresis * multiplier}.
         * Effectively, when {@code lastState} is true the window is expanded outward by {@code
         * hysteresis}; when false the window is moved inward by {@code hysteresis}.
         *
         * <p>Comparison is inclusive: the method returns {@code true} if {@code value >= lowerBound
         * && value <= upperBound}.
         *
         * <p>Notes: - This method relies on instance fields: {@code threshold}, {@code maxValue},
         * {@code thresholdRange}, {@code hysteresis}, and {@code lastState}. - {@code
         * thresholdRange} and {@code hysteresis} may be {@code null}; the method handles those
         * cases as described above.
         *
         * @param value the value to test against the computed threshold window
         * @return {@code true} if the value falls within the inclusive computed bounds, otherwise
         *     {@code false}
         */
        protected boolean testThreshold(double value) {
            double lowerBound = threshold;
            double upperBound = maxValue;
            if (thresholdRange != null) {
                lowerBound = threshold - thresholdRange / 2.0;
                upperBound = threshold + thresholdRange / 2.0;
            } else {
                // This is to handle the case where only threshold is set
                // and there is hysteresis and the value jumps over the threshold
                // when there is no thresholdRange such as jumping from 0.0 to 1.0
                // when threshold is 0.5 and hysteresis is greater than 0.0
                upperBound += (hysteresis != null) ? hysteresis : 0.0;
            }
            if (hysteresis != null) {
                double hysteresis_multiplier = lastState ? 1.0 : -1.0;
                lowerBound -= hysteresis * hysteresis_multiplier;
                upperBound += hysteresis * hysteresis_multiplier;
            }
            return value >= lowerBound && value <= upperBound;
        }

        /**
         * Applies toggle semantics to a raw button input.
         *
         * <p>When toggle mode (isToggle) is enabled, a rising edge (pressed == true and lastState
         * == false) flips the internal isToggled flag. lastState is updated to the current pressed
         * value and the method returns the current toggled state.
         *
         * <p>When toggle mode is disabled, the method returns the raw pressed value.
         *
         * <p>Side effects: updates lastState and may update isToggled when a rising edge is
         * detected.
         *
         * @param pressed the current raw input state (true if the control is currently pressed)
         * @return the effective output state after applying toggle logic: - if isToggle is true,
         *     the current toggled state (isToggled); - otherwise, the raw pressed value
         *     <p>Implementation Note: This method is protected and not synchronized. If accessed
         *     concurrently from multiple threads, external synchronization is required to ensure
         *     correctness.
         */
        protected boolean applyToggle(boolean pressed) {
            if (isToggle) {
                if (pressed && !lastState) {
                    isToggled = !isToggled;
                }
                lastState = pressed;
                return isToggled;
            } else {
                return pressed;
            }
        }

        /**
         * Determines whether this controller input is considered "pressed".
         *
         * <p>This method: 1. Retrieves the prepared input value via {@code getPreparedValue()}. 2.
         * Clamps that value to the configured {@code minValue} and {@code maxValue}. 3. Evaluates
         * whether the clamped value meets the activation criteria using {@code testThreshold(...)}.
         * 4. Passes the resulting boolean through {@code applyToggle(...)} before returning, so the
         * final result may reflect any toggle state managed by the controller.
         *
         * @return {@code true} if the (possibly toggled) input state is considered pressed; {@code
         *     false} otherwise
         */
        public boolean isPressed() {
            double value = getPreparedValue();
            System.out.println("Prepared Value: " + value);
            boolean pressed = testThreshold(MathUtil.clamp(value, minValue, maxValue));
            return applyToggle(pressed);
        }

        /**
         * Returns a Supplier that provides the current "pressed" state of this controller. The
         * supplier delegates to this instance's {@link #isPressed()} method each time it is
         * invoked.
         *
         * @return a non-null {@code Supplier<Boolean>} which yields {@code true} when the
         *     controller is pressed and {@code false} otherwise
         */
        public Supplier<Boolean> getIsPressedSupplier() {
            return this::isPressed;
        }

        /**
         * Returns a {@link java.util.function.BooleanSupplier} that delegates to this instance's
         * {@code isPressed()} method.
         *
         * <p>Each invocation of the returned supplier calls {@code isPressed()} on this controller
         * and therefore reflects the controller's current pressed state at the time of the call.
         *
         * @return a {@link java.util.function.BooleanSupplier} that supplies {@code true} when this
         *     controller is pressed and {@code false} otherwise
         */
        public BooleanSupplier getPrimitiveIsPressedSupplier() {
            return this::isPressed;
        }

        /**
         * Returns the {@link Trigger} associated with this controller.
         *
         * @return the Trigger used by this controller
         */
        public Trigger getTrigger() {
            return trigger;
        }

        /**
         * Returns the numeric value representing the current pressed state of this controller
         * input.
         *
         * <p>This method queries {@link #isPressed()} at the time of invocation. If the input is
         * pressed, the method returns {@code maxValue}; otherwise it returns {@code minValue}.
         *
         * @return {@code maxValue} when {@link #isPressed()} is true, otherwise {@code minValue}
         */
        @Override
        public double getValue() {
            boolean isPressed = isPressed();
            return isPressed ? maxValue : minValue;
        }

        /**
         * Initializes this controller instance.
         *
         * <p>Performs superclass initialization and creates a {@code Trigger} that polls this
         * controller's pressed state on the CommandScheduler's default button loop. The resulting
         * trigger can be used to schedule commands or respond to button events.
         *
         * @param controller the controller to initialize
         */
        @Override
        public void initialize(Controller controller) {
            super.initialize(controller);
            if (minValue == null)
                minValue = (lowLevelControlElement != null) ? lowLevelControlElement.minValue : 0.0;
            if (maxValue == null)
                maxValue = (lowLevelControlElement != null) ? lowLevelControlElement.maxValue : 1.0;
            trigger =
                    new Trigger(
                            CommandScheduler.getInstance().getDefaultButtonLoop(), this::isPressed);
        }
    }

    /**
     * Represents an analog axis control element of a controller.
     *
     * <p>Axis instances model continuous two-directional inputs (for example joystick axes or
     * triggers treated as axes). The constructor ensures a sensible default range by initializing
     * {@code minValue} to -1.0 and {@code maxValue} to 1.0 when they are null.
     *
     * <p>The current axis reading is exposed via {@link #getValue()}, which returns the prepared
     * value (i.e., the result of {@link #getPreparedValue()}, after any normalization, deadbanding,
     * scaling or filtering provided by the superclass).
     *
     * <p>See {@link ControlElement} for shared behavior and lifecycle details.
     */
    public static class Axis extends ControlElement {

        /**
         * Returns the controller's current prepared value.
         *
         * <p>The returned value is the result of any preprocessing applied to the raw input (for
         * example: deadband removal, scaling, smoothing, or other transformations) performed by
         * getPreparedValue(). Use this method to obtain the controller's ready-to-use numeric
         * output for control logic.
         *
         * @return the prepared controller value after preprocessing
         * @see #getPreparedValue()
         */
        @Override
        public double getValue() {
            return getPreparedValue();
        }

        public void initialize(Controller controller) {
            super.initialize(controller);
            if (minValue == null)
                minValue =
                        (lowLevelControlElement != null) ? lowLevelControlElement.minValue : -1.0;
            if (maxValue == null)
                maxValue = (lowLevelControlElement != null) ? lowLevelControlElement.maxValue : 1.0;
        }
    }

    /**
     * A ControlElement representing a POV (point-of-view / hat) switch on a controller.
     *
     * <p>This element uses angular degrees to represent the hat position and reserves -1.0 to
     * indicate the hat is not pressed/centered. The constructor initializes sensible defaults for
     * the value range:
     *
     * <ul>
     *   <li>minValue = -1.0 (sentinel for "not pressed")
     *   <li>maxValue = 360.0 (maximum angle in degrees)
     * </ul>
     *
     * <p>getValue() returns the prepared/normalized value provided by
     * ControlElement#getPreparedValue(), typically yielding the current POV angle in degrees or
     * -1.0 when the POV is not engaged.
     */
    public static class POV extends ControlElement {

        /**
         * Returns the current prepared value for this controller.
         *
         * <p>This method delegates to {@link #getPreparedValue()} and exposes the numeric
         * representation of the controller's current state (for example, an axis or trigger
         * position). The returned value should reflect any preprocessing or normalization performed
         * by {@code getPreparedValue()}.
         *
         * @return the prepared controller value as a {@code double}
         * @see #getPreparedValue()
         */
        @Override
        public double getValue() {
            return getPreparedValue();
        }

        public void initialize(Controller controller) {
            super.initialize(controller);
            if (minValue == null)
                minValue =
                        (lowLevelControlElement != null) ? lowLevelControlElement.minValue : -1.0;
            if (maxValue == null)
                maxValue =
                        (lowLevelControlElement != null) ? lowLevelControlElement.maxValue : 360.0;
        }
    }

    /**
     * Initialize all ControlElement instances managed by this controller.
     *
     * <p>The method iterates over the controller's collection of control elements and calls each
     * element's initialize method, providing this controller as the initializing context. This
     * allows each ControlElement to perform setup such as binding callbacks, registering listeners,
     * or setting initial state that depends on the controller.
     *
     * <p>Subclasses may override to customize initialization order or behavior, but should call
     * super.initializeControlElements() if they want the default bulk-initialization behavior
     * preserved.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Assumes the controller's collection of elements is non-null; callers are responsible
     *       for ensuring the collection is initialized before invoking this method.
     *   <li>Exceptions thrown by individual elements' initialize implementations may prevent
     *       subsequent elements from being initialized; callers or overrides may choose to
     *       handle/log exceptions to ensure best-effort initialization.
     * </ul>
     */
    protected void initializeControlElements() {
        for (ControlElement controlElement : controllerElements.values()) {
            controlElement.initialize(this);
        }
    }
}
