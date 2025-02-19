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
    private Tunable mechanism;

    Angle startPosition;

    double appliedOutput;
    private final double rampUpSpeed;

    AngularVelocity threshold;

    /**
     * Create a command to automatically tune kS for a tunable system
     *
     * @param mechanism The mechanism or subsystem to tune
     * @param motionThreshold An AngularVelocity, what velocity must be detected for the system to
     *     be considered to be 'moving'
     * @param rampUpSpeed How much to increase the applied output by per loop. 0.001 is very precise
     *     but very slow.
     */
    public TuneS(Tunable mechanism, AngularVelocity motionThreshold, double rampUpSpeed) {
        this.mechanism = mechanism;

        this.threshold = motionThreshold;

        this.rampUpSpeed = rampUpSpeed;

        // this.withTimeout(5); TODO: Maybe add?
    }

    @Override
    public void initialize() {
        startPosition = mechanism.getPosition();
        appliedOutput = 0;
    }

    @Override
    public void execute() {
        mechanism.setOutput(appliedOutput);
        appliedOutput += rampUpSpeed;
    }

    @Override
    public void end(boolean interrupted) {
        mechanism.setOutput(0.0);
        SmartDashboard.putNumber("Test-Mode/kS", appliedOutput);
        System.out.println("=====");
        System.out.println("  TuneS: kS = " + appliedOutput);
        System.out.println("=====");
    }

    @Override
    public boolean isFinished() {
        return mechanism.getVelocity().gt(threshold);
    }
}
