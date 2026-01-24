package coppercore.wpilib_interface.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * This class manages a collection of controllers. It includes methods to get controllers and their
 * interfaces by command.
 */
public class Controllers {

    // Will add more fields later
    public List<Controller> controllers = List.of();

    /** Creates an empty Controllers object */
    public Controllers() {
        this.controllers = new ArrayList<>();
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
     * Get controller interface by command
     *
     * @param func Function to check if controller has the interface
     * @param getter Function to get the interface from the controller
     * @param command The command to get the interface for
     * @return The controller interface
     */
    private Controller.ControlElement getFromAllControllers(
            BiFunction<Controller, String, Boolean> func,
            BiFunction<Controller, String, Controller.ControlElement> getter,
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
     *
     * @param command The command to get the interface for
     * @return The controller interface
     */
    public Controller.ControlElement getInterface(String command) {
        return getFromAllControllers(
                (controller, cmd) -> controller.hasControllerInterface(cmd),
                (controller, cmd) -> controller.getControllerInterface(cmd),
                command);
    }

    /**
     * Get button by command
     *
     * @param command The command to get the button for
     * @return The button
     */
    public Controller.Button getButton(String command) {
        return (Controller.Button)
                getFromAllControllers(
                        (controller, cmd) -> controller.hasButton(cmd),
                        (controller, cmd) -> controller.getButton(cmd),
                        command);
    }

    /**
     * Get axis by command
     *
     * @param command The command to get the axis for
     * @return The axis
     */
    public Controller.Axis getAxis(String command) {
        return (Controller.Axis)
                getFromAllControllers(
                        (controller, cmd) -> controller.hasAxis(cmd),
                        (controller, cmd) -> controller.getAxis(cmd),
                        command);
    }

    /**
     * Get POV by command
     *
     * @param command The command to get the POV for
     * @return The POV
     */
    public Controller.POV getPOV(String command) {
        return (Controller.POV)
                getFromAllControllers(
                        (controller, cmd) -> controller.hasPOV(cmd),
                        (controller, cmd) -> controller.getPOV(cmd),
                        command);
    }
}
