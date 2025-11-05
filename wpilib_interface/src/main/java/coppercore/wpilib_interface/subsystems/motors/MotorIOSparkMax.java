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

    private final Alert configFailedToApplyAlert;

    private final Alert disconnectedAlert;

    /**
     * Create a new SparkMax IO, initializing a SparkMax
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

        this.deviceName =
                new StringBuilder().append(config.name).append("_SparkMax_").append(id).toString();

        this.sparkMaxConfig = sparkMaxConfig;

        this.sparkMax = new SparkMax(id.id(), motorType);

        this.controller = sparkMax.getClosedLoopController();

        String configFailedToApplyMessage =
                new StringBuilder()
                        .append(deviceName)
                        .append(" failed to apply configs.")
                        .toString();

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

        String disconnectedMessage =
                new StringBuilder()
                        .append(deviceName)
                        .append(" disconnected/is reporting an error.")
                        .toString();

        this.disconnectedAlert = new Alert(disconnectedMessage, AlertType.kError);

        SparkUtil.tryUntilOk(
                () ->
                        sparkMax.configure(
                                sparkMaxConfig,
                                ResetMode.kResetSafeParameters,
                                PersistMode.kPersistParameters),
                id,
                (err) -> {
                    configFailedToApplyAlert.set(true);
                });

        if (followerIndex.isPresent()) {
            follow(
                    config.leadMotorId.id(),
                    config.followerMotorConfigs[followerIndex.get()].invert());
        }
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        boolean connected = true;

        // No way to get this value from the motor controller
        inputs.statorCurrentAmps = 0.0;

        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAlternateEncoder().getVelocity(),
                        (value) -> inputs.velocity.mut_replace(value, RPM));
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAlternateEncoder().getPosition(),
                        (value) -> inputs.position.mut_replace(value, Rotations));
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAppliedOutput() * sparkMax.getBusVoltage(),
                        (value) -> inputs.appliedVolts = value);
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        sparkMax::getOutputCurrent,
                        (value) -> inputs.supplyCurrentAmps = value);
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAbsoluteEncoder().getPosition(),
                        (value) -> inputs.rawRotorPosition.mut_replace(value, Rotations));

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
                "Exponential profiles are not yet supported in Spark IOs.");
    }

    @Override
    public void controlToVelocityUnprofiled(AngularVelocity velocitySetpoint) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'controlToVelocityUnprofiled'");
    }

    @Override
    public void controlToVelocityProfiled(AngularVelocity velocitySetpoint) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'controlToVelocityProfiled'");
    }

    @Override
    public void controlOpenLoop(Voltage voltage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'controlOpenLoop'");
    }

    @Override
    public void controlOpenLoop(Current current) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'controlOpenLoop'");
    }

    @Override
    public void follow(int leaderId, boolean opposeLeaderDirection) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public void setProfileConstraints(MotionProfileConfig profileConfig) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setProfileConstraints'");
    }

    @Override
    public void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setGains'");
    }

    @Override
    public void setBrakeMode(boolean shouldBrake) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBrakeMode'");
    }

    @Override
    public void setCurrentPosition(Angle position) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCurrentPosition'");
    }
}
