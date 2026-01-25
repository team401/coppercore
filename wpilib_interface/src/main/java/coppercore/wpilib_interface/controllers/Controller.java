package coppercore.wpilib_interface.controllers;

import coppercore.parameter_tools.json.annotations.JSONExclude;
import coppercore.parameter_tools.json.annotations.JSONName;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
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
 * <p>This class encapsulates a physical controller (joystick/gamepad) connected to the robot and
 * a collection of logical control elements (buttons, axes, POVs) that are exposed to the rest of
 * the codebase by command names. A Controller is responsible for:
 *
 * <ul>
 *   <li>Storing the hardware port and controller type.</li>
 *   <li>Holding shorthand-to-id mappings for buttons, axes and POVs (used when resolving
 *       human-readable identifiers such as "A" or "left_x" to numeric hardware IDs).</li>
 *   <li>Maintaining a registry of low-level hardware bindings (LowLevelControlElement) and
 *       higher-level ControlElement wrappers (Button, Axis, POV) keyed by command name.</li>
 *   <li>Initializing all low-level and high-level elements so they become ready to read values
 *       from the DriverStation and to participate in the command scheduler loop.</li>
 * </ul>
 *
 * Usage and lifecycle
 * <ol>
 *   <li>A Controller is typically created by a JSON-to-Java loader (e.g. ControllerJsonRepresentation)
 *       which populates fields and shorthand maps.</li>
 *   <li>One or more ControlElement instances (which wrap LowLevelControlElement instances) are
 *       registered with this Controller using addControlElementForCommand(...).</li>
 *   <li>Once the Controller is fully configured and assigned a hardware port, initializeControlElements()
 *       should be called. This resolves textual IDs to numeric IDs, assigns the controller port
 *       into each low-level element, and initializes any button Triggers so they are wired into the
 *       CommandScheduler default button loop.</li>
 *   <li>After initialization the rest of the robot code can obtain Suppliers or primitive suppliers
 *       (for example via ControlElement#getSupplier or getPrimitiveSupplier, or Button#getIsPressedSupplier)
 *       to poll values or attach commands.</li>
 * </ol>
 *
 * Value transformation and behavior
 * <p>ControlElements support configurable inversion, clamping or linear remapping between arbitrary
 * input and output ranges. Low-level elements expose raw hardware reads (via DriverStation) and
 * provide helpers for deadband handling (axes) and POV semantics. Buttons additionally provide
 * thresholding, hysteresis, and optional toggle behavior; toggled state is persisted in the Button
 * instance and updated when the configured press threshold is crossed.
 *
 * Threading and performance
 * <p>Reading control values ultimately queries the DriverStation and is expected to be called from
 * robot-periodic contexts (main loop or default button loop). Calls are intended to be inexpensive,
 * but any heavy processing should be avoided in tight control loops. The Controller and its element
 * maps are not inherently thread-safe with respect to concurrent modification; construct and initialize
 * controllers on robot startup and avoid mutating them from multiple threads.
 *
 * Error handling
 * <p>During initialization low-level elements resolve their numeric IDs from shorthand maps or by
 * parsing the configured string ID. If resolution fails (for example the shorthand is missing and
 * the string is not an integer) initialization will throw a RuntimeException; callers should ensure
 * JSON/configuration correctness or catch and surface initialization errors during robot setup.
 *
 * Interactions with other systems
 * <ul>
 *   <li>Control elements create Trigger instances wired to CommandScheduler#getDefaultButtonLoop()
 *       when Buttons are initialized.</li>
 *   <li>LowLevelControlElement implementations call DriverStation API methods to obtain current
 *       hardware state.</li>
 * </ul>
 *
 * Convenience accessors
 * <p>Controller exposes typed lookup helpers (hasButton/getButton, hasAxis/getAxis, hasPOV/getPOV)
 * and a generic hasControlElement/getControlElement API for programs that treat controls polymorphically.
 *
 * See Also:
 * <ul>
 *   <li>Controller.LowLevelControlElement — raw hardware binding and value preparation helpers</li>
 *   <li>Controller.ControlElement, Controller.Button, Controller.Axis, Controller.POV — higher-level
 *       wrappers providing application-facing behavior (thresholds, toggles, deadbands, mapping)</li>
 * </ul>
 */
public class Controller {

    int port = -1;
    ControllerType controllerType;
    // initialized in ControllerJsonRepresentation.toJava
    HashMap<String, Integer> buttonShorthands = null;
    HashMap<String, Integer> axisShorthands = null;
    HashMap<String, Integer> povShorthands = null;
    private HashMap<String, ControlElement> controllerElements = new HashMap<>();
    HashMap<String, Button> buttons = new HashMap<>();
    HashMap<String, Axis> axes = new HashMap<>();
    HashMap<String, POV> povs = new HashMap<>();

    /**
     * Get the port number of the controller
     *
     * @return The port number
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

    @JsonType(
            property = "controllerType",
            subtypes = {
                @JsonSubtype(clazz = LowLevelButton.class, name = "button"),
                @JsonSubtype(clazz = LowLevelAxis.class, name = "axis"),
                @JsonSubtype(clazz = LowLevelPOV.class, name = "pov"),
            })

    /**
     * Low-level control element
     *
     * <p>These represents the raw, low-level control elements directly tied to the controller
     * hardware such as a button or an axis
     */
    public abstract static class LowLevelControlElement {
        public String elementType;

        @JSONName("id")
        public String stringID;

        public Boolean inverted = false;
        public Double minValue;
        public Double maxValue;
        public Boolean clampValue = false;
        @JSONExclude protected int id;
        @JSONExclude protected int port;

        /**
         * Adjusts an input value to the controller's configured output range.
         *
         * <p>If the controller is configured to clamp values (the {@code clampValue} flag),
         * the input {@code value} is constrained to lie within the controller's {@code minValue}
         * and {@code maxValue} bounds using a clamping operation. Otherwise, the input {@code value}
         * is linearly mapped from the source range defined by {@code oldMin}..{@code oldMax}
         * into the target range defined by {@code minValue}..{@code maxValue}.
         *
         * @param value the input value to be adjusted
         * @param oldMin the lower bound of the input (source) range
         * @param oldMax the upper bound of the input (source) range
         * @return the adjusted value either clamped to [minValue, maxValue] or scaled into that range
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
         * @param value  the raw input value to normalize and transform
         * @param oldMin the lower bound of the original input range (inclusive)
         * @param oldMax the upper bound of the original input range (inclusive)
         * @return the value clamped to the input range and then mapped/possibly inverted into the
         *         controller's configured output range
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
         * <ol>
         *   <li>Sets this.port to the value returned by controller.getPort().</li>
         *   <li>Resolves a numeric ID for this instance from the instance field {@code stringID}:
         *     <ul>
         *       <li>If {@code elementType} equals {@code "button"}, {@code "axis"} or {@code "pov"},
         *           the method first attempts to look up the numeric ID in the corresponding
         *           shorthand map on the provided {@code controller} ({@code buttonShorthands},
         *           {@code axisShorthands} or {@code povShorthands}).</li>
         *       <li>If no mapping is found, the method attempts to parse {@code stringID} as an
         *           integer using {@link Integer#parseInt(String)}.</li>
         *     </ul>
         *   </li>
         *   <li>When a numeric ID is successfully resolved, it is assigned to this instance's
         *       {@code id} field.</li>
         * </ol>
         *
         * <p>Note: {@code elementType} and {@code stringID} are expected to be fields of the
         * containing object; they are consulted during resolution.
         *
         * @param controller the Controller used to obtain the port and any shorthand mappings;
         *                   must not be {@code null}
         * @throws RuntimeException if {@code stringID} cannot be resolved to an integer via
         *                          the controller's shorthand maps or by parsing
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
         * <p>The exact meaning, units, and valid range of the returned value are implementation-dependent.
         * Concrete subclasses should document whether the value represents, for example, a joystick axis
         * (commonly in the range -1.0 to 1.0), a sensor measurement, a computed control output, or another quantity.
         *
         * <p>Implementations should aim for this method to be side-effect free and inexpensive to call,
         * as it may be invoked frequently by control loops, logging, or telemetry systems.
         *
         * @return the current value of this controller as a double
         */
        public abstract double getValue();
    }

    /**
     * Low-level control element representing a digital button on a joystick or controller.
     *
     * <p>This class maps a digital press/release state to a numeric value suitable for
     * the controller framework. By default the elementType is set to "button" and the
     * numeric range is initialized to [0.0, 1.0], reflecting the binary nature of a
     * button (released = 0.0, pressed = 1.0).
     *
     * <p>Instances are thin wrappers around the underlying hardware input and do not
     * cache state; callers should invoke {@code getValue()} from an appropriate control
     * context (for example the main robot loop) to obtain fresh readings.
     *
     * @see LowLevelControlElement
     */
    public static class LowLevelButton extends LowLevelControlElement {

        public LowLevelButton() {
            elementType = "button";
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 1.0;
        }

        /**
         * Returns the current numeric value for this controller input.
         *
         * <p>This method queries the DriverStation for the digital state of the button
         * identified by this controller's port and id. The boolean pressed state is
         * converted to a numeric value (1.0 when pressed, 0.0 when not pressed) and
         * then passed to {@code prepareValue(double, double, double)} with a dead zone
         * of 0.0 and a maximum of 1.0. Any normalization, filtering, or clamping done
         * by {@code prepareValue} will be applied to the result.
         *
         * <p>Note: This reads hardware state from the DriverStation; call it from
         * appropriate robot control contexts (for example the main control loop) and
         * avoid calling it excessively from non-real-time threads if {@code prepareValue}
         * is expensive.
         *
         * @return a normalized value in the range [0.0, 1.0]: 1.0 if the underlying
         *         button is pressed, 0.0 if not, after applying {@code prepareValue}.
         * @see edu.wpi.first.wpilibj.DriverStation#getStickButton(int, int)
         * @see #prepareValue(double, double, double)
         */
        public double getValue() {
            boolean pressed = DriverStation.getStickButton(port, id);
            return prepareValue((pressed) ? 1.0 : 0.0, 0.0, 1.0);
        }
    }

    /**
     * Raw axis interface
     *
     * <p>This represents an axis on the controller
     *
     * <p>Values range from -1.0 to 1.0
     *
     * <p>It includes deadband handling
     */
    public static class LowLevelAxis extends LowLevelControlElement {
        public Double deadband = 0.0;
        public Boolean remapDeadbanded = false;

        public LowLevelAxis() {
            elementType = "axis";
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 1.0;
        }

        /**
         * Applies the configured deadband to a joystick/controller input value.
         *
         * <p>Behavior depends on the instance flags and thresholds:
         * <ul>
         *   <li>If {@code remapDeadbanded} is {@code false}, the method simply clips small inputs:
         *       returns {@code 0.0} when {@code Math.abs(value) < deadband}, otherwise returns the
         *       original {@code value} unchanged.</li>
         *   <li>If {@code remapDeadbanded} is {@code true}, inputs inside the deadband are mapped to
         *       {@code 0.0}, while inputs outside the deadband are linearly rescaled so that the
         *       remaining range {@code [deadband, 1.0]} is remapped to {@code [0.0, 1.0]}. The original
         *       sign of {@code value} is preserved.</li>
         * </ul>
         *
         * <p>Example (when remapping enabled):
         * adjustedValue = (|value| - deadband) / (1.0 - deadband); result = sign(value) * adjustedValue
         *
         * @param value the input value to process (commonly in the range -1.0 to 1.0)
         * @return the value after applying the deadband and optional remapping. The return value will
         *         be {@code 0.0} for inputs within the deadband, and otherwise will preserve the sign of
         *         the original input. When remapping is enabled, non-zero outputs are scaled to occupy
         *         the full output range outside the deadband.
         *
         * @implNote This method relies on the instance fields {@code deadband} (expected in the range
         *           [0.0, 1.0)) and {@code remapDeadbanded}. If {@code deadband} is equal to or greater
         *           than {@code 1.0}, remapping will cause a division by zero; such values are not
         *           supported and should be avoided or validated elsewhere.
         */
        protected double applyDeadband(double value) {
            if (remapDeadbanded == false) {
                return (Math.abs(value) < deadband) ? 0.0 : value;
            }
            if (Math.abs(value) < deadband) {
                return 0.0;
            } else {
                double sign = Math.signum(value);
                double adjustedValue = (Math.abs(value) - deadband) / (1.0 - deadband);
                return sign * adjustedValue;
            }
        }

        /**
         * Returns the current processed axis value for this controller.
         *
         * <p>This method reads the raw axis value from the DriverStation using this
         * controller's configured port and axis id, applies the configured deadband to
         * ignore small inputs, and then prepares the value (for example scaling or
         * clamping) into the configured minimum/maximum range via {@code prepareValue}.
         *
         * <p>The raw axis value is typically in the range [-1.0, 1.0]; the returned
         * value will be constrained to [minValue, maxValue] after preparation.
         *
         * @return the processed axis value after deadband filtering and range preparation
         */
        public double getValue() {
            double value = DriverStation.getStickAxis(port, id);
            return prepareValue(applyDeadband(value), minValue, maxValue);
        }
    }

    /**
     * Raw POV interface
     *
     * <p>This represents a POV (D-pad) on the controller
     *
     * <p>Values range from 0 to 360 degrees. The value is -1 when not pressed
     */
    public static class LowLevelPOV extends LowLevelControlElement {

        public LowLevelPOV() {
            elementType = "pov";
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 360.0;
        }

        public double getValue() {
            return prepareValue(DriverStation.getStickPOV(port, id), -1, 360);
        }
    }

    @JsonType(
            property = "commandType",
            subtypes = {
                @JsonSubtype(clazz = Button.class, name = "button"),
                @JsonSubtype(clazz = Axis.class, name = "axis"),
                @JsonSubtype(clazz = POV.class, name = "pov"),
            })
    /**
     * Control element base class
     *
     * <p>These represents the ControlElements that the code will interact with. They wrap around
     * the low-level, raw interfaces to provide additional functionality Such as thresholds,
     * hysteresis, and toggling for buttons. Also includes range adjustment and inversion for all
     * interfaces
     */
    public abstract static class ControlElement {
        @JSONName("controllerInterface")
        LowLevelControlElement lowLevelControlElement;

        public String command;
        public String commandType;
        public Boolean inverted = false;
        public Double minValue;
        public Double maxValue;
        public Boolean clampValue = false;

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

        protected double getPreparedValue() {
            return applyInversion(
                    fixRange(lowLevelControlElement.getValue()), inverted, minValue, maxValue);
        }

        /**
         * Get the value of the controller interface
         *
         * @return The value
         */
        public abstract double getValue();

        /**
         * Initialize the controller interface
         *
         * @param controller The controller to initialize with
         */
        public void initialize(Controller controller) {
            lowLevelControlElement.initialize(controller);
        }

        /**
         * Get a Supplier for the value of the controller interface
         *
         * @return The Supplier
         */
        public Supplier<Double> getSupplier() {
            return this::getValue;
        }

        /**
         * Get a DoubleSupplier for the value of the controller interface
         *
         * @return The DoubleSupplier
         */
        public DoubleSupplier getPrimitiveSupplier() {
            return this::getValue;
        }
    }

    /**
     * Button interface
     *
     * <p>This represents a button for code interaction
     *
     * <p>This includes thresholds, hysteresis, and toggling
     */
    public static class Button extends ControlElement {
        public Double threshold =
                0.0; // This is the value above which the button is considered pressed
        public Double thresholdRange = null; // This is the range above the threshold for hysteresis
        public Double hysteresis =
                null; // This is the amount the value must drop below the threshold to be considered
        // released
        public Boolean isToggle = false;
        public Boolean isToggled = false;
        public Boolean lastState = false;
        public Trigger trigger;

        // Maybe optimize this by storing the bounds instead of calculating them every time
        protected boolean testThreshold(double value) {
            double lowerBound = threshold;
            double upperBound = threshold;
            if (thresholdRange != null) {
                lowerBound = threshold - thresholdRange / 2.0;
                upperBound = threshold + thresholdRange / 2.0;
            }
            if (hysteresis != null) {
                double hysteresis_multiplier = lastState ? 1.0 : -1.0;
                lowerBound -= hysteresis * hysteresis_multiplier;
                upperBound += hysteresis * hysteresis_multiplier;
            }
            return value >= lowerBound && value <= upperBound;
        }

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

        public Button() {
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 1.0;
        }

        public boolean isPressed() {
            double value = getPreparedValue();
            boolean pressed = testThreshold(MathUtil.clamp(value, minValue, maxValue));
            return applyToggle(pressed);
        }

        public Supplier<Boolean> getIsPressedSupplier() {
            return this::isPressed;
        }

        public BooleanSupplier getPrimitiveIsPressedSupplier() {
            return this::isPressed;
        }

        public Trigger getTrigger() {
            return trigger;
        }

        @Override
        public double getValue() {
            boolean isPressed = isPressed();
            return isPressed ? maxValue : minValue;
        }

        @Override
        public void initialize(Controller controller) {
            super.initialize(controller);
            trigger =
                    new Trigger(
                            CommandScheduler.getInstance().getDefaultButtonLoop(), this::isPressed);
        }
    }

    /**
     * Axis interface
     *
     * <p>This represents an axis for code interaction
     */
    public static class Axis extends ControlElement {
        public Axis() {
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 1.0;
        }

        @Override
        public double getValue() {
            return getPreparedValue();
        }
    }

    /**
     * POV interface
     *
     * <p>This represents a POV for code interaction
     */
    public static class POV extends ControlElement {
        public POV() {
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 360.0;
        }

        @Override
        public double getValue() {
            return getPreparedValue();
        }
    }

    /** Finish loading the controller by initializing all controller interfaces */
    protected void initializeControlElements() {
        for (ControlElement controlElement : controllerElements.values()) {
            controlElement.initialize(this);
        }
    }
}
