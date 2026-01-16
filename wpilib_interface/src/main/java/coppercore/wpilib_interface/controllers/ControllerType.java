package coppercore.wpilib_interface.controllers;

import java.util.HashMap;

// Maybe add more info to this later to allow defining more complex controller types
// And maybe define custom axis/button values here too guarantee compatibility
public class ControllerType {

    public String name;
    public HashMap<String, Integer> AxisShorthands;
    public HashMap<String, Integer> ButtonShorthands;
    public HashMap<String, Integer> POVShorthands;

    protected ControllerType(String name) {
        this.name = name;
        this.AxisShorthands = new HashMap<>();
        this.ButtonShorthands = new HashMap<>();
        this.POVShorthands = new HashMap<>();
    }

    public static ControllerType createSimpleControllerType(String name) {
        return new ControllerType(name);
    }

    public ControllerType addButtonShorthand(String shorthand, int id) {
        ButtonShorthands.put(shorthand, id);
        return this;
    }

    public ControllerType addAxisShorthand(String shorthand, int id) {
        AxisShorthands.put(shorthand, id);
        return this;
    }

    public ControllerType addPOVShorthand(String shorthand, int id) {
        POVShorthands.put(shorthand, id);
        return this;
    }
}
