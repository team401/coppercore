package coppercore.wpi_interface.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import coppercore.wpi_interface.Controllers;
import java.util.List;
// import org.junit.Before;
import org.junit.jupiter.api.Test;

public class unittest {

    // @Before
    // public static void TestPrep() {
    //    Controllers.loadControllers();
    // }

    @Test
    public static void TestOne() {
        Controllers.loadControllers();
        List<Controllers.Controller> controllers = Controllers.getControllers();
        System.out.println(controllers.size());
        assertEquals(23124, controllers.size());
        assertFalse(true);
    }
}
