package coppercore.wpilib_interface.subsystems.motors.talonfx;

import static edu.wpi.first.units.Units.Celsius;
import static edu.wpi.first.units.Units.Hertz;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.CoastOut;
import com.ctre.phoenix6.controls.DynamicMotionMagicTorqueCurrentFOC;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicExpoTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicVelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.StaticBrake;
import com.ctre.phoenix6.controls.TorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import coppercore.wpilib_interface.CTREUtil;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.CanBusMotorControllerBase;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.motors.profile.MotionProfileConfig;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.Optional;

/**
 * A base motor IO that implements closed-loop control for a TalonFX-supporting motor using
 * MotionMagicExpo, MotionMagicVelocity, and TorqueCurrentFOC wherever possible.
 */
public class MotorIOTalonFX extends CanBusMotorControllerBase implements MotorIO {
    /**
     * The default refresh rate for medium priority status signals such as applied voltage or closed
     * loop output.
     *
     * <p>These "medium priority" signals are StatusSignals that are important to monitor at a
     * frequency higher than 4hz, but do not need minimum latency and so are kept at a medium
     * refresh rate, such as 20hz.
     */
    public static final Frequency defaultMediumPriorityUpdateFrequency = Hertz.of(20.0);

    /**
     * The default refresh rate for high priority status signals (velocity, position, stator and
     * supply current).
     *
     * <p>These "high priority" signals are StatusSignals that are vital to monitor at a high
     * frequency to lower latency. They're kept at a very high refresh rate, such as 200-250hz, so
     * that the newest possible data is available during each periodic cycle.
     */
    public static final Frequency defaultHighPriorityUpdateFrequency = Hertz.of(200.0);

    /**
     * The default refresh rate for motor output status signals (MotorVoltage and TorqueCurrent).
     * These signals must be kept at a decent refresh rate since Follower requests read these
     * signals to decide what output to apply to their motors.
     *
     * <p>DutyCycle isn't used here since we should never use it.
     */
    public static final Frequency defaultOutputUpdateFrequency = Hertz.of(75.0);

    /** Bitmask constant for the position signal. */
    public static final int SIGNAL_POSITION = 1 << 0;

    /** Bitmask constant for the velocity signal. */
    public static final int SIGNAL_VELOCITY = 1 << 1;

    /** Bitmask constant for the motor voltage voltage (also called applied voltage) signal. */
    public static final int SIGNAL_MOTOR_VOLTAGE = 1 << 2;

    /** Bitmask constant for the stator current signal. */
    public static final int SIGNAL_STATOR_CURRENT = 1 << 3;

    /** Bitmask constant for the supply current signal. */
    public static final int SIGNAL_SUPPLY_CURRENT = 1 << 4;

    /** Bitmask constant for the raw rotor position signal. */
    public static final int SIGNAL_RAW_ROTOR_POSITION = 1 << 5;

    /** Bitmask constant for the closed loop output signal. */
    public static final int SIGNAL_CLOSED_LOOP_OUTPUT = 1 << 6;

    /** Bitmask constant for the closed loop reference signal. */
    public static final int SIGNAL_CLOSED_LOOP_REFERENCE = 1 << 7;

    /** Bitmask constant for the closed loop reference slope signal. */
    public static final int SIGNAL_CLOSED_LOOP_REFERENCE_SLOPE = 1 << 8;

    /** Bitmask constant for the temperature signal. */
    public static final int SIGNAL_TEMPERATURE = 1 << 9;

    /** Bitmask constant for the torque current signal. */
    public static final int SIGNAL_TORQUE_CURRENT = 1 << 10;

    /** Bitmask combining all signal constants. */
    public static final int SIGNAL_ALL = (1 << 11) - 1;

    /**
     * Default medium priority signal mask. Includes all signals (signals also in the high priority
     * mask will be overridden to use the high priority update frequency).
     */
    public static final int DEFAULT_MEDIUM_PRIORITY_SIGNALS = SIGNAL_ALL;

    /**
     * Default high priority signal mask. Includes velocity, position, stator current, and supply
     * current.
     */
    public static final int DEFAULT_HIGH_PRIORITY_SIGNALS =
            SIGNAL_VELOCITY | SIGNAL_POSITION | SIGNAL_STATOR_CURRENT | SIGNAL_SUPPLY_CURRENT;

