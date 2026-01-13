package coppercore.wpilib_interface.controllers;

import java.util.HashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import coppercore.parameter_tools.json.annotations.AfterJsonLoad;
import coppercore.parameter_tools.json.annotations.JSONExclude;
import coppercore.parameter_tools.json.annotations.JSONName;
import coppercore.parameter_tools.json.annotations.JsonSubtype;
import coppercore.parameter_tools.json.annotations.JsonType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.button.Trigger;

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


    public int getPort() {
        return port;
    }

    public ControllerType getControllerType() {
        return controllerType;
    }

    public ControllerInterface getControllerInterface(String command) {
        return controllerInterfaces.get(command);
    }

    private static double clampRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Might be able to optimize this
    private static double adjustRange(double value, double oldMin, double oldMax, double newMin, double newMax) {
        // Ensure value is inside old range
        double clampedValue = clampRange(value, newMin, newMax);
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
            return (clampValue) ? clampRange(value, minValue, maxValue) : adjustRange(value, oldMin, oldMax, minValue, maxValue);
        }

        protected double applyInversion(double value) {
            if (inverted) {
                return maxValue + minValue - value;
            } else {
                return value;
            }
        }

        protected double prepareValue(double value, double oldMin, double oldMax) {
            return applyInversion(fixRange(value, oldMin, oldMax));
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

    public static class RawPOV extends RawControllerInterface {

        public RawPOV() {
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 360.0;
        }

        public double getValue() {
            return prepareValue(DriverStation.getStickPOV(port, id), 0, 360);
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
            return (clampValue) ? clampRange(value, minValue, maxValue) : adjustRange(value, rawInterface.minValue, rawInterface.maxValue, minValue, maxValue);
        }

        protected double applyInversion(double value) {
            // Need to check this formula
            return (inverted) ? maxValue + minValue - value : value;
        }

        protected double getPreparedValue() {
            return applyInversion(fixRange(rawInterface.getValue()));
        }

        public abstract double getValue();

        public void initilizeInterface(Controller controller) {
            rawInterface.initilizeInterface(controller);
        }
    }

    
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
            boolean pressed = testThreshold(clampRange(value, minValue, maxValue));
            return applyToggle(pressed);
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
            trigger = new Trigger(this::isPressed);
        }
    }

    public static class Axis extends ControllerInterface {
        public Axis() {
            if (minValue == null) minValue = -1.0;
            if (maxValue == null) maxValue = 1.0;
        }

        @Override
        public double getValue() {
            return getPreparedValue();
        }

        public Supplier<Double> getSupplier() {
            return () -> getValue();
        }

        public DoubleSupplier getPrimitiveSupplier() {
            return this::getValue;
        }
    }

    public static class POV extends ControllerInterface {
        public POV() {
            if (minValue == null) minValue = 0.0;
            if (maxValue == null) maxValue = 360.0;
        }

        @Override
        public double getValue() {
            return getPreparedValue();
        }
    }

    protected void finishControllerLoading() {
        for (ControllerInterface controllerInterface : controllerInterfaces.values()) {
            controllerInterface.initilizeInterface(this);
        }
    }
}
