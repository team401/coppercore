package coppercore.wpilib_interface.subsystems.motors.talonfx;

import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
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
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * A base motor IO that implements closed-loop control for a TalonFX-supporting motor using
 * MotionMagicExpo, MotionMagicVelocity, and TorqueCurrentFOC wherever possible.
 */
public class MotorIOTalonFX extends CanBusMotorControllerBase implements MotorIO {
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

    /** Applied Voltage StatusSignal cached for easy repeated access */
    protected final StatusSignal<Voltage> appliedVoltageSignal;

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

    /** Array of status signals to be easily passed to refreshAll */
    protected final BaseStatusSignal[] signals;

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
     * Create a new MotorIOTalonFX given a mechanism config, a CANDeviceID, and a
     * TalonFXConfiguration.
     *
     * <p>This constructor initializes all required fields but doesn't handle leader vs. follower
     * behavior, so it is protected and is intended only to be called from a constructor that will
     * extract the lead motor or follower motor ID from the config.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param id The CANDeviceID of the motor in question.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     */
    protected MotorIOTalonFX(
            MechanismConfig config, CANDeviceID id, TalonFXConfiguration talonFXConfig) {
        super(config, id, "_TalonFX_");

        this.talonFXConfig = talonFXConfig;

        this.talon = new TalonFX(id.id(), new CANBus(id.canbus()));

        CTREUtil.tryUntilOk(
                () -> talon.getConfigurator().apply(talonFXConfig),
                id,
                (code) -> {
                    configFailedToApplyAlert.set(true);
                });

        this.positionSignal = talon.getPosition();
        this.velocitySignal = talon.getVelocity();
        this.appliedVoltageSignal = talon.getMotorVoltage();
        this.statorCurrentSignal = talon.getStatorCurrent();
        this.supplyCurrentSignal = talon.getSupplyCurrent();
        this.rawRotorPositionSignal = talon.getRotorPosition();
        this.closedLoopOutputSignal = talon.getClosedLoopOutput();
        this.closedLoopReferenceSignal = talon.getClosedLoopReference();
        this.closedLoopReferenceSlopeSignal = talon.getClosedLoopReferenceSlope();

        this.signals =
                new BaseStatusSignal[] {
                    velocitySignal,
                    positionSignal,
                    appliedVoltageSignal,
                    statorCurrentSignal,
                    supplyCurrentSignal,
                    rawRotorPositionSignal,
                    closedLoopOutputSignal,
                    closedLoopReferenceSignal,
                    closedLoopReferenceSlopeSignal,
                };

        CTREUtil.tryUntilOk(
                () -> BaseStatusSignal.setUpdateFrequencyForAll(50.0, signals), id, (code) -> {});
        CTREUtil.tryUntilOk(() -> talon.optimizeBusUtilization(), id, (code) -> {});

        this.dynamicProfiledPositionRequest =
                new DynamicMotionMagicTorqueCurrentFOC(
                                0,
                                talonFXConfig.MotionMagic.MotionMagicCruiseVelocity,
                                talonFXConfig.MotionMagic.MotionMagicAcceleration)
                        .withJerk(talonFXConfig.MotionMagic.MotionMagicJerk);
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for the "lead motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, int, TalonFXConfiguration)} to create a
     * follower.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     */
    public MotorIOTalonFX(MechanismConfig config, TalonFXConfiguration talonFXConfig) {
        this(config, config.leadMotorId, talonFXConfig);
    }

    /**
     * Create a new TalonFX IO for a lead motor, initializing a TalonFX and all required
     * StatusSignals
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a lead
     *     motor.
     */
    public static MotorIOTalonFX newLeader(
            MechanismConfig config, TalonFXConfiguration talonFXConfig) {
        return new MotorIOTalonFX(config, talonFXConfig);
    }

    /**
     * Create a new TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * <p>This constructor is for a "follower motor". Use {@link
     * MotorIOTalonFX#MotorIOTalonFX(MechanismConfig, TalonFXConfiguration)} to create the leader.
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     */
    public MotorIOTalonFX(
            MechanismConfig config, int followerIndex, TalonFXConfiguration talonFXConfig) {
        this(config, config.followerMotorConfigs[followerIndex].id(), talonFXConfig);

        follow(config.leadMotorId.id(), config.followerMotorConfigs[followerIndex].invert());
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
     * @return A new MotorIOTalonFX created with the specified parameters, configured as a follower
     *     motor.
     */
    public static MotorIOTalonFX newFollower(
            MechanismConfig config, int followerIndex, TalonFXConfiguration talonFXConfig) {
        return new MotorIOTalonFX(config, followerIndex, talonFXConfig);
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
        inputs.appliedVolts = appliedVoltageSignal.getValueAsDouble();
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
    }

    @Override
    public void controlNeutral() {
        talon.setControl(neutralRequest);
    }

    @Override
    public void controlCoast() {
        talon.setControl(coastRequest);
    }

    @Override
    public void controlBrake() {
        talon.setControl(brakeRequest);
    }

    @Override
    public void controlToPositionUnprofiled(Angle positionSetpoint) {
        talon.setControl(unprofiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToPositionProfiled(Angle positionSetpoint) {
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
        talon.setControl(
                dynamicProfiledPositionRequest
                        .withPosition(positionSetpoint)
                        .withVelocity(profileConfig.getMaxVelocity())
                        .withAcceleration(profileConfig.getMaxAcceleration())
                        .withJerk(profileConfig.getMaxJerk()));
    }

    @Override
    public void controlToPositionExpoProfiled(Angle positionSetpoint) {
        talon.setControl(expoProfiledPositionRequest.withPosition(positionSetpoint));
    }

    @Override
    public void controlToVelocityUnprofiled(AngularVelocity velocity) {
        talon.setControl(unprofiledVelocityRequest.withVelocity(velocity));
    }

    @Override
    public void controlToVelocityProfiled(AngularVelocity velocity) {
        talon.setControl(profiledVelocityRequest.withVelocity(velocity));
    }

    @Override
    public void controlOpenLoopVoltage(Voltage voltage) {
        talon.setControl(voltageRequest.withOutput(voltage));
    }

    @Override
    public void controlOpenLoopCurrent(Current current) {
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
}
