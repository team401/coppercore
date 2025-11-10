package coppercore.wpilib_interface.subsystems.motors;

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
import coppercore.wpilib_interface.subsystems.motors.profile.MotionProfileConfig;
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
import java.util.Optional;

public class MotorIOSparkMax implements MotorIO {
    protected final MechanismConfig config;

    protected final CANDeviceID id;

    protected final SparkMaxConfig sparkMaxConfig;

    protected final SparkMax sparkMax;

    protected final SparkClosedLoopController controller;

    protected final String deviceName;

    protected boolean absoluteEncoderRequired = false;

    private final Alert configFailedToApplyAlert;

    private final Alert disconnectedAlert;

    /**
     * Create a new SparkMax IO, initializing a SparkMax
     *
     * <p>By default, an absolute encoder is not required. When using an absolute encoder, call
     * {@link requireAbsoluteEncoder} to make the `connected` field of the input factor in absolute
     * encoder refresh status. For example:
     *
     * <pre>{@code
     * new ScoringSubsystem(new MotorIOSparkMax(
     *     config,
     *     followerIndex,
     *     sparkMaxConfig,
     *     motorType)
     *         .requireAbsoluteEncoder());
     * }</pre>
     *
     * @param config A MechanismConfig to use for CAN IDs
     * @param followerIndex An Optional containing either the index of the follower motor (what
     *     position in config.followerIds this motor is) or None if this is the lead motor. If
     *     followerIndex is not None, this IO will automatically follow the lead motor at the end of
     *     its constructor.
     * @param sparkMaxConfig A SparkMaxConfig to apply to the motor.
     * @param motorType The motor type (brushless or brushed)
     */
    public MotorIOSparkMax(
            MechanismConfig config,
            Optional<Integer> followerIndex,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType) {
        this.config = config;
        this.id =
                followerIndex
                        .map((idx) -> config.followerMotorConfigs[idx].id())
                        .orElse(config.leadMotorId);

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

        if (followerIndex.isPresent()) {
            follow(
                    config.leadMotorId.id(),
                    config.followerMotorConfigs[followerIndex.get()].invert());
        }
    }

    /**
     * Make `updateInputs` report a disconnected motor when the absolute encoder position fails to
     * refresh.
     *
     * <p>By default, a failure to read an absolute encoder is ignored, as it is an optional part of
     * the motor configuration.
     *
     * @return This MotorIOSparkMax, for easy method chaining.
     */
    public MotorIOSparkMax requireAbsoluteEncoder() {
        this.absoluteEncoderRequired = true;

        return this;
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

        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getEncoder().getVelocity(),
                        (velocityRPM) -> inputs.velocity.mut_replace(velocityRPM, RPM));
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getEncoder().getPosition(),
                        (positionRotations) ->
                                inputs.position.mut_replace(positionRotations, Rotations));
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

        boolean absoluteEncoderConnected =
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAbsoluteEncoder().getPosition(),
                        (rotorPositionRotations) ->
                                inputs.rawRotorPosition.mut_replace(
                                        rotorPositionRotations, Rotations));

        if (absoluteEncoderRequired) {
            connected &= absoluteEncoderConnected;
        }

        inputs.connected = connected;

        if (!connected) {
            DriverStation.reportError(
                    deviceName + ": Reading inputs caused error: " + sparkMax.getLastError(),
                    false);
        }

        disconnectedAlert.set(!connected);
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
                "Current open-loop control is not supported by Spark IOs.");
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
