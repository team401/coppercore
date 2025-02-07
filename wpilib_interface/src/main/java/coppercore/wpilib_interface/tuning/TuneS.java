// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * A command to automatically tune kS for a tunable system
 *
 * <p>Outputs its findings to SmartDashboard Test-Mode/kS and to console.
 */
public class TuneS extends Command {
    private Tunable subsystem;

    private int slot;

    Angle startPosition;

    double appliedOutput;

    AngularVelocity threshold;

    /**
     * Create a command to automatically tune kS for a tunable system
     *
     * @param subsystem The subsystem to tune
     * @param motionThreshold An AngularVelocity, what velocity must be detected for the system to
     *     be considered to be 'moving'
     * @param slot The slot of the mechanism in the subsystem to tune
     */
    public TuneS(Tunable subsystem, AngularVelocity motionThreshold, int slot) {
        this.subsystem = subsystem;
        this.slot = slot;

        this.threshold = motionThreshold;

        // this.withTimeout(5); TODO: Maybe add?
    }

    @Override
    public void initialize() {
        startPosition = subsystem.getPosition(slot);
        appliedOutput = 0;
    }

    @Override
    public void execute() {
        subsystem.setOutput(appliedOutput, slot);
        appliedOutput += 0.001;
    }

    @Override
    public void end(boolean interrupted) {
        subsystem.setOutput(0.0, slot);
        SmartDashboard.putNumber("Test-Mode/kS", appliedOutput);
        System.out.println("=====");
        System.out.println("  TuneS: kS = " + appliedOutput);
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        return subsystem.getVelocity(slot).gt(threshold);
    }
}
