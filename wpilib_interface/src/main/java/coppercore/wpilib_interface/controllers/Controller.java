package coppercore.wpilib_interface.controllers;

import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import coppercore.parameter_tools.json.annotations.JSONExclude;
import coppercore.parameter_tools.json.annotations.JSONName;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * Controller class
 * 
 * This represents a physical controller and its interfaces
 * It includes methods to get the controller interfaces
 * Within each interface, there are methods to get the value of the interface
 * Also includes raw interfaces that directly interact with the hardware
 * 
 * Gets input from WPILib's DriverStation class
 */
public class Controller {
    
    int port = -1;
    ControllerType controllerType;
    HashMap<String, Integer> buttonShorthands = null;
    HashMap<String, Integer> axisShorthands = null;
    HashMap<String, Integer> povShorthands = null;
    HashMap<String, ControllerInterface> controllerInterfaces = new HashMap<>();
    HashMap<String, Button> buttons = new HashMap<>();
    HashMap<String, Axis> axes = new HashMap<>();
    HashMap<String, POV> povs = new HashMap<>();

    /**
     * Get the port number of the controller
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the type of the controller
     * @return The controller type
     */
    public ControllerType getControllerType() {
        return controllerType;
    }

    /**
     * Check if the controller has a controller interface for the given command
     * @param command The command to check
     * @return True if the controller has the interface, false otherwise
     */
    public boolean hasControllerInterface(String command) {
        return controllerInterfaces.containsKey(command);
    }

    /**
     * Get the controller interface for the given command
     * @param command The command to get the interface for
     * @return The controller interface
     */
    public ControllerInterface getControllerInterface(String command) {
        return controllerInterfaces.get(command);
    }

    /**
     * Check if the controller has a button for the given command
     * @param command The command to check
     * @return True if the controller has the button, false otherwise
     */
    public boolean hasButton(String command) {
        return buttons.containsKey(command);
    }

    /**
     * Get the button for the given command
     * @param command The command to get the button for
     * @return The button
     */
    public Button getButton(String command) {
        return buttons.get(command);
    }

    /**
     * Check if the controller has an axis for the given command
     * @param command The command to check
     * @return True if the controller has the axis, false otherwise
     */
    public boolean hasAxis(String command) {
        return axes.containsKey(command);
    }

    /**
     * Get the axis for the given command
     * @param command The command to get the axis for
     * @return The axis
     */
    public Axis getAxis(String command) {
        return axes.get(command);
    }

    /**
     * Check if the controller has a POV for the given command
     * @param command The command to check
     * @return True if the controller has the POV, false otherwise
     */
    public boolean hasPOV(String command) {
        return povs.containsKey(command);
    }

    /**
     * Get the POV for the given command
     * @param command The command to get the POV for
     * @return The POV
     */
    public POV getPOV(String command) {
        return povs.get(command);
    }

    private static double applyInversion(double value, boolean inverted, double minValue, double maxValue) {
        return (inverted) ? maxValue + minValue - value : value;
    }

    // Might be able to optimize this
    private static double adjustRange(double value, double oldMin, double oldMax, double newMin, double newMax) {
        // Ensure value is inside old range
        double clampedValue = MathUtil.clamp(value, newMin, newMax);
        // Need to double check this formula
        double t = (clampedValue - oldMin) / (oldMax - oldMin);
        // Also need to double check this formula
        return t * (newMax - newMin) + newMin;
    }

    @JsonType(
        property = "controllerType",
        subtypes = {
            @JsonSubtype(clazz = RawButton.class, name = "button"),
            @JsonSubtype(clazz = RawAxis.class, name = "axis"),
            @JsonSubtype(clazz = RawPOV.class, name = "pov"),
        }
    )
    
    /**
     * Raw controller interface class
     * 
     * These represents the raw interfaces directly tied to the controller hardware
     */
    public static abstract class RawControllerInterface {
        public String controllerType;
        @JSONName("id")
        public String stringID;
        public Boolean inverted = false;
        public Double minValue;
        public Double maxValue;
        public Boolean clampValue = false;
        @JSONExclude
        protected int id;
        @JSONExclude
        protected int port;

        protected double fixRange(double value, double oldMin, double oldMax) {
            return (clampValue) ? MathUtil.clamp(value, minValue, maxValue) : adjustRange(value, oldMin, oldMax, minValue, maxValue);
        }

