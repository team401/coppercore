package coppercore.wpilib_interface.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.helpers.JSONConverter;
import coppercore.wpilib_interface.controllers.Controller;
import coppercore.wpilib_interface.controllers.ControllerJsonRepresentation;
import coppercore.wpilib_interface.controllers.Controllers;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ControllersTests {

    public static final String BLANK_CONTROLLERS_CONFIG_FILE = "blank_controllers_config.json";
    public static final String EMPTY_LIST_CONTROLLERS_CONFIG_FILE =
            "empty_list_controllers_config.json";
    public static final String SINGLE_CONTROLLER_CONFIG_FILE = "single_controller_config.json";
    public static final String MULTIPLE_CONTROLLERS_CONFIG_FILE =
            "multiple_controllers_config.json";
    public static final String MAPPED_CONTROLLER_ELEMENTS_CONFIG_FILE =
            "mapped_controller_elements_config.json";

    public static final double DELTA = 0.01;

    static {
        JSONConverter.addConversion(Controller.class, ControllerJsonRepresentation.class);
    }

    public JSONHandler jsonHandler =
            new JSONHandler(
                    Controllers.getControllerJsonSyncConfigBuilder()
                        .build(),
                    new UnitTestingPathProvider());

    public void resetDriverStationSim() {
        DriverStationSim.resetData();
        DriverStationSim.notifyNewData();
    }

    public void setupControllerSim(int port, int axisCount, int buttonCount) {
        DriverStationSim.setJoystickAxisCount(port, axisCount);
        DriverStationSim.setJoystickButtonCount(port, buttonCount);
        DriverStationSim.notifyNewData();
    }

    public void setAxis(int port, int axisIndex, double value) {
        DriverStationSim.setJoystickAxis(port, axisIndex, value);
        DriverStationSim.notifyNewData();
    }

    public void setButton(int port, int buttonIndex, boolean pressed) {
        DriverStationSim.setJoystickButton(port, buttonIndex, pressed);
        DriverStationSim.notifyNewData();
    }

    public Controllers loadFromJson(String fileName) {
        System.out.println("Loading controllers from JSON file: " + fileName);
        Controllers controllers = jsonHandler.getObject(new Controllers(), fileName);
        return controllers;
    }

    @Test
    public void BlankControllersJsonLoadTest() {
        Controllers controllers = loadFromJson(BLANK_CONTROLLERS_CONFIG_FILE);
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> {
                    controllers.getControllerByIndex(0);
                });
    }

    @Test
    public void EmptyListControllersJsonLoadTest() {
        Controllers controllers = loadFromJson(EMPTY_LIST_CONTROLLERS_CONFIG_FILE);
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> {
                    controllers.getControllerByIndex(0);
                });
    }

    @Test
    public void SingleControllerJsonLoadTest() {
        Controllers controllers = loadFromJson(SINGLE_CONTROLLER_CONFIG_FILE);
        Controller controller = controllers.getControllerByIndex(0);
        assertNotNull(controller);
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> {
                    controllers.getControllerByIndex(1);
                });
        assertEquals(2, controller.getPort());
        Controller.Button shootButton = controller.getButton("shoot");
        assertNotNull(shootButton);
        Controller.Axis driveAxis = controller.getAxis("drive");
        assertNotNull(driveAxis);

        setupControllerSim(2, 1, 1);

        setAxis(2, 0, 0);
        Assertions.assertEquals(0.0, driveAxis.getValue(), DELTA);
        setAxis(2, 0, 0.5);
        Assertions.assertEquals(0.5, driveAxis.getValue(), DELTA);
        setButton(2, 1, false);
        Assertions.assertEquals(false, shootButton.isPressed());
        setButton(2, 1, true);
        Assertions.assertEquals(true, shootButton.isPressed());
        setButton(2, 1, false);
        Assertions.assertEquals(false, shootButton.isPressed());
    }

    @Test
    public void MultipleControllersJsonLoadTest() {
        Controllers controllers = loadFromJson(MULTIPLE_CONTROLLERS_CONFIG_FILE);
        Controller controller1 = controllers.getControllerByIndex(0);
        Controller controller2 = controllers.getControllerByIndex(1);
        assertNotNull(controller1);
        assertNotNull(controller2);
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> {
                    controllers.getControllerByIndex(2);
                });
        assertEquals(3, controller1.getPort());
        assertEquals(1, controller2.getPort());

        setupControllerSim(3, 1, 1);
        setupControllerSim(1, 1, 1);

        Controller.Button shootButton =
                controllers.getButton("shoot"); // Should get from first controller
        assertNotNull(shootButton);
        Controller.Axis driveAxis =
                controllers.getAxis("drive"); // Should get from second controller
        assertNotNull(driveAxis);
    }

    @Test
    public void MappedControllerElementsJsonLoadTest() {
        Controllers controllers = loadFromJson(MAPPED_CONTROLLER_ELEMENTS_CONFIG_FILE);
        Controller controller = controllers.getControllerByIndex(0);
        assertNotNull(controller);
        assertEquals(0, controller.getPort());
        Controller.Button shootButton = controller.getButton("shoot");
        assertNotNull(shootButton);

        setupControllerSim(0, 1, 1);

        setAxis(0, 0, 0);
        System.out.println(shootButton.isPressed());
        System.out.println("Axis Value: " + DriverStation.getStickAxis(0, 0));
        Assertions.assertEquals(false, shootButton.isPressed());
        setAxis(0, 0, 0.45);
        Assertions.assertEquals(false, shootButton.isPressed());
        setAxis(0, 0, 0.55);
        Assertions.assertEquals(true, shootButton.isPressed());
        setAxis(0, 0, 0.45);
        Assertions.assertEquals(false, shootButton.isPressed());
    }
}
