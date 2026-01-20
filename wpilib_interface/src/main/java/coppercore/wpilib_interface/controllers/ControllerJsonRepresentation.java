package coppercore.wpilib_interface.controllers;

import coppercore.parameter_tools.json.helpers.JSONObject;
import java.util.HashMap;
import java.util.List;

/**
 * JSON representation of a Controller
 * 
 * This class is used for JSON serialization and deserialization of Controller objects
 * The toJava method converts the JSON representation back to a Controller object
 * This class should not be instantiated directly
 */
public class ControllerJsonRepresentation extends JSONObject<Controller> {
    
    int port;
    String type;
    List<Controller.ControllerInterface> controllerInterfaces;
    HashMap<String, Integer> buttonShorthands = new HashMap<>();
    HashMap<String, Integer> axisShorthands = new HashMap<>();
    HashMap<String, Integer> povShorthands = new HashMap<>();
    
    /**
     * Constructor for JSON deserialization
     * @param controller
     */
    public ControllerJsonRepresentation(Controller controller) {
        super(controller);
        throw new RuntimeException("This method should not be called");
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Convert json representation to Java object
     */
    public Controller toJava() {
        Controller controller = new Controller();
        controller.port = this.port;
        controller.controllerType = Controllers.getControllerType(this.type);
        controller.buttonShorthands =
                new HashMap<String, Integer>(controller.controllerType.ButtonShorthands);
        controller.axisShorthands =
                new HashMap<String, Integer>(controller.controllerType.AxisShorthands);
        controller.povShorthands =
                new HashMap<String, Integer>(controller.controllerType.POVShorthands);

        controller.buttonShorthands.putAll(this.buttonShorthands);
        controller.axisShorthands.putAll(this.axisShorthands);
        controller.povShorthands.putAll(this.povShorthands);

        for (Controller.ControllerInterface controllerInterface : this.controllerInterfaces) {
            switch (controllerInterface.commandType) {
                case "button":
                    controller.buttons.put(
                            controllerInterface.command, (Controller.Button) controllerInterface);
                    break;
                case "axis":
                    controller.axes.put(
                            controllerInterface.command, (Controller.Axis) controllerInterface);
                    break;
                case "pov":
                    controller.povs.put(
                            controllerInterface.command, (Controller.POV) controllerInterface);
                    break;
                default:
                    throw new RuntimeException(
                            "Unknown controller interface type: "
                                    + controllerInterface.commandType);
            }
            controller.controllerInterfaces.put(controllerInterface.command, controllerInterface);
        }

        controller.finishControllerLoading();

        return controller;
    }
}
