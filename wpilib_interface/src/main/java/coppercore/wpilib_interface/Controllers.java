package coppercore.wpilib_interface;

import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONExclude;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class Controllers {
    public Map<String, Integer> buttonShorthands;
    public Map<String, Integer> axesShorthands;
    public List<Controller> controllers = null;

    @JSONExclude
    public static JSONSync<Controllers> synced =
            new JSONSync<Controllers>(
                    new Controllers(), "filePath", new JSONSyncConfigBuilder().build());

    public static class Controller {
        public Integer port = -1;
        public String type = null;
        public Boolean hasPov = false;
        public List<Button> buttons = null;
        public List<Axis> axes = null;
        @JSONExclude public transient CommandGenericHID commandHID;

        public IntSupplier getPov() {
            return () -> commandHID.getHID().getPOV();
        }

        public void setupController() {
            if (port < 0 || port > 5)
                throw new RuntimeException("Invalid port must be between 0 and 5 " + port);
            commandHID =
                    switch (type) {
                        case "joystick" -> commandHID = new CommandJoystick(port);
                        case "xbox" -> commandHID = new CommandXboxController(port);
                        default -> throw new RuntimeException("Invalid controller type " + type);
                    };
            setupControlInputs();
        }

        public void setupControlInputs() {
            for (Button button : buttons) button.setupTrigger(commandHID);
            for (Axis axis : axes) axis.setupAxis(commandHID);
        }

        public Trigger getButton(String command) {
            for (Button button : buttons) {
                if (button.command.equals(command)) return button.trigger;
            }
            throw new RuntimeException("Button not found " + command);
        }

        public Trigger getButton(int id) {
            return commandHID.button(id);
        }

        public boolean hasButton(String command) {
            for (Button button : buttons) {
                if (button.command.equals(command)) return true;
            }
            return false;
        }

        public boolean hasAxis(String command) {
            for (Axis axis : axes) {
                if (axis.command.equals(command)) return true;
            }
            return false;
        }

        public DoubleSupplier getAxis(String command) {
            for (Axis axis : axes) {
                if (axis.command.equals(command)) return axis.getSupplier();
            }
            throw new RuntimeException("Axis not found " + command);
        }

        public DoubleSupplier getAxis(int axisNum) {
            for (Axis axis : axes) {
                if (axis.axisNum == axisNum) return axis.getSupplier();
            }
            throw new RuntimeException("Axis not found " + axisNum);
        }
    }

    public static class Button {
        public String command = null;
        public String button = null;
        public Boolean isPov = false;
        @JSONExclude public Trigger trigger = null;

        public void setupTrigger(CommandGenericHID commandHID) {
            int id;
            try {
                id = Integer.valueOf(button, 10);
            } catch (NumberFormatException e) {
                if (!Controllers.synced.getObject().buttonShorthands.containsKey(button)) {

                    throw new RuntimeException(
                            "Button ID not found as integer or in shorthands " + button);
                }
                id = Controllers.synced.getObject().buttonShorthands.get(button);
            }
            if (isPov) trigger = commandHID.pov(id);
            else trigger = commandHID.button(id);
        }
    }

    public static class Axis {
        public String command = null;
        public String axis = null;
        public Boolean negate = false;
        @JSONExclude public int axisNum = -1;
        @JSONExclude public DoubleSupplier supplier = null;

        public void setupAxis(CommandGenericHID commandHID) {
            try {
                axisNum = Integer.valueOf(axis, 10);
            } catch (NumberFormatException e) {
                if (!Controllers.synced.getObject().axesShorthands.containsKey(axis))
                    throw new RuntimeException("Axis ID not found " + axis);
                axisNum = Controllers.synced.getObject().axesShorthands.get(axis);
            }
            supplier = () -> ((negate) ? -1 : 1) * commandHID.getRawAxis(axisNum);
        }

        public DoubleSupplier getSupplier() {
            return supplier;
        }
    }

    public static List<Controller> getControllers() {
        return synced.getObject().controllers;
    }

    public static void setupControllers() {
        for (Controller controller : synced.getObject().controllers) {
            controller.setupController();
        }
    }

    public static void loadControllers() {
        synced.loadData();
        setupControllers();
    }
}