    /**
     * Default output signal mask. Includes only torque current, since followers using
     * TorqueCurrentFOC read this signal to decide what output to apply to their motors. Add {@link
     * #SIGNAL_MOTOR_VOLTAGE} if voltage-mode followers are used.
     */
    public static final int DEFAULT_OUTPUT_SIGNALS = SIGNAL_TORQUE_CURRENT;

    /**
     * The SignalRefreshRates record stores the desired signal refresh rates for a MotorIOTalonFX.
     *
     * @param highPriorityUpdateFrequency A Frequency specifying the update frequency for the
     *     position, velocity, stator current, and supply current signals.
     * @param mediumPriorityUpdateFrequency A Frequency specifying the update frequency for the
     *     applied voltage, raw rotor position, closed loop output, closed loop reference, closed
     *     loop reference slope, and temperature signals.
     * @param outputUpdateFrequency A Frequency specifying the update frequency for signals in the
     *     output signal bitmask (by default, only TorqueCurrent), which are used by follower
     *     requests. It isn't recommended to lower these below 20hz. Note that if any of these
     *     signals is present in the high priority status signal bitmask, its refresh rate will be
     *     set to the high priority refresh rate instead.
     */
    public record SignalRefreshRates(
            Frequency highPriorityUpdateFrequency,
            Frequency mediumPriorityUpdateFrequency,
            Frequency outputUpdateFrequency) {
        /**
         * Creates a new SignalRefreshRates object with the default values of 200hz for high
         * priority and 20hz for medium priority signals.
         *
         * @return A new SignalRefreshRates object with the default values of 200hz for high
         *     priority and 20hz for medium priority signals.
         */
        public static SignalRefreshRates defaults() {
            return new SignalRefreshRates(
                    defaultHighPriorityUpdateFrequency,
                    defaultMediumPriorityUpdateFrequency,
                    defaultOutputUpdateFrequency);
        }
    }

    /**
     * TalonFXConfiguration used by the motor. This may be shared between multiple IOs and therefore
     * should be mutated only with extreme caution.
     */
    protected final TalonFXConfiguration talonFXConfig;

    /**
     * TalonFX motor controller interface object to interact with a Kraken x60 or x44's integrated
     * TalonFX controller
     */
    protected final TalonFX talon;

    /** Position StatusSignal cached for easy repeated access */
    protected final StatusSignal<Angle> positionSignal;

    /** Velocity StatusSignal cached for easy repeated access */
    protected final StatusSignal<AngularVelocity> velocitySignal;

    /** Motor Voltage StatusSignal cached for easy repeated access */
    protected final StatusSignal<Voltage> motorVoltageSignal;

    /** Torque Current StatusSignal cached for easy repeated access */
    protected final StatusSignal<Current> torqueCurrentSignal;

    /** Stator Current StatusSignal cached for easy repeated access */
    protected final StatusSignal<Current> statorCurrentSignal;

    /** Supply Current StatusSignal cached for easy repeated access */
    protected final StatusSignal<Current> supplyCurrentSignal;

    /** Raw Rotor Position StatusSignal cached for easy repeated access */
    protected final StatusSignal<Angle> rawRotorPositionSignal;

    /** CLosed Loop Output StatusSignal cached for easy repeated access */
    protected final StatusSignal<Double> closedLoopOutputSignal;

    /** Closed Loop Reference StatusSignal cached for easy repeated access */
    protected final StatusSignal<Double> closedLoopReferenceSignal;

    /** Closed Loop Reference Slope StatusSignal cached for easy repeated access */
    protected final StatusSignal<Double> closedLoopReferenceSlopeSignal;

    /** Temperature StatusSignal cached for easy repeated access */
    protected final StatusSignal<Temperature> temperatureSignal;

    /** Array of status signals to be easily passed to refreshAll */
    protected final BaseStatusSignal[] signals;

    /**
     * The Update Frequency to request from each motor output request. Can be configured using
     * setRequestUpdateFrequency.
     *
     * <ul>
     *   <li><b>Default value:</b> Empty (uses default update frequency for each request type)
     * </ul>
     */
    protected Optional<Frequency> requestUpdateFrequencyHz = Optional.empty();

