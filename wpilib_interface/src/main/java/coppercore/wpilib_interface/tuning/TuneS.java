// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import coppercore.controls.Tunable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

public class TuneS extends Command {
    private Tunable subsystem;

    private int slot;

    double startPosition;

    double appliedOutput;

    public TuneS(Tunable subsystem, int slot) {
        this.subsystem = subsystem;
        this.slot = slot;

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
    }

    @Override
    public boolean isFinished() {
        return subsystem.getVelocity(slot) > 0.01;
    }
}
