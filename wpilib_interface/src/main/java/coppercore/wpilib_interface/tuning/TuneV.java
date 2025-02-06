// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import coppercore.controls.Tunable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

public class TuneV extends Command {
    private Tunable subsystem;

    private double output;

    private int slot;
    private double conversionFactor;

    private ArrayList<Double> velocities;

    double startPosition; // TODO

    double kS;
    double pastkV;
    double average = 0;
    double vel = 0;

    public TuneV(Tunable subsystem, double output, int slot) {
        this.subsystem = subsystem;
        this.output = output;
        this.slot = slot;
        this.kS = SmartDashboard.getNumber("Test-Mode/kS", 0);
        this.pastkV = SmartDashboard.getNumber("Test-Mode/kV", 0);

        this.conversionFactor = subsystem.getConversionFactor(slot);

        this.withTimeout(5);
    }

    @Override
    public void initialize() {
        SmartDashboard.putBoolean("Test-Mode/Ended", false);
        subsystem.setOutput(output, slot);
        velocities = new ArrayList<Double>();
    }

    @Override
    public void execute() {
        vel = subsystem.getVelocity(slot);
        SmartDashboard.putNumber("Test-Mode/Velocity", vel);
        if (Math.abs(subsystem.getPosition(slot)) < 0.6 * conversionFactor) {
            velocities.add(vel);
        }
    }

    @Override
    public void end(boolean interrupted) {
        SmartDashboard.putBoolean("Test-Mode/Ended", true);
        subsystem.setOutput(0.0, slot);

        for (double v : velocities) {
            average += v;
        }

        average /= velocities.size();

        SmartDashboard.putNumber("Test-Mode/kV", ((output - kS) / average) + pastkV);
    }

    @Override
    public boolean isFinished() {
        return Math.abs(subsystem.getPosition(slot)) > 0.9 * conversionFactor;
    }
}
