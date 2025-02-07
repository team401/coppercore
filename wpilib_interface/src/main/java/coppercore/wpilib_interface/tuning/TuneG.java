// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * A command to automatically characterize kG
 *
 * <p>Relies on SmartDashboard Test-Mode/kS to get a more accurate number.
 *
 * <p>Outputs its findings to SmartDashboard at Test-Mode/kS and to console
 */
public class TuneG extends Command {
    private Tunable subsystem;

    private int slot;

    Angle startPosition;

    double kG;
    double kS;

    /**
     * Create a command to automatically characterize kG for a Tunable system
     *
     * @param subsystem The Tunable subsystem to tune
     * @param slot The slot of the mechanism to tune kG on
     */
    public TuneG(Tunable subsystem, int slot) {
        this.subsystem = subsystem;
        this.kS = SmartDashboard.getNumber("Test-Mode/kS", 0);
        this.slot = slot;

        // this.withTimeout(5); TODO: Maybe add?
    }

    @Override
    public void initialize() {
        startPosition = subsystem.getPosition(slot);
        kG = kS;
    }

    @Override
    public void execute() {
        subsystem.setOutput(kG, slot);
        kG += 0.001;
    }

    @Override
    public void end(boolean interrupted) {
        subsystem.setOutput(0.0, slot);
        SmartDashboard.putNumber("Test-Mode/kG", kG - kS);
        System.out.println("=====");
        System.out.println("  TuneG: kG = " + (kG - kS));
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        // This used to check if position was greater than abs(startPosition) - 0.1, if things break
        // maybe try this again
        return subsystem.getPosition(slot).gt(startPosition);
    }
}