        protected double prepareValue(double value, double oldMin, double oldMax) {
            return applyInversion(fixRange(value, oldMin, oldMax), inverted, minValue, maxValue);
        }

        public void initilizeInterface(Controller controller) {
            this.port = controller.getPort();

            Integer resolvedID = null;
            switch (controllerType) {
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
                    throw new RuntimeException("Could not resolve ID for controller interface with string ID: " + stringID);
                }
            }
            this.id = resolvedID;
        }

        public abstract double getValue();
    }

    /**
     * Raw button interface
     * 
     * This represents a button on the controller
     * 
     * Values range from 0.0 (not pressed) to 1.0 (pressed)
     * 
     * It returns 1.0 when pressed and 0.0 when not pressed
     * It also includes no debouncing or toggling
     * 
     */
    public static class RawButton extends RawControllerInterface {

        public RawButton() {
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 1.0;
        }

        public double getValue() {
            boolean pressed = DriverStation.getStickButton(port, id);
            return prepareValue((pressed) ? 1.0 : 0.0, 0.0, 1.0);
        }

    }

    /**
     * Raw axis interface
     * 
     * This represents an axis on the controller
     * 
     * Values range from -1.0 to 1.0 
     * 
     * It includes deadband handling
     * 
     */
    public static class RawAxis extends RawControllerInterface {
        public Double deadband = 0.0;
        public Boolean remapDeadbanded = false;

        public RawAxis() {
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 1.0;
        }

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

        public double getValue() {
            double value = DriverStation.getStickAxis(port, id);
            return prepareValue(applyDeadband(value), minValue, maxValue);
        }
    }

    /**
     * Raw POV interface
     * 
     * This represents a POV (D-pad) on the controller
     * 
     * Values range from 0 to 360 degrees
     * It returns -1 when not pressed
     */
    public static class RawPOV extends RawControllerInterface {

        public RawPOV() {
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
     }
    )
    /**
     * Controller interface class
     * 
     * These represents the interfaces that the code will interact with
     */
    public static abstract class ControllerInterface {
        @JSONName("controllerInterface")
        RawControllerInterface rawInterface;
        public String command;
        public String commandType;
        public Boolean inverted = false;
        public Double minValue;
        public Double maxValue;
        public Boolean clampValue = false;

        protected double fixRange(double value) {
            return (clampValue) ? MathUtil.clamp(value, minValue, maxValue) : adjustRange(value, rawInterface.minValue, rawInterface.maxValue, minValue, maxValue);
        }

        protected double getPreparedValue() {
            return applyInversion(fixRange(rawInterface.getValue()), inverted, minValue, maxValue);
        }

        /**
         * Get the value of the controller interface
         * @return The value
         */
        public abstract double getValue();

        /**
         * Initialize the controller interface
         * @param controller The controller to initialize with
         */
        public void initilizeInterface(Controller controller) {
            rawInterface.initilizeInterface(controller);
        }

        /**
         * Get a Supplier for the value of the controller interface
         * @return The Supplier
         */
        public Supplier<Double> getSupplier() {
            return this::getValue;
        }

        /**
         * Get a DoubleSupplier for the value of the controller interface
         * @return The DoubleSupplier
         */
        public DoubleSupplier getPrimitiveSupplier() {
            return this::getValue;
        }
    }

    /**
     * Button interface
     * 
     * This represents a button for code interaction
     * 
     * This includes thresholds, hysteresis, and toggling
     */
    public static class Button extends ControllerInterface {
        public Double threshold = 0.0; // This is the value above which the button is considered pressed
        public Double thresholdRange = null; // This is the range above the threshold for hysteresis
        public Double hysteresis = null; // This is the amount the value must drop below the threshold to be considered released
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
        public void initilizeInterface(Controller controller) {
            super.initilizeInterface(controller);
            trigger = new Trigger(CommandScheduler.getInstance().getDefaultButtonLoop(), this::isPressed);
        }
    }

    /**
     * Axis interface
     * 
     * This represents an axis for code interaction
     */
    public static class Axis extends ControllerInterface {
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
     * This represents a POV for code interaction
     */
    public static class POV extends ControllerInterface {
        public POV() {
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 360.0;
        }

        @Override
        public double getValue() {
            return getPreparedValue();
        }

    }

    /**
     * Finish loading the controller by initializing all controller interfaces
     */
    protected void finishControllerLoading() {
        for (ControllerInterface controllerInterface : controllerInterfaces.values()) {
            controllerInterface.initilizeInterface(this);
        }
    }
}
