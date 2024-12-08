import coppercore.wpilib_interface.UnitUtils;
import edu.wpi.first.units.Units;
// import edu.wpi.first.units.Units.Meters;
import edu.wpi.first.units.Units.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnitUtilsTests {
    @Test
    public void clampWithinBounds() {
        Assertions.assertTrue(
                UnitUtils.clampMeasure(
                                Units.Meters.of(2.0), Units.Meters.of(1.0), Units.Meters.of(3.0))
                        .equals(Units.Meters.of(2.0)));
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
