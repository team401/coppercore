// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import static edu.wpi.first.units.Units.RotationsPerSecond;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * A command to automatically characterize kV for a Tunable system.
 *
 * <p>Relies on SmartDashboard Test-Mode/kS to get a more accurate number.
 *
 * <p>Outputs its findings to SmartDashboard Test-Mode/kV and to console.
 */
public class TuneV extends Command {
    private Tunable subsystem;

    private double output;

    private int slot;

    private ArrayList<AngularVelocity> velocities;

    double startPosition; // TODO

    double kS;
    double pastkV;
    MutAngularVelocity average = RotationsPerSecond.mutable(0.0);
    AngularVelocity vel = RotationsPerSecond.of(0.0);

    Angle maxPos;

    /**
     * Automatically tune kV for a system
     *
     * @param subsystem The Tunable subsystem to tune
     * @param output What output to apply (volts for voltage control, amps for an FOC system)
     * @param maxPos What position to drive to before stopping the command
     * @param slot The slot of the mechanism to tune
     */
    public TuneV(Tunable subsystem, double output, Angle maxPos, int slot) {
        this.subsystem = subsystem;
        this.output = output;
        this.slot = slot;
        this.kS = SmartDashboard.getNumber("Test-Mode/kS", 0);
        this.pastkV = SmartDashboard.getNumber("Test-Mode/kV", 0);

        this.maxPos = maxPos;

        this.withTimeout(5);
    }

    @Override
    public void initialize() {
        SmartDashboard.putBoolean("Test-Mode/Ended", false);
        subsystem.setOutput(output, slot);
        velocities = new ArrayList<AngularVelocity>();
    }

    @Override
    public void execute() {
        vel = subsystem.getVelocity(slot);
        SmartDashboard.putNumber("Test-Mode/VelocityRotPerSec", vel.in(RotationsPerSecond));
        // if (Math.abs(subsystem.getPosition(slot)) < 0.6 * conversionFactor) {
        velocities.add(vel);
        // }
    }

    @Override
    public void end(boolean interrupted) {
        SmartDashboard.putBoolean("Test-Mode/Ended", true);
        subsystem.setOutput(0.0, slot);

        for (AngularVelocity v : velocities) {
            average.mut_plus(v);
        }

        average.mut_divide(velocities.size());

        double kV = (output - kS) / average.in(RotationsPerSecond);
        SmartDashboard.putNumber(
                "Test-Mode/kV", ((output - kS) / average.in(RotationsPerSecond)) + pastkV);
        System.out.println("=====");
        System.out.println("  TuneV: kV = " + kV);
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        return subsystem.getPosition(slot).gt(maxPos);
    }
}
