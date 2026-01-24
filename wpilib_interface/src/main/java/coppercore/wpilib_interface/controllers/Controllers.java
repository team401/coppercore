package coppercore.wpilib_interface.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * This class manages a collection of controllers. It includes methods to get controllers and their
 * control elements by command.
 */
public class Controllers {

    // Will add more fields later
    private List<Controller> controllers = List.of();

    /** Creates an empty Controllers object */
    public Controllers() {
        this(new ArrayList<>());
    }

    /**
     * Creates a Controllers object with the given controllers
     *
     * @param controllers
     */
    public Controllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     * Get controller by index
     *
     * @param index The index of the controller
     * @return The controller at the given index
     */
    public Controller getControllerByIndex(int index) {
        return controllers.get(index);
    }

    /**
     * Get controller by port
     *
     * @param port The port of the controller
     * @return The controller at the given port
     */
    public Controller getControllerByPort(int port) {
        for (Controller controller : controllers) {
            if (controller.port == port) {
                return controller;
            }
        }
        throw new RuntimeException("No controller found on port: " + port);
    }

    /**
     * Find and return the first matching control element for a given command.
     *
     * @param pred Predicate to apply to (Controller, Command)
     * @param getter Function to get the element from the controller
     * @param command The command to get the interface for
     * @return The control element
     */
    private Controller.ControlElement findFirstMatchingControlElement(
            BiFunction<Controller, String, Boolean> matchPred,
            BiFunction<Controller, String, Controller.ControlElement> getter,
            String command) {
        for (Controller controller : controllers) {
            if (matchPred.apply(controller, command)) {
                return getter.apply(controller, command);
            }
        }
        throw new RuntimeException("No control element found for command: " + command);
    }

    /**
     * Get controller element by command.
     *
     * @param command The command to get the element for
     * @return The first matching controller element.
     */
    public Controller.ControlElement getInterface(String command) {
        return findFirstMatchingControlElement(
                (controller, cmd) -> controller.hasControlElement(cmd),
                (controller, cmd) -> controller.getControlElement(cmd),
                command);
    }

    /**
     * Get button by command.
     *
     * @param command The command to get the button for
     * @return The first matching button
     */
    public Controller.Button getButton(String command) {
        return (Controller.Button)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasButton(cmd),
                        (controller, cmd) -> controller.getButton(cmd),
                        command);
    }

    /**
     * Get axis by command
     *
     * @param command The command to get the axis for
     * @return The first matching axis.
     */
    public Controller.Axis getAxis(String command) {
        return (Controller.Axis)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasAxis(cmd),
                        (controller, cmd) -> controller.getAxis(cmd),
                        command);
    }

    /**
     * Get POV by command
     *
     * @param command The command to get the POV for
     * @return The first matching POV.
     */
    public Controller.POV getPOV(String command) {
        return (Controller.POV)
                findFirstMatchingControlElement(
                        (controller, cmd) -> controller.hasPOV(cmd),
                        (controller, cmd) -> controller.getPOV(cmd),
                        command);
    }
}
