package coppercore.wpilib_interface.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * This class manages controller types and a collection of controllers
 * It includes methods to register and retrieve controller types
 * It also includes methods to get controllers and their interfaces by command
 */
public class Controllers {

    private static HashMap<String, ControllerType> controllerTypes = new HashMap<>();

    /**
     * Register a controller type with a simple constructor function
     * @param name The name of the controller type
     */
    public static void registerControllerType(String name){
        registerControllerType(
            name,
            ControllerType.createSimpleControllerType(name)
        );
    }

    /**
     * Register a controller type
     * @param name The name of the controller type
     * @param type The controller type
     */
    public static void registerControllerType(String name, ControllerType type) {
        controllerTypes.put(name, type);
    }

    /**
     * Get a registered controller type
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

    // Will add more fields later
    public List<Controller> controllers = List.of();

    /**
     * Creates an empty Controllers object
     */
    public Controllers() {
        this.controllers = new ArrayList<>();
    }

    /**
     * Creates a Controllers object with the given controllers
     * @param controllers
     */
    public Controllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     * Get controller by index
     * @param index The index of the controller
     * @return The controller at the given index
     */
    public Controller getControllerByIndex(int index) {
        return controllers.get(index);
    }

    /**
     * Get controller by port
     * @param port The port of the controller
     * @return The controller at the given port
     */
    public Controller getControllerByPort(int port){
        for(Controller controller : controllers){
            if(controller.port == port){
                return controller;
            }
        }
        throw new RuntimeException("No controller found on port: " + port);
    }

    /**
     * Get controller interface by command
     * @param func Function to check if controller has the interface
     * @param getter Function to get the interface from the controller
     * @param command The command to get the interface for
     * @return The controller interface
     */
    private Controller.ControllerInterface getFromAllControllers(
            BiFunction<Controller, String, Boolean> func,
            BiFunction<Controller, String, Controller.ControllerInterface> getter,
            String command) {
        for (Controller controller : controllers) {
            if (func.apply(controller, command)) {
                return getter.apply(controller, command);
            }
        }
        throw new RuntimeException("No controller interface found for command: " + command);
    }

    /**
     * Get controller interface by command
     * @param command The command to get the interface for
     * @return The controller interface
     */
    public Controller.ControllerInterface getInterface(String command) {
        return getFromAllControllers(
                (controller, cmd) -> controller.hasControllerInterface(cmd),
                (controller, cmd) -> controller.getControllerInterface(cmd),
                command);
    }

    /**
     * Get button by command
     * @param command The command to get the button for
     * @return The button
     */
    public Controller.Button getButton(String command) {
        return (Controller.Button) getFromAllControllers(
            (controller, cmd) -> controller.hasButton(cmd),
            (controller, cmd) -> controller.getButton(cmd),
            command
        );
    }

    /**
     * Get axis by command
     * @param command The command to get the axis for
     * @return The axis
     */
    public Controller.Axis getAxis(String command) {
        return (Controller.Axis) getFromAllControllers(
            (controller, cmd) -> controller.hasAxis(cmd),
            (controller, cmd) -> controller.getAxis(cmd),
            command
        );
    }

    /**
     * Get POV by command
     * @param command The command to get the POV for
     * @return The POV
     */
    public Controller.POV getPOV(String command) {
        return (Controller.POV) getFromAllControllers(
            (controller, cmd) -> controller.hasPOV(cmd),
            (controller, cmd) -> controller.getPOV(cmd),
            command
        );
    }
}
