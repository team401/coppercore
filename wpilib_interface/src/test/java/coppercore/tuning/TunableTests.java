package coppercore.tuning;

import static edu.wpi.first.units.Units.RotationsPerSecond;

import coppercore.wpilib_interface.tuning.Tunable;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.measure.AngularVelocity;
import org.junit.Test;

public class TunableTests {
    @Test
    void testTunable() {
        Tunable<AngleUnit> tunable =
                new Tunable<AngleUnit>() {
                    public AngularVelocity getVelocity() {
                        return RotationsPerSecond.of(0);
                    }
                };
    }
}
