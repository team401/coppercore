// TODO: WIP - Not tested

package coppercore.wpilib_interface.tuning;

import static edu.wpi.first.units.Units.Rotations;

import edu.wpi.first.math.filter.MedianFilter;
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

    private final MedianFilter positionFilter;

    private final double movementThreshold;

    private double filteredPosition;

    /**
     * Create a command to automatically characterize kG for a Tunable system
     * 
     * <p> WARNING: this command is very finicky and doesn't seem to function properly. We recommend using a TuneS to tune your kG, or tuning it manually
     *
     * @param mechanism The Tunable mechanism/subsystem to tune
     * @param rampUpSpeed How much to increase the applied output by per loop. 0.001 is very precise
     *     but very slow.
     * @param filterWindow Filter window for the position median filter
     * @param movementThreshold How much the system must move before it is considered "having
     *     moved", in rotations
     */
    public TuneG(
            Tunable mechanism, double rampUpSpeed, int filterWindow, double movementThreshold) {
        this.mechanism = mechanism;
        this.kS = SmartDashboard.getNumber("Test-Mode/kS", 0);

        this.rampUpSpeed = rampUpSpeed;

        positionFilter = new MedianFilter(filterWindow);

        this.movementThreshold = movementThreshold;

        // this.withTimeout(5); TODO: Maybe add?
    }

    @Override
    public void initialize() {
        startPosition = mechanism.getPosition();
        kG = kS;
    }

    @Override
    public void execute() {
        filteredPosition = positionFilter.calculate(mechanism.getPosition().in(Rotations));

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
        return filteredPosition > (startPosition.in(Rotations) + movementThreshold);
    }
}
