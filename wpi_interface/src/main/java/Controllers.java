import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class Controllers {
    public static Map<String, Integer> buttonShorthands;
    public static Map<String, Integer> axesShorthands;
    public static List<Controller> controllers = null;

    static class Controller {
        public int port = -1;
        public String type = null;
        public boolean hasPov = false;
        public List<Button> buttons = null;
        public List<Axis> axes = null;
        //public transient JsonSync synced = ------;
        public transient CommandGenericHID commandHID;

        public IntSupplier getPov() {
            return () -> commandHID.getHID().getPOV();
        }

        public void setupController() {
            if (port < 1 || port > 5)
                throw new RuntimeException("Invalid port must be between 1 and 5 " + port);
            commandHID = switch (type) {
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

    private static class Button {
        public String command = null;
        public String button = null;
        public boolean isPov = false;
        public transient Trigger trigger = null;

        public void setupTrigger(CommandGenericHID commandHID) {
            int id;
            try {
                id = Integer.valueOf(button, 10);
            } catch (NumberFormatException e) {
                if (!buttonShorthands.containsKey(button))
                    throw new RuntimeException(
                            "Button Id not found as interger or in shorthands " + button);
                id = buttonShorthands.get(button);
            }
            if (isPov) trigger = commandHID.pov(id);
            else trigger = commandHID.button(id);
        }
    }

    private static class Axis {
        public String command = null;
        public String axis = null;
        public boolean negate = false;
        public transient int axisNum = -1;
        public transient DoubleSupplier supplier = null;

        public void setupAxis(CommandGenericHID commandHID) {
            try {
                axisNum = Integer.valueOf(axis, 10);
            } catch (NumberFormatException e) {
                if (!buttonShorthands.containsKey(axis))
                    throw new RuntimeException(
                            "Axis Id not found " + axis);
                axisNum = buttonShorthands.get(axis);
            }
            supplier = () -> ((negate)? 1: -1) * commandHID.getRawAxis(axisNum);
        }

        public DoubleSupplier getSupplier() {
            return supplier;
        }
    }

    public static List<Controller> getControllers() {
        return controllers;
    }

    public static void setupControllers() {
        for (Controller controller : controllers) {
            controller.setupController();
        }
    }

    public static void loadControllers() {
        
        //synced.loadData();

        setupControllers();
    }
}
