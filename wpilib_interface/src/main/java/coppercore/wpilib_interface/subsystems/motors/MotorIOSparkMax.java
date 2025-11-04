package coppercore.wpilib_interface.subsystems.motors;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import coppercore.wpilib_interface.SparkUtil;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import java.util.Optional;

public class MotorIOSparkMax implements MotorIO {
    protected final MechanismConfig config;

    protected final CANDeviceID id;

    protected final SparkMaxConfig sparkMaxConfig;

    protected final SparkMax sparkMax;

    protected final String deviceName;

    private final Alert configFailedToApplyAlert;

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

        String configFailedToApplyMessage =
                new StringBuilder()
                        .append(deviceName)
                        .append(" failed to apply configs.")
                        .toString();

        this.configFailedToApplyAlert = new Alert(configFailedToApplyMessage, AlertType.kError);

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

        // No way to know this
        inputs.supplyCurrentAmps = 0.0;

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
                        (value) -> inputs.statorCurrentAmps = value);
        connected &=
                SparkUtil.ifOk(
                        sparkMax,
                        () -> sparkMax.getAbsoluteEncoder().getPosition(),
                        (value) -> inputs.rawRotorPosition.mut_replace(value, Rotations));

        inputs.connected = connected;
    }
}
