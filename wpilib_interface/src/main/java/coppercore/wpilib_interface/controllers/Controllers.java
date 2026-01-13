package coppercore.wpilib_interface.controllers;

import java.util.HashMap;
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

}
