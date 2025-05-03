import coppercore.wpilib_interface.AsymmetricDebouncer;
import coppercore.wpilib_interface.UnitUtils;
import edu.wpi.first.math.MathSharedStore;
import edu.wpi.first.units.Units;

import java.math.MathContext;

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
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(0.0, 1.0);
    }

    @Test
    public void clampBelowBounds() {
        Assertions.assertTrue(
                UnitUtils.clampMeasure(
                                Units.Meters.of(0.0), Units.Meters.of(1.0), Units.Meters.of(3.0))
                        .equals(Units.Meters.of(1.0)));
    }

    @Test
    public void clampAboveBounds() {
        Assertions.assertTrue(
                UnitUtils.clampMeasure(
                                Units.Meters.of(4.0), Units.Meters.of(1.0), Units.Meters.of(3.0))
                        .equals(Units.Meters.of(3.0)));
    }
}
