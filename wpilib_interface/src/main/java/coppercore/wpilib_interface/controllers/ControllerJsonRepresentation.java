package coppercore.wpilib_interface.controllers;

import coppercore.parameter_tools.json.helpers.JSONObject;
import java.util.HashMap;
import java.util.List;

/**
 * JSON representation of a Controller
 *
 * <p>This class is used for JSON serialization and deserialization of Controller objects The toJava
 * method converts the JSON representation back to a Controller object This class should not be
 * instantiated directly
 */
public class ControllerJsonRepresentation extends JSONObject<Controller> {

    int port;
    String type;
    List<Controller.ControlElement> controllerElements;
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
        throw new RuntimeException("This method should not be called");
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

        controller.buttonShorthands.putAll(this.buttonShorthands);
        controller.axisShorthands.putAll(this.axisShorthands);
        controller.povShorthands.putAll(this.povShorthands);

        for (Controller.ControlElement controlElement : this.controllerElements) {
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
                            "Unknown controller interface type: " + controlElement.commandType);
            }
            controller.controllerElements.put(controlElement.command, controlElement);
        }

        controller.finishControllerLoading();

        return controller;
    }
}
