package coppercore.wpilib_interface.subsystems.motors.talonfx;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.sim.TalonFXSimState;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter;
import edu.wpi.first.wpilibj.Timer;
import java.util.Optional;

/**
 * A MotorIOTalonFXPositionSim uses Phoenix-6 simulation features (TalonFX sim state) to read and
 * update an ElevatorSim or SingleJointedArmSim periodically and propagate its values into the
 * SimState of the motor.
 *
 * <p>The same PositionSimAdapter should (and can safely) be passed to all
 * MotorIOTalonFXPositionSims and CANCoderIOSims for each mechanism because only the lead motor
 * MotorIOTalonFXPositionSim will actually update the input the sim while the others will only read
 * values.
 *
 * <p>The distinction betewen MotorIOTalonFXPositionSim and MotorIOTalonFXFlywheelSim is that
 * FlywheelSim does not provide an easy way to access position measurements.
 *
 * <p>This class extends MotorIOTaloNFX to ensure that behavior is as close to identical in real
 * life and simulation as possible.
 */
public class MotorIOTalonFXPositionSim extends MotorIOTalonFX {
    private final PositionSimAdapter physicsSimAdapter;

    private final TalonFXSimState talonSimState;

    private final Timer deltaTimer = new Timer();
    private double lastTimestamp;

    private final boolean isFollower;

    /**
     * Track whether or not to invert the sim rotation based on an being inverted follower in the
     * config.
     */
    private final boolean invertSimRotation;

    /**
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An Optional containing either the index of the follower motor (what
     *     position in config.followerIds this motor is) or None if this is the lead motor. If
     *     followerIndex is not None, this IO will automatically follow the lead motor at the end of
     *     its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or SingleJointedArmSimAdapter to use for
     *     mechanism physics simulation.
     */
    public MotorIOTalonFXPositionSim(
            MechanismConfig config,
            Optional<Integer> followerIndex,
            TalonFXConfiguration talonFXConfig,
            PositionSimAdapter physicsSimAdapter) {
        super(config, followerIndex, talonFXConfig);

        this.isFollower = followerIndex.isPresent();
        this.invertSimRotation =
                followerIndex.map(idx -> config.followerMotorConfigs[idx].invert()).orElse(false);

        this.talonSimState = this.talon.getSimState();

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.start();
        this.lastTimestamp = deltaTimer.get();
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        updateSimState();

        super.updateInputs(inputs);
    }

    /**
     * Update the state of the mechanismPhysicsSim and send these values to the motor's
     * TalonFXSimState
     *
     * <p>If this IO is not the leader, it will not update the sim to avoid conflicts/updating
     * multiple times per periodic.
     */
    private void updateSimState() {
        if (!isFollower) {
            double timestamp = deltaTimer.get();

            double deltaTimeSeconds = timestamp - this.lastTimestamp;
            this.lastTimestamp = timestamp;

            physicsSimAdapter.update(talonSimState.getMotorVoltageMeasure(), deltaTimeSeconds);
        }

        double invertMultiplier = invertSimRotation ? -1.0 : 1.0;

        talonSimState.setRawRotorPosition(
                physicsSimAdapter.getMotorPosition().times(invertMultiplier));
        talonSimState.setRotorVelocity(
                physicsSimAdapter.getMotorAngularVelocity().times(invertMultiplier));
    }
}
