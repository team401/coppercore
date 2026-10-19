package coppercore.wpilib_interface.tuning;

import org.wpilib.tunable.TunableDouble;
import org.wpilib.tunable.Tunables;

/** Values shared by the test-mode characterization commands and editable from a dashboard. */
final class TestModeTunables {
    static final TunableDouble KS = Tunables.addDouble("Test-Mode/kS", 0.0);
    static final TunableDouble KV = Tunables.addDouble("Test-Mode/kV", 0.0);

    private TestModeTunables() {}
}
