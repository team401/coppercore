import static edu.wpi.first.units.Units.*;

import coppercore.wpilib_interface.AsymmetricDebouncer;
import edu.wpi.first.util.WPIUtilJNI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsymmetricDebouncerTests {
    @Test
    public void instantRiseAndFall() {
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(0.0, 0.0);
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
    }

    @Test
    public void instantRise() {
        WPIUtilJNI.enableMockTime();
        WPIUtilJNI.setMockTime((long) Seconds.of(0.0).in(Microseconds));
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(0.0, 1.0);
        Assertions.assertFalse(debouncer.calculate(false)); // Start with false, hasn't risen yet
        Assertions.assertTrue(debouncer.calculate(true)); // Should rise instantly
        Assertions.assertTrue(debouncer.calculate(false)); // But should not fall instantly
        WPIUtilJNI.setMockTime((long) Seconds.of(0.5).in(Microseconds));
        Assertions.assertTrue(debouncer.calculate(false)); // Should not fall until >=1.0 seconds
        WPIUtilJNI.setMockTime((long) Seconds.of(1.0).in(Microseconds));
        Assertions.assertFalse(debouncer.calculate(false)); // Now it should have fallen
        WPIUtilJNI.setMockTime((long) Seconds.of(1.5).in(Microseconds));
        Assertions.assertFalse(debouncer.calculate(false));
        WPIUtilJNI.disableMockTime();
    }
}
