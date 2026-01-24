package coppercore.wpilib_interface.controllers;

import java.util.HashMap;

// Maybe add more info to this later to allow defining more complex controller types
// And maybe define custom axis/button values here too guarantee compatibility
/** Class representing a type of controller */
public class ControllerType {

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
}
