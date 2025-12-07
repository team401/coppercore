package coppercore.wpilib_interface.subsystems.motors.talonfx;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.sim.TalonFXSimState;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;

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
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals.
     *
     * <p>This constructor is for a lead motor. Use {@link
     * MotorIOTalonFXPositionSim#MotorIOTalonFXPositionSim(MechanismConfig, int,
     * TalonFXConfiguration, PositionSimAdapter)} to create a follower.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     */
    public MotorIOTalonFXPositionSim(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            PositionSimAdapter physicsSimAdapter) {
        super(config, talonFXConfig);

        this.isFollower = false;

        this.invertSimRotation = false;

        this.talonSimState = this.talon.getSimState();

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.start();
        this.lastTimestamp = deltaTimer.get();
    }

    /**
     * Create a new simulated TalonFX IO for a lead motor, initializing a real TalonFX IO and all
     * required StatusSignals and then extracting its sim state.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXPositionSim created with the specified parameters, configured as
     *     a lead motor.
     */
    public static MotorIOTalonFXPositionSim newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            PositionSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXPositionSim(config, talonFXConfig, physicsSimAdapter);
    }

    /**
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals.
     *
     * <p>This constructor is for a follower. Use {@link
     * MotorIOTalonFXPositionSim#MotorIOTalonFXPositionSim(MechanismConfig, TalonFXConfiguration,
     * PositionSimAdapter)} to create a lead motor.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerMotorConfigs this motor is). This IO will automatically follow the lead
     *     motor at the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     */
    public MotorIOTalonFXPositionSim(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            PositionSimAdapter physicsSimAdapter) {
        super(config, followerIndex, talonFXConfig);

        this.isFollower = true;
        this.invertSimRotation = config.followerMotorConfigs[followerIndex].invert();

        this.talonSimState = this.talon.getSimState();

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.start();
        this.lastTimestamp = deltaTimer.get();
    }

    /**
     * Create a new simulated TalonFX IO for a follower motor, initializing a real TalonFX IO and
     * all required StatusSignals and then extracting its sim state.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerMotorConfigs this motor is). This IO will automatically follow the lead
     *     motor at the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXPositionSim created with the specified parameters, configured as
     *     a lead motor.
     */
    public static MotorIOTalonFXPositionSim newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            PositionSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXPositionSim(
                config, followerIndex, talonFXConfig, physicsSimAdapter);
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
    protected void updateSimState() {
        talonSimState.setSupplyVoltage(RobotController.getBatteryVoltage());

        if (!isFollower) {
            double timestamp = deltaTimer.get();

            double deltaTimeSeconds = timestamp - this.lastTimestamp;
            this.lastTimestamp = timestamp;

            physicsSimAdapter.update(talonSimState.getMotorVoltageMeasure(), deltaTimeSeconds);
        }

        boolean baseDirectionBackwards =
                talonFXConfig.MotorOutput.Inverted != InvertedValue.CounterClockwise_Positive;
        double invertMultiplier = baseDirectionBackwards ? -1.0 : 1.0;
        if (invertSimRotation) {
            invertMultiplier *= -1.0;
        }

        talonSimState.setRawRotorPosition(
                physicsSimAdapter.getMotorPosition().times(invertMultiplier));
        talonSimState.setRotorVelocity(
                physicsSimAdapter.getMotorAngularVelocity().times(invertMultiplier));
    }
}
