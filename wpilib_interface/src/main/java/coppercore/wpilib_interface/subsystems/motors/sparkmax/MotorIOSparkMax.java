package coppercore.wpilib_interface.subsystems.motors.sparkmax;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;

import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import coppercore.wpilib_interface.SparkUtil;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
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
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * The MotorIOSparkMax implements the MotorIO interface for the <a
 * href="https://docs.revrobotics.com/brushless/spark-max/overview">SparkMax</a> motor controller.
 *
 * <p>MotorIOSparkMax does not support measuring stator current or raw rotor position. These values
 * will be set to zero when updateInputs is called.
 *
 * <p>This IO implementation also does not support dynamic motion profiles, exponential motion
 * profiles, or current/FOC-based open-loop control. Calling any of the aforementioned control
 * methods will result in an UnsupportedOperationException.
 */
public class MotorIOSparkMax implements MotorIO {
    protected final MechanismConfig config;

    protected final CANDeviceID id;

    protected final SparkMaxConfig sparkMaxConfig;

    protected final SparkMax sparkMax;

    protected final SparkClosedLoopController controller;

    protected final String deviceName;

    private final Alert configFailedToApplyAlert;

    private final Alert disconnectedAlert;

    /**
     * Create a new MotorIOSparkMax given a mechanism config, a CANDeviceID, a SparkMaxConfig, and a
     * MotorType
     *
     * <p>This constructor initializes all required fields but doesn't handle leader vs. follower
     * behavior, so it is protected and is intended only to be called from a constructor that will
     * extract the lead motor or follower motor ID from the config.
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param id The CANDeviceID of the motor in question.
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     */
    protected MotorIOSparkMax(
            MechanismConfig config,
            CANDeviceID id,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType) {
        this.config = config;
        this.id = id;

        this.deviceName = config.name + "_SparkMax_" + id;

        // Copy the config since updating follow mode modifies the config in place.
        this.sparkMaxConfig = new SparkMaxConfig().apply(sparkMaxConfig);

        this.sparkMax = new SparkMax(id.id(), motorType);

        this.controller = sparkMax.getClosedLoopController();

        String configFailedToApplyMessage = deviceName + " failed to apply configs.";

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

        String disconnectedMessage = deviceName + " disconnected/is reporting an error.";

        this.disconnectedAlert = new Alert(disconnectedMessage, AlertType.kError);

        applyConfig();
    }

    /**
     * Create a new leader SparkMax IO, initializing a SparkMax object to interface with the motor
     * controller.
     *
     * <p>This constructor is used to create a lead motor IO. To create the IO for a follower motor,
     * use {@link MotorIOSparkMax#MotorIOSparkMax(MechanismConfig, int, SparkMaxConfig, MotorType)}.
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     */
    public MotorIOSparkMax(
            MechanismConfig config, SparkMaxConfig sparkMaxConfig, MotorType motorType) {
        this(config, config.leadMotorId, sparkMaxConfig, motorType);
    }

    /**
     * Create a new SparkMax IO for a leader motor.
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     * @return A new MotorIOSparkMax created with the lead motor ID from the config and the
     *     specified configs.
     */
    public static MotorIOSparkMax newLeader(
            MechanismConfig config, SparkMaxConfig sparkMaxConfig, MotorType motorType) {
        return new MotorIOSparkMax(config, sparkMaxConfig, motorType);
    }

    /**
     * Create a new follower SparkMax IO, initializing a SparkMax object to interface with the motor
     * controller.
     *
     * <p>This constructor is used to create a follower motor IO. To create the IO for the leader
     * motor, use {@link MotorIOSparkMax#MotorIOSparkMax(MechanismConfig, SparkMaxConfig,
     * MotorType)}.
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     */
    public MotorIOSparkMax(
            MechanismConfig config,
            int followerIndex,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType) {
        this(config, config.followerMotorConfigs[followerIndex].id(), sparkMaxConfig, motorType);
        follow(config.leadMotorId.id(), config.followerMotorConfigs[followerIndex].invert());
    }

    /**
     * Create a new follower SparkMax IO, initializing a SparkMax object to interface with the motor
     * controller.
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param followerIndex An int containing the index of the follower motor (what position in
     *     config.followerIds this motor is). This IO will automatically follow the lead motor at
     *     the end of its constructor.
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     * @return A new MotorIOSparkMax configured with the correct ID and invert based on the follower
     *     configs in the specified MechanismConfig.
     */
    public static MotorIOSparkMax newFollower(
            MechanismConfig config,
            int followerIndex,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType) {
        return new MotorIOSparkMax(config, followerIndex, sparkMaxConfig, motorType);
    }

