package coppercore.wpilib_interface.subsystems.motors.talonfx;

import static edu.wpi.first.units.Units.Hertz;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.ctre.phoenix6.sim.TalonFXSimState.MotorType;
import coppercore.wpilib_interface.CTREUtil;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.sim.CoppercoreSimAdapter;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;

/**
 * A MotorIOTalonFXSim uses Phoenix-6 simulation features (TalonFX sim state) to read and update a
 * DCMotorSim, ElevatorSim, or SingleJointedArmSim periodically and propagate its values into the
 * SimState of the motor.
 *
 * <p>The same CoppercoreSimAdapter should (and can safely) be passed to all MotorIOTalonFXSims and
 * CANCoderIOSims for each mechanism because only the lead motor MotorIOTalonFXSim will actually
 * update the input the sim while the others will only read values.
 *
 * <p>This class extends MotorIOTalonFX to ensure that behavior is as close to identical in real
 * life and simulation as possible.
 */
public class MotorIOTalonFXSim extends MotorIOTalonFX {
    private final CoppercoreSimAdapter physicsSimAdapter;

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
     * Whether or not this IO is being used in a unit test. When this value is true, updateInputs
     * should wait for all status signals to refresh before continuing.
     */
    private boolean isUnitTestMode = false;

    /**
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals.
     *
     * <p>This constructor is for a lead motor. Use {@link
     * MotorIOTalonFXSim#MotorIOTalonFXSim(MechanismConfig, int, TalonFXConfiguration,
     * SignalRefreshRates, int, int, CoppercoreSimAdapter)} to create a follower.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     */
    public MotorIOTalonFXSim(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            CoppercoreSimAdapter physicsSimAdapter) {
        super(
                config,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals);

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
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                physicsSimAdapter);
    }

    /**
     * Create a new simulated TalonFX IO for a lead motor, initializing a real TalonFX IO and all
     * required StatusSignals and then extracting its sim state.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS,
                physicsSimAdapter);
    }

    /**
     * Create a new simulated TalonFX IO for a lead motor, initializing a real TalonFX IO and all
     * required StatusSignals, using {@link MotorIOTalonFX.SignalRefreshRates#defaults() default}
     * SignalRefreshRates, and then extracting its sim state.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                talonFXConfig,
                SignalRefreshRates.defaults(),
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS,
                physicsSimAdapter);
    }

    /**
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals.
     *
     * <p>This constructor is for a follower. Use {@link
     * MotorIOTalonFXSim#MotorIOTalonFXSim(MechanismConfig, TalonFXConfiguration,
     * SignalRefreshRates, int, int, CoppercoreSimAdapter)} to create a lead motor.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerMotorConfigs this motor is). This IO will automatically follow the lead
     *     motor at the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     */
    public MotorIOTalonFXSim(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            CoppercoreSimAdapter physicsSimAdapter) {
        super(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS);

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
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS,
                physicsSimAdapter);
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
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                physicsSimAdapter);
    }

    /**
     * Create a new simulated TalonFX IO for a follower motor, initializing a real TalonFX IO and
     * all required StatusSignals, using {@link MotorIOTalonFX.SignalRefreshRates#defaults()
     * default} SignalRefreshRates and then extracting its sim state.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerMotorConfigs this motor is). This IO will automatically follow the lead
     *     motor at the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation.
     * @return A new MotorIOTalonFXSim created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFXSim newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            CoppercoreSimAdapter physicsSimAdapter) {
        return new MotorIOTalonFXSim(
                config,
                followerIndex,
                talonFXConfig,
                SignalRefreshRates.defaults(),
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS,
                physicsSimAdapter);
    }

    /**
     * Updates this IO's TalonFX sim state's motor type (either KrakenX60 or KrakenX44), returning
     * this IO for easy chaining.
     *
     * <p>By default, KrakenX60 is assumed, and so this method only needs to be called for KrakenX44
     * simulations.
     *
     * @param motorType The MotorType, KrakenX60 or KrakenX44, to send to the TalonFXSimState.
     * @return This MotorIOTalonFXSim, to allow for easy method chaining or a builder-style
     *     declaration.
     */
    public MotorIOTalonFXSim withMotorType(MotorType motorType) {
        CTREUtil.tryUntilOk(() -> talonSimState.setMotorType(motorType), id, (code) -> {});
        return this;
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        if (isUnitTestMode) {
            BaseStatusSignal.waitForAll(1.0, signals);
        }

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

    /**
     * Sets the refresh rate of all status signals to 1000 hz and enables waiting for status signals
     * to refresh in updateInputs. This should hopefully improve consistency in unit tests.
     */
    public void enableUnitTestMode() {
        this.isUnitTestMode = true;
        BaseStatusSignal.setUpdateFrequencyForAll(Hertz.of(1000), signals);
    }
}
