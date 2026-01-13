package coppercore.wpilib_interface.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import edu.wpi.first.wpilibj.GenericHID;

/**
 * 
 */
public class Controllers {

    public static HashMap<String, ControllerType> controllerTypes = new HashMap<>();

    public static void registerControllerType(String name, Function<Integer, GenericHID> hidFunction){
        registerControllerType(
            name,
            ControllerType.createSimpleControllerType(name)
        );
    }

    public static void registerControllerType(String name, ControllerType type){
        controllerTypes.put(name, type);
    }

    public static ControllerType getControllerType(String name){
        ControllerType type = controllerTypes.get(name);
        if(type == null){
            throw new RuntimeException("Controller type not registered: " + name);
        }
        return type;
    }

    static {
        registerControllerType(
            "joystick",
            ControllerType
                .createSimpleControllerType("joystick")
                // Buttons
                .addButtonShorthand("trigger", 0)
                .addButtonShorthand("top", 1)
                // Axes
                .addAxisShorthand("x", 0)
                .addAxisShorthand("y", 1)
                .addAxisShorthand("z", 2)
                .addAxisShorthand("twist", 3)
                .addAxisShorthand("throttle", 4)
        );
        registerControllerType(
            "xbox",
            ControllerType
                .createSimpleControllerType("xbox")
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
                .addAxisShorthand("rightTrigger", 3)
        );
        registerControllerType(
            "ps4",
            ControllerType
                .createSimpleControllerType("ps4")
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
                .addAxisShorthand("R2", 5)
        );
        registerControllerType(
            "ps5",
            ControllerType
                .createSimpleControllerType("ps5")
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
                .addAxisShorthand("R2", 5)
        );
        registerControllerType(
            "stadia",
            ControllerType
                .createSimpleControllerType("stadia")
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
                .addAxisShorthand("rightY", 4)
        );
    }

    // Will add more fields later
    public List<Controller> controllers = List.of();
    
    public Controllers(){}

    public Controllers(List<Controller> controllers){
        this.controllers = controllers;
    }

    public Controller getControllerByIndex(int index){
        return controllers.get(index);
    }

    public Controller getControllerByPort(int port){
        for(Controller controller : controllers){
            if(controller.port == port){
                return controller;
            }
        }
        throw new RuntimeException("No controller found on port: " + port);
    }

    private Controller.ControllerInterface getFromAllControllers(BiFunction<Controller, String, Boolean> func, BiFunction<Controller, String, Controller.ControllerInterface> getter, String command) {
        for (Controller controller : controllers) {
            if (func.apply(controller, command)) {
                return getter.apply(controller, command);
            }
        }
        throw new RuntimeException("No controller interface found for command: " + command);
    }

    public Controller.ControllerInterface getInterface(String command) {
        return getFromAllControllers(
            (controller, cmd) -> controller.hasControllerInterface(cmd),
            (controller, cmd) -> controller.getControllerInterface(cmd),
            command
        );
    }

    public Controller.Button getButton(String command) {
        return (Controller.Button) getFromAllControllers(
            (controller, cmd) -> {
                if (!controller.hasControllerInterface(cmd)) {
                    return false;
                }
                Controller.ControllerInterface ci = controller.getControllerInterface(cmd);
                return ci instanceof Controller.Button;
            },
            (controller, cmd) -> controller.getControllerInterface(cmd),
            command
        );
    }

    public Controller.Axis getAxis(String command) {
        return (Controller.Axis) getFromAllControllers(
            (controller, cmd) -> {
                if (!controller.hasControllerInterface(cmd)) {
                    return false;
                }
                Controller.ControllerInterface ci = controller.getControllerInterface(cmd);
                return ci instanceof Controller.Axis;
            },
            (controller, cmd) -> controller.getControllerInterface(cmd),
            command
        );
    }

    public Controller.POV getPOV(String command) {
        return (Controller.POV) getFromAllControllers(
            (controller, cmd) -> {
                if (!controller.hasControllerInterface(cmd)) {
                    return false;
                }
                Controller.ControllerInterface ci = controller.getControllerInterface(cmd);
                return ci instanceof Controller.POV;
            },
            (controller, cmd) -> controller.getControllerInterface(cmd),
            command
        );
    }

}
