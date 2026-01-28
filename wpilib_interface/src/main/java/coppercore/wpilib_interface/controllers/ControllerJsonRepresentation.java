package coppercore.wpilib_interface.controllers;

import coppercore.parameter_tools.json.helpers.JSONObject;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

/**
 * JSON representation of a Controller
 *
 * <p>This class is used for JSON serialization and deserialization of Controller objects The toJava
 * method converts the JSON representation back to a Controller object.
 *
 * <p>This class should not be instantiated directly
 */
public class ControllerJsonRepresentation extends JSONObject<Controller> {

    int port;
    String type;
    List<Controller.ControlElement> controlElements;
    HashMap<String, Integer> buttonShorthands = new HashMap<>();
    HashMap<String, Integer> axisShorthands = new HashMap<>();
    HashMap<String, Integer> povShorthands = new HashMap<>();

    /**
     * Constructor for JSON deserialization
     *
     * @param controller
     */
    public ControllerJsonRepresentation(Controller controller) {
        super(controller);
        throw new RuntimeException("This method should not be called directly");
    }

    @Override
    /** Convert json representation to Java object */
    public Controller toJava() {
        Controller controller = new Controller();
        controller.port = this.port;
        controller.controllerType = ControllerType.getControllerType(this.type);
        controller.buttonShorthands =
                new HashMap<String, Integer>(controller.controllerType.ButtonShorthands);
        controller.axisShorthands =
                new HashMap<String, Integer>(controller.controllerType.AxisShorthands);
        controller.povShorthands =
                new HashMap<String, Integer>(controller.controllerType.POVShorthands);

        if (this.buttonShorthands != null) {
            controller.buttonShorthands.putAll(this.buttonShorthands);
        }
        if (this.axisShorthands != null) {
            controller.axisShorthands.putAll(this.axisShorthands);
        }
        if (this.povShorthands != null) {
            controller.povShorthands.putAll(this.povShorthands);
        }

        for (Controller.ControlElement controlElement : this.controlElements) {
            switch (controlElement.commandType) {
                case "button":
                    controller.buttons.put(
                            controlElement.command, (Controller.Button) controlElement);
                    break;
                case "axis":
                    controller.axes.put(controlElement.command, (Controller.Axis) controlElement);
                    break;
                case "pov":
                    controller.povs.put(controlElement.command, (Controller.POV) controlElement);
                    break;
                default:
                    throw new RuntimeException(
                            "Unknown control element type: " + controlElement.commandType);
            }
            controller.addControlElementForCommand(controlElement);
        }

        controller.initializeControlElements();

        return controller;
    }

    public static Constructor<? extends JSONObject<?>> getConstructor()
            throws NoSuchMethodException {
        return ControllerJsonRepresentation.class.getConstructor(Controller.class);
    }
}