    /** A neutral request to use for basic config-based neutral mode commands */
    protected final NeutralOut neutralRequest = new NeutralOut();

    /**
     * A coast request to use when coasting is required, regardless of configured NeutralMode value
     */
    protected final CoastOut coastRequest = new CoastOut();

    /**
     * A brake request to use when braking is required, regardless of configured NeutralMode value
     */
    protected final StaticBrake brakeRequest = new StaticBrake();

    /** An unprofiled position FOC request for non-profiled position closed-loop control */
    protected final PositionTorqueCurrentFOC unprofiledPositionRequest =
            new PositionTorqueCurrentFOC(Rotations.zero());

    /**
     * A Motion-Magic profiled position FOC request for non-expo profiled position closed-loop
     * control
     */
    protected final MotionMagicTorqueCurrentFOC profiledPositionRequest =
            new MotionMagicTorqueCurrentFOC(Rotations.zero());

    /**
     * A Dynamic Motion-Magic position FOC request for dynamically profiled position closed-loop
     * control
     */
    protected final DynamicMotionMagicTorqueCurrentFOC dynamicProfiledPositionRequest;

    /** A Motion-Magic-Expo profiled FOC request for expo profiled position closed-loop control */
    protected final MotionMagicExpoTorqueCurrentFOC expoProfiledPositionRequest =
            new MotionMagicExpoTorqueCurrentFOC(Rotations.zero());

    /** An unprofiled velocity FOC request for unprofiled velocity closed-loop control */
    protected final VelocityTorqueCurrentFOC unprofiledVelocityRequest =
            new VelocityTorqueCurrentFOC(RotationsPerSecond.zero());

    /** A Motion-Magic profiled velocity FOC request for profiled velocity closed-loop control */
    protected final MotionMagicVelocityTorqueCurrentFOC profiledVelocityRequest =
            new MotionMagicVelocityTorqueCurrentFOC(RotationsPerSecond.zero());

    /** A voltage request to use for all open-loop voltage control */
    protected final VoltageOut voltageRequest = new VoltageOut(0.0);

    /** A torque-current FOC request to use for all open-loop current control */
    protected final TorqueCurrentFOC currentRequest = new TorqueCurrentFOC(0.0);

