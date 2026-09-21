// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import static org.wpilib.units.Units.RotationsPerSecond;

import java.util.ArrayList;
import org.wpilib.command2.Command;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;

/**
 * A command to automatically characterize kV for a Tunable system.
 *
 * <p>Relies on the Test-Mode/kS tunable to get a more accurate number.
 *
 * <p>Logs its findings under Test-Mode/kV, updates the tunable, and prints to console.
 */
public class TuneV extends Command {
    private Tunable mechanism;

    private double output;

    private ArrayList<AngularVelocity> velocities;

    double startPosition; // TODO

    double kS;
    double pastkV;
    AngularVelocity average = RotationsPerSecond.of(0.0);
    AngularVelocity vel = RotationsPerSecond.of(0.0);

    Angle maxPos;

    /**
     * Automatically tune kV for a system
     *
     * @param mechanism The Tunable mechanism/subsystem to tune
     * @param output What output to apply (volts for voltage control, amps for an FOC system)
     * @param maxPos What position to drive to before stopping the command
     */
    public TuneV(Tunable mechanism, double output, Angle maxPos) {
        this.mechanism = mechanism;
        this.output = output;
        this.maxPos = maxPos;

        // this.withTimeout(5);
    }

    @Override
    public void initialize() {
        kS = TestModeTunables.KS.get();
        pastkV = TestModeTunables.KV.get();
        Telemetry.log("Test-Mode/Ended", false);
        mechanism.setOutput(output);
        velocities = new ArrayList<AngularVelocity>();
        average = RotationsPerSecond.of(0.0);
    }

    @Override
    public void execute() {
        vel = mechanism.getVelocity();
        Telemetry.log("Test-Mode/VelocityRotPerSec", vel.in(RotationsPerSecond));
        // if (Math.abs(subsystem.getPosition(slot)) < 0.6 * conversionFactor) {
        velocities.add(vel);
        // }
    }

    @Override
    public void end(boolean interrupted) {
        Telemetry.log("Test-Mode/Ended", true);
        mechanism.setOutput(0.0);

        for (AngularVelocity v : velocities) {
            average = average.plus(v);
        }

        average = average.div(velocities.size());

        double kV = (output - kS) / average.in(RotationsPerSecond);
        TestModeTunables.KV.set(kV + pastkV);
        Telemetry.log("Test-Mode/kV", kV + pastkV);
        System.out.println("=====");
        System.out.println("  TuneV: kV = " + kV);
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        return mechanism.getPosition().gt(maxPos);
    }
}
