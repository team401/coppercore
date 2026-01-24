package coppercore.wpilib_interface.controllers;

import java.util.HashMap;

// Maybe add more info to this later to allow defining more complex controller types
// And maybe define custom axis/button values here too guarantee compatibility
/** Class representing a type of controller */
public class ControllerType {

    private static HashMap<String, ControllerType> controllerTypes = new HashMap<>();

    // The name of the controller type
    public String name;
    // Shorthands for axes, buttons, and POVs
    public HashMap<String, Integer> AxisShorthands;
    // Shorthands for buttons
    public HashMap<String, Integer> ButtonShorthands;
    // Shorthands for POVs
    public HashMap<String, Integer> POVShorthands;

    /**
     * Protected constructor
     *
     * @param name The name of the controller type
     */
    protected ControllerType(String name) {
        this.name = name;
        this.AxisShorthands = new HashMap<>();
        this.ButtonShorthands = new HashMap<>();
        this.POVShorthands = new HashMap<>();
    }

    /**
     * Register a controller type with a simple constructor function
     *
     * @param name The name of the controller type
     */
    public static void registerControllerType(String name) {
        registerControllerType(name, ControllerType.createSimpleControllerType(name));
    }

    /**
     * Register a controller type
     *
     * @param name The name of the controller type
     * @param type The controller type
     */
    public static void registerControllerType(String name, ControllerType type) {
        controllerTypes.put(name, type);
    }

    /**
     * Get a registered controller type
     *
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

    /**
     * Create a simple controller type with no shorthands
     *
     * @param name The name of the controller type
     * @return The controller type
     */
    public static ControllerType createSimpleControllerType(String name) {
        return new ControllerType(name);
    }

    /**
     * Add a button shorthand
     *
     * @param shorthand The shorthand name
     * @param id The button id
     * @return The controller type
     */
    public ControllerType addButtonShorthand(String shorthand, int id) {
        ButtonShorthands.put(shorthand, id);
        return this;
    }

    /**
     * Add an axis shorthand
     *
     * @param shorthand The shorthand name
     * @param id The axis id
     * @return The controller type
     */
    public ControllerType addAxisShorthand(String shorthand, int id) {
        AxisShorthands.put(shorthand, id);
        return this;
    }

    /**
     * Add a POV shorthand
     *
     * @param shorthand The shorthand name
     * @param id The POV id
     * @return The controller type
     */
    public ControllerType addPOVShorthand(String shorthand, int id) {
        POVShorthands.put(shorthand, id);
        return this;
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
}
