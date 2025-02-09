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
    private Tunable mechanism;

    Angle startPosition;

    double kG;
    double kS;

    private final double rampUpSpeed;

    /**
     * Create a command to automatically characterize kG for a Tunable system
     *
     * @param mechanism The Tunable mechanism/subsystem to tune
     * @param rampUpSpeed How much to increase the applied output by per loop. 0.001 is very precise
     *     but very slow.
     */
    public TuneG(Tunable mechanism, double rampUpSpeed) {
        this.mechanism = mechanism;
        this.kS = SmartDashboard.getNumber("Test-Mode/kS", 0);

        this.rampUpSpeed = rampUpSpeed;

        // this.withTimeout(5); TODO: Maybe add?
    }

    @Override
    public void initialize() {
        startPosition = mechanism.getPosition();
        kG = kS;
    }

    @Override
    public void execute() {
        mechanism.setOutput(kG);
        kG += rampUpSpeed;
    }

    @Override
    public void end(boolean interrupted) {
        mechanism.setOutput(0.0);
        SmartDashboard.putNumber("Test-Mode/kG", kG - kS);
        System.out.println("=====");
        System.out.println("  TuneG: kG = " + (kG - kS));
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        // This used to check if position was greater than abs(startPosition) - 0.1, if things break
        // maybe try this again
        return mechanism.getPosition().gt(startPosition);
    }
}