    /**
     * Uses tryUntilOk to apply sparkMaxConfig until it succeeds. If the config failed to apply
     * supplier has been initialized (is not null), this will set it to display when a config fails
     * to applUses tryUntilOk to apply sparkMaxConfig until it succeeds. If the config failed to
     * apply supplier has been initialized (is not null), this will set it to display when a config
     * fails to apply.
     */
    private void applyConfig() {
        SparkUtil.tryUntilOk(
                () ->
                        sparkMax.configure(
                                sparkMaxConfig,
                                ResetMode.kResetSafeParameters,
                                PersistMode.kPersistParameters),
                id,
                (err) -> {
                    if (configFailedToApplyAlert != null) {
                        configFailedToApplyAlert.set(true);
                    }
                });
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        boolean connected = true;

        // No way to get this value from the motor controller
        inputs.statorCurrentAmps = 0.0;
        inputs.rawRotorPositionRadians = 0.0;

        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getEncoder().getPosition(),
                        (positionRotations) ->
                                inputs.positionRadians =
                                        Units.rotationsToRadians(positionRotations));
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getEncoder().getVelocity(),
                        (velocityRPM) ->
                                inputs.velocityRadiansPerSecond =
                                        Units.rotationsPerMinuteToRadiansPerSecond(velocityRPM));
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAppliedOutput() * sparkMax.getBusVoltage(),
                        (appliedVolts) -> inputs.appliedVolts = appliedVolts);
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        sparkMax::getOutputCurrent,
                        (current) -> inputs.supplyCurrentAmps = current);

        inputs.connected = connected;

        if (!connected) {
            DriverStation.reportError(
                    deviceName + ": Reading inputs caused error: " + sparkMax.getLastError(),
                    false);
        }

        disconnectedAlert.set(!connected);
    }

    @Override
    public void controlNeutral() {
        sparkMax.set(0.0);
    }

    @Override
    public void controlCoast() {
        throw new UnsupportedOperationException(
                "Explicit brake requests are not supported by MotorIOSparkMax");
    }

    @Override
    public void controlBrake() {
        throw new UnsupportedOperationException(
                "Explicit brake requests are not supported by MotorIOSparkMax");
    }

    @Override
    public void controlToPositionUnprofiled(Angle positionSetpoint) {
        controller.setReference(positionSetpoint.in(Rotations), ControlType.kPosition);
    }

    @Override
    public void controlToPositionProfiled(Angle positionSetpoint) {
        controller.setReference(
                positionSetpoint.in(Rotations), ControlType.kMAXMotionPositionControl);
    }

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {
        // Dynamic motion profiles would have to be implemented here in the IO, since they are not
        // supported by the motor controller. This means that we would be requiring the user to fire
        // all closed-loop control requests periodically and would also result in lower-frequency
        // motion profiling and inconsistent behavior between dynamic and non-dynamic profiles. To
        // future programmers: if this is a required feature and you deem it to be worth the
        // inconsistencies caused, feel free to implement it.
        throw new UnsupportedOperationException("Dynamic profiles are not supported in Spark IOs.");
    }

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint, MotionProfileConfig profileConfig) {
        throw new UnsupportedOperationException("Dynamic profiles are not supported in Spark IOs.");
    }

    @Override
    public void controlToPositionExpoProfiled(Angle positionSetpoint) {
        throw new UnsupportedOperationException(
                "Exponential profiles are not supported in Spark IOs.");
    }

    @Override
    public void controlToVelocityUnprofiled(AngularVelocity velocitySetpoint) {
        controller.setReference(velocitySetpoint.in(RPM), ControlType.kVelocity);
    }

    @Override
    public void controlToVelocityProfiled(AngularVelocity velocitySetpoint) {
        controller.setReference(velocitySetpoint.in(RPM), ControlType.kMAXMotionVelocityControl);
    }

    @Override
    public void controlOpenLoopVoltage(Voltage voltage) {
        sparkMax.setVoltage(voltage);
    }

    @Override
    public void controlOpenLoopCurrent(Current current) {
        throw new UnsupportedOperationException(
                "Current-based open-loop control is not supported by Spark IOs.");
    }

    @Override
    public void follow(int leaderId, boolean opposeLeaderDirection) {
        sparkMaxConfig.follow(leaderId, opposeLeaderDirection);

        applyConfig();
    }

    @Override
    public void setProfileConstraints(MotionProfileConfig profileConfig) {
        sparkMaxConfig.closedLoop.apply(profileConfig.asMaxMotionConfig());

        applyConfig();
    }

    @Override
    public void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {
        // TODO: Decide on whether adding manual calculation of feedforward is worth it
        sparkMaxConfig.closedLoop.pidf(kP, kI, kD, kV);
        applyConfig();
    }

    @Override
    public void setNeutralMode(NeutralMode neutralMode) {
        sparkMaxConfig.idleMode(SparkUtil.translateNeutralMode(neutralMode));

        applyConfig();
    }

    @Override
    public void setCurrentPosition(Angle position) {
        sparkMax.getEncoder().setPosition(position.in(Rotations));
    }
}