    /**
     * Create a new MotorIOTalonFX given a mechanism config, a CANDeviceID, a TalonFXConfiguration,
     * signal refresh rates, and bitmasks specifying which signals should receive medium vs. high
     * priority update frequencies.
     *
     * <p>This constructor initializes all required fields but doesn't handle leader vs. follower
     * behavior, so it is protected and is intended only to be called from a constructor that will
     * extract the lead motor or follower motor ID from the config.
     *
     * <p>Signals included in {@code mediumPrioritySignals} will use the medium priority update
     * frequency, and signals included in {@code highPrioritySignals} will use the high priority
     * update frequency. Signals not included in either mask will have their update frequency
     * drastically reduced to 4hz by {@code optimizeBusUtilization}.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param id The CANDeviceID of the motor in question.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param outputSignals A bitmask of SIGNAL_* constants specifying which signals should receive
     *     the output update frequency. These signals are read by follower requests to decide what
     *     output to apply to their motors. Defaults to {@link #DEFAULT_OUTPUT_SIGNALS}.
     */
    protected MotorIOTalonFX(
            MechanismConfig config,
            CANDeviceID id,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            int outputSignals) {
        super(config, id, "_TalonFX_");

        this.talonFXConfig = talonFXConfig;

        this.talon = new TalonFX(id.id(), id.canbus());

        CTREUtil.tryUntilOk(
                () -> talon.getConfigurator().apply(talonFXConfig),
                id,
                (code) -> {
                    configFailedToApplyAlert.set(true);
                });

        this.positionSignal = talon.getPosition();
        this.velocitySignal = talon.getVelocity();
        this.motorVoltageSignal = talon.getMotorVoltage();
        this.torqueCurrentSignal = talon.getTorqueCurrent();
        this.statorCurrentSignal = talon.getStatorCurrent();
        this.supplyCurrentSignal = talon.getSupplyCurrent();
        this.rawRotorPositionSignal = talon.getRotorPosition();
        this.closedLoopOutputSignal = talon.getClosedLoopOutput();
        this.closedLoopReferenceSignal = talon.getClosedLoopReference();
        this.closedLoopReferenceSlopeSignal = talon.getClosedLoopReferenceSlope();
        this.temperatureSignal = talon.getAncillaryDeviceTemp();

        this.signals =
                new BaseStatusSignal[] {
                    velocitySignal,
                    positionSignal,
                    motorVoltageSignal,
                    statorCurrentSignal,
                    supplyCurrentSignal,
                    rawRotorPositionSignal,
                    closedLoopOutputSignal,
                    closedLoopReferenceSignal,
                    closedLoopReferenceSlopeSignal,
                    temperatureSignal
                };

        BaseStatusSignal[] mediumSignals = signalsForMask(mediumPrioritySignals);
        BaseStatusSignal[] highSignals = signalsForMask(highPrioritySignals);
        BaseStatusSignal[] outputSigs = signalsForMask(outputSignals);

        // Signals that won't be used for low-latency code functions, such as scoring/shooting,
        // state machine transitions, fast-paced decisionmaking.
        if (mediumSignals.length > 0) {
            CTREUtil.tryUntilOk(
                    () ->
                            BaseStatusSignal.setUpdateFrequencyForAll(
                                    signalRefreshRates.mediumPriorityUpdateFrequency,
                                    mediumSignals),
                    id,
                    (code) -> {});
        }

        // Signals that are used for follower requests
        if (outputSigs.length > 0) {
            CTREUtil.tryUntilOk(
                    () ->
                            BaseStatusSignal.setUpdateFrequencyForAll(
                                    signalRefreshRates.outputUpdateFrequency, outputSigs),
                    id,
                    (code) -> {});
        }

        // Signals that need to have a latency as low as possible
        if (highSignals.length > 0) {
            CTREUtil.tryUntilOk(
                    () ->
                            BaseStatusSignal.setUpdateFrequencyForAll(
                                    signalRefreshRates.highPriorityUpdateFrequency, highSignals),
                    id,
                    (code) -> {});
        }

        CTREUtil.tryUntilOk(() -> talon.optimizeBusUtilization(), id, (code) -> {});

        this.dynamicProfiledPositionRequest =
                new DynamicMotionMagicTorqueCurrentFOC(
                                0,
                                talonFXConfig.MotionMagic.MotionMagicCruiseVelocity,
                                talonFXConfig.MotionMagic.MotionMagicAcceleration)
                        .withJerk(talonFXConfig.MotionMagic.MotionMagicJerk);
    }

