package coppercore.wpilib_interface.subsystems.encoders;

import static edu.wpi.first.units.Units.Rotations;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.ctre.phoenix6.sim.CANcoderSimState;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter;
import edu.wpi.first.wpilibj.Timer;

/**
 * The EncoderIOCANCoderPositionSim class uses Phoenix-6 device simulation (CANCoderSimState) to
 * read values from a PositionSimAdapter periodically and update its values to a CANCoderSimState,
 * while performing the same code-side behavior as the real life EncoderIOCANCoder.
 *
 * <p>The same PositionSimAdapter should (and can safely) be passed to all
 * MotorIOTalonFXPositionSims and EncoderIOCANCoderPositionSims for each mechanism because only the
 * lead motor MotorIOTalonFXPositionSim will actually update the input the sim while the others will
 * only read values.
 */
public class EncoderIOCANCoderPositionSim extends EncoderIOCANCoder {
    protected final PositionSimAdapter physicsSimAdapter;

    protected final CANcoderSimState cancoderSimState;

    protected final Timer deltaTimer = new Timer();
    protected double lastTimestamp;

    /**
     * Create a new simulated CANcoder IO, initializing a new CANcoder and all required signals.
     *
     * @param id A CANDeviceID containing the integer ID and String CAN bus that the device is on.
     * @param cancoderConfig A Phoenix-6 CANcoderConfiguration to apply to the CANcoder. This config
     *     will not be mutated by this IO.
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for mechanism physics
     *     simulation readings.
     */
    public EncoderIOCANCoderPositionSim(
            CANDeviceID id,
            CANcoderConfiguration cancoderConfig,
            PositionSimAdapter physicsSimAdapter) {
        super(id, cancoderConfig);

        this.cancoderSimState = this.cancoder.getSimState();

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.restart();
        this.lastTimestamp = deltaTimer.get();
    }

    @Override
    public void updateInputs(EncoderInputs inputs) {
        // TODO: Figure out how to *correctly* handle inverts so that motor positions aren't always
        // negative.
        updateSimState();

        super.updateInputs(inputs);
    }

    /**
     * Read the state of the mechanism physics sim and send the values to the CANcoderSimState.
     *
     * <p>This value will not update the simulation to avoid conflicts. This means that the sim must
     * be updated by the lead motor's sim IO.
     */
    private void updateSimState() {
        boolean isDirectionBackward =
                cancoderConfig.MagnetSensor.SensorDirection
                        != SensorDirectionValue.CounterClockwise_Positive;
        double invertMultiplier = isDirectionBackward ? -1.0 : 1.0;
        var encoderPos =
                physicsSimAdapter
                        .getEncoderPosition()
                        .times(invertMultiplier)
                        .plus(Rotations.of(this.cancoderConfig.MagnetSensor.MagnetOffset));

        cancoderSimState.setRawPosition(encoderPos);
        cancoderSimState.setVelocity(physicsSimAdapter.getEncoderAngularVelocity());
    }
}