    /**
     * Create a new MotorIOTalonFX given a mechanism config, a CANDeviceID, and a
     * TalonFXConfiguration, using the default signal priority masks.
     *
     * <p>This constructor uses {@link #DEFAULT_MEDIUM_PRIORITY_SIGNALS} and {@link
     * #DEFAULT_HIGH_PRIORITY_SIGNALS} for the signal priority bitmasks.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param id The CANDeviceID of the motor in question.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     */
    protected MotorIOTalonFX(
            MechanismConfig config,
            CANDeviceID id,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates) {
        this(
                config,
                id,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS,
                DEFAULT_OUTPUT_SIGNALS);
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for the "lead motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, int, TalonFXConfiguration, SignalRefreshRates,
     * int, int)} to create a follower.
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
     */
    public MotorIOTalonFX(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals) {
        this(
                config,
                config.leadMotorId,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                DEFAULT_OUTPUT_SIGNALS);
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for the "lead motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, int, TalonFXConfiguration, SignalRefreshRates,
     * int, int, int)} to create a follower.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high-, medium-, and output-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param outputSignals A bitmask of SIGNAL_* constants specifying which signals should receive
     *     the output update frequency (used by follower requests).
     */
    public MotorIOTalonFX(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            int outputSignals) {
        this(
                config,
                config.leadMotorId,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                outputSignals);
    }

    /**
     * Create a new TalonFX IO for a lead motor, initializing a TalonFX and all required
     * StatusSignals
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
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFX newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals) {
        return new MotorIOTalonFX(
                config,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals);
    }

    /**
     * Create a new TalonFX IO for a lead motor, initializing a TalonFX and all required
     * StatusSignals
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high-, medium-, and output-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param outputSignals A bitmask of SIGNAL_* constants specifying which signals should receive
     *     the output update frequency (used by follower requests).
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFX newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            int outputSignals) {
        return new MotorIOTalonFX(
                config,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                outputSignals);
    }

    /**
     * Create a new TalonFX IO for a lead motor, initializing a TalonFX and all required
     * StatusSignals, using the default set of medium/high priority signals.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFX newLeader(
            MechanismConfig config,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates) {
        return new MotorIOTalonFX(
                config,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS);
    }

    /**
     * Create a new TalonFX IO for a lead motor, initializing a TalonFX and all required
     * StatusSignals, using the {@link SignalRefreshRates#defaults() default} SignalRefreshRates.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFX newLeader(
            MechanismConfig config, TalonFXConfiguration talonFXConfig) {
        return new MotorIOTalonFX(
                config,
                talonFXConfig,
                SignalRefreshRates.defaults(),
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS);
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for a "follower motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, CANDeviceID, TalonFXConfiguration,
     * SignalRefreshRates, int, int)} to create the leader.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     */
    public MotorIOTalonFX(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals) {
        this(
                config,
                config.followerMotorConfigs[followerIndex].id(),
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                DEFAULT_OUTPUT_SIGNALS);

        follow(config.leadMotorId.id(), config.followerMotorConfigs[followerIndex].invert());
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for a "follower motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, CANDeviceID, TalonFXConfiguration,
     * SignalRefreshRates, int, int, int)} to create the leader.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high-, medium-, and output-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param outputSignals A bitmask of SIGNAL_* constants specifying which signals should receive
     *     the output update frequency (used by follower requests).
     */
    public MotorIOTalonFX(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            int outputSignals) {
        this(
                config,
                config.followerMotorConfigs[followerIndex].id(),
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                outputSignals);

        follow(config.leadMotorId.id(), config.followerMotorConfigs[followerIndex].invert());
    }

    /**
     * Create a new TalonFX IO for a follower motor, initializing a TalonFX and all required
     * StatusSignals, and automatically following the lead motor specified in the config.
     *
     * <p>This factory method uses the default sets of medium- and high- priority signals. See
     * {@link MotorIOTalonFX#newFollower(MechanismConfig, int, TalonFXConfiguration,
     * SignalRefreshRates, int, int)} to configure the status signals in each group.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a follower
     *     motor.
     */
    public static MotorIOTalonFX newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates) {
        return new MotorIOTalonFX(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS);
    }

    /**
     * Create a new TalonFX IO for a follower motor, initializing a TalonFX and all required
     * StatusSignals, using the {@link SignalRefreshRates#defaults() default} SignalRefreshRates and
     * automatically following the lead motor specified in the config.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a follower
     *     motor.
     */
    public static MotorIOTalonFX newFollower(
            MechanismConfig config, int followerIndex, TalonFXConfiguration talonFXConfig) {
        return new MotorIOTalonFX(
                config,
                followerIndex,
                talonFXConfig,
                SignalRefreshRates.defaults(),
                DEFAULT_MEDIUM_PRIORITY_SIGNALS,
                DEFAULT_HIGH_PRIORITY_SIGNALS);
    }

    /**
     * Create a new TalonFX IO for a follower motor, initializing a TalonFX and all required
     * StatusSignals, and automatically following the lead motor specified in the config.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high- and medium-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a follower
     *     motor.
     */
    public static MotorIOTalonFX newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals) {
        return new MotorIOTalonFX(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals);
    }

    /**
     * Create a new TalonFX IO for a follower motor, initializing a TalonFX and all required
     * StatusSignals, and automatically following the lead motor specified in the config.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @param signalRefreshRates A SignalRefreshRates object containing the desired refresh rates
     *     for high-, medium-, and output-priority signals.
     * @param mediumPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the medium priority update frequency.
     * @param highPrioritySignals A bitmask of SIGNAL_* constants specifying which signals should
     *     receive the high priority update frequency (overrides medium priority).
     * @param outputSignals A bitmask of SIGNAL_* constants specifying which signals should receive
     *     the output update frequency (used by follower requests).
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a follower
     *     motor.
     */
    public static MotorIOTalonFX newFollower(
            MechanismConfig config,
            int followerIndex,
            TalonFXConfiguration talonFXConfig,
            SignalRefreshRates signalRefreshRates,
            int mediumPrioritySignals,
            int highPrioritySignals,
            int outputSignals) {
        return new MotorIOTalonFX(
                config,
                followerIndex,
                talonFXConfig,
                signalRefreshRates,
                mediumPrioritySignals,
                highPrioritySignals,
                outputSignals);
    }

    /**
     * Returns an array of BaseStatusSignal objects corresponding to the bits set in the given mask.
     *
     * @param mask A bitmask of SIGNAL_* constants.
     * @return An array of the matching BaseStatusSignal objects.
     */
    private BaseStatusSignal[] signalsForMask(int mask) {
        BaseStatusSignal[] result = new BaseStatusSignal[Integer.bitCount(mask & SIGNAL_ALL)];
        int i = 0;
        if ((mask & SIGNAL_POSITION) != 0) result[i++] = positionSignal;
        if ((mask & SIGNAL_VELOCITY) != 0) result[i++] = velocitySignal;
        if ((mask & SIGNAL_MOTOR_VOLTAGE) != 0) result[i++] = motorVoltageSignal;
        if ((mask & SIGNAL_STATOR_CURRENT) != 0) result[i++] = statorCurrentSignal;
        if ((mask & SIGNAL_SUPPLY_CURRENT) != 0) result[i++] = supplyCurrentSignal;
        if ((mask & SIGNAL_RAW_ROTOR_POSITION) != 0) result[i++] = rawRotorPositionSignal;
        if ((mask & SIGNAL_CLOSED_LOOP_OUTPUT) != 0) result[i++] = closedLoopOutputSignal;
        if ((mask & SIGNAL_CLOSED_LOOP_REFERENCE) != 0) result[i++] = closedLoopReferenceSignal;
        if ((mask & SIGNAL_CLOSED_LOOP_REFERENCE_SLOPE) != 0)
            result[i++] = closedLoopReferenceSlopeSignal;
        if ((mask & SIGNAL_TEMPERATURE) != 0) result[i++] = temperatureSignal;
        if ((mask & SIGNAL_TORQUE_CURRENT) != 0) result[i++] = torqueCurrentSignal;
        return result;
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        StatusCode code = BaseStatusSignal.refreshAll(signals);

        inputs.connected = code.isOK();

        disconnectedAlert.set(!code.isOK());

        if (code.isError()) {
            DriverStation.reportError(
                    deviceName + ": Failed to refresh status signals: " + code, false);
        } else if (code.isWarning()) {
            DriverStation.reportWarning(
                    deviceName + ": Warning while refreshing status signals: " + code, false);
        }

        inputs.positionRadians = positionSignal.getValue().in(Radians);
        inputs.velocityRadiansPerSecond = velocitySignal.getValue().in(RadiansPerSecond);
        inputs.appliedVolts = motorVoltageSignal.getValueAsDouble();
        inputs.statorCurrentAmps = statorCurrentSignal.getValueAsDouble();
        inputs.supplyCurrentAmps = supplyCurrentSignal.getValueAsDouble();
        inputs.rawRotorPositionRadians = rawRotorPositionSignal.getValue().in(Radians);
        inputs.closedLoopOutput = closedLoopOutputSignal.getValue();
        // These 2 status signals report a value in terms of rotations, convert it to radians to
        // ensure base-unit consistency.
        inputs.closedLoopReference =
                Units.rotationsToRadians(closedLoopReferenceSignal.getValueAsDouble());
        inputs.closedLoopReferenceSlope =
                Units.rotationsToRadians(closedLoopReferenceSlopeSignal.getValueAsDouble());
        inputs.tempCelsius = temperatureSignal.getValue().in(Celsius);
    }

    @Override
    public void controlNeutral() {
        requestUpdateFrequencyHz.ifPresent(frequency -> neutralRequest.withUpdateFreqHz(frequency));

        talon.setControl(neutralRequest);
    }

    @Override
    public void controlCoast() {
        requestUpdateFrequencyHz.ifPresent(frequency -> coastRequest.withUpdateFreqHz(frequency));

        talon.setControl(coastRequest);
    }

    @Override
    public void controlBrake() {
        requestUpdateFrequencyHz.ifPresent(frequency -> brakeRequest.withUpdateFreqHz(frequency));

        talon.setControl(brakeRequest);
    }

    @Override
    public void controlToPositionUnprofiled(Angle positionSetpoint) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> unprofiledPositionRequest.withUpdateFreqHz(frequency));

        talon.setControl(unprofiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToPositionProfiled(Angle positionSetpoint) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> profiledPositionRequest.withUpdateFreqHz(frequency));

        talon.setControl(profiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> dynamicProfiledPositionRequest.withUpdateFreqHz(frequency));

        talon.setControl(
                dynamicProfiledPositionRequest
                        .withPosition(positionSetpoint)
                        .withVelocity(maxVelocity)
                        .withAcceleration(maxAcceleration)
                        .withJerk(maxJerk));
    }

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint, MotionProfileConfig profileConfig) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> dynamicProfiledPositionRequest.withUpdateFreqHz(frequency));

        talon.setControl(
                dynamicProfiledPositionRequest
                        .withPosition(positionSetpoint)
                        .withVelocity(profileConfig.getMaxVelocity())
                        .withAcceleration(profileConfig.getMaxAcceleration())
                        .withJerk(profileConfig.getMaxJerk()));
    }

    @Override
    public void controlToPositionExpoProfiled(Angle positionSetpoint) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> expoProfiledPositionRequest.withUpdateFreqHz(frequency));

        talon.setControl(expoProfiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToVelocityUnprofiled(AngularVelocity velocity) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> unprofiledVelocityRequest.withUpdateFreqHz(frequency));

        talon.setControl(unprofiledVelocityRequest.withVelocity(velocity));
    }

    @Override
    public void controlToVelocityProfiled(AngularVelocity velocity) {
        requestUpdateFrequencyHz.ifPresent(
                frequency -> profiledVelocityRequest.withUpdateFreqHz(frequency));

        talon.setControl(profiledVelocityRequest.withVelocity(velocity));
    }

    @Override
    public void controlOpenLoopVoltage(Voltage voltage) {
        requestUpdateFrequencyHz.ifPresent(frequency -> voltageRequest.withUpdateFreqHz(frequency));

        talon.setControl(voltageRequest.withOutput(voltage));
    }

    @Override
    public void controlOpenLoopCurrent(Current current) {
        requestUpdateFrequencyHz.ifPresent(frequency -> currentRequest.withUpdateFreqHz(frequency));

        talon.setControl(currentRequest.withOutput(current));
    }

    @Override
    public void follow(int leaderId, boolean opposeLeaderDirection) {
        talon.setControl(
                new Follower(leaderId, CTREUtil.translateFollowerInvert(opposeLeaderDirection)));
    }

    @Override
    public void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {
        talon.getConfigurator()
                .apply(
                        new Slot0Configs()
                                .withKP(kP)
                                .withKI(kI)
                                .withKD(kD)
                                .withKS(kS)
                                .withKG(kG)
                                .withKV(kV)
                                .withKA(kA)
                                .withGravityType(
                                        config.gravityFeedforwardType
                                                .toPhoenix6GravityTypeValue()));
    }

    @Override
    public void setNeutralMode(NeutralMode neutralMode) {
        talon.setNeutralMode(CTREUtil.translateNeutralMode(neutralMode));
    }

    @Override
    public void setCurrentPosition(Angle position) {
        talon.setPosition(position);
    }

    @Override
    public void setProfileConstraints(MotionProfileConfig config) {
        CTREUtil.tryUntilOk(
                () -> talon.getConfigurator().apply(config.asMotionMagicConfigs()),
                id,
                (code) -> {
                    configFailedToApplyAlert.set(true);
                });
    }

    @Override
    public void setRequestUpdateFrequency(Frequency updateFrequency) {
        this.requestUpdateFrequencyHz = Optional.of(updateFrequency);
    }
}
