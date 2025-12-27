package coppercore.wpilib_interface.subsystems.sim;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * The DummySimAdapter class provides a way to manually set the positions used in a
 * PositionSimAdapter. This is intended to be used for unit tests.
 *
 * <p>DummySimAdapter also keeps track of the last delta time and voltage passed to update (which is
 * a no-op) in case unit tests need to check the values passed into the adapter.
 */
public class DummySimAdapter implements PositionSimAdapter {
    private Angle motorPosition = Radians.zero();
    private AngularVelocity motorAngularVelocity = RadiansPerSecond.zero();
    private Angle encoderPosition = Radians.zero();
    private AngularVelocity encoderAngularVelocity = RadiansPerSecond.zero();
    private Current currentDraw = Amps.zero();

    private Voltage lastMotorAppliedOutput = Volts.zero();
    private double lastDeltaTimeSeconds = 0.0;

    @Override
    public Angle getMotorPosition() {
        return this.motorPosition;
    }

    @Override
    public AngularVelocity getMotorAngularVelocity() {
        return this.motorAngularVelocity;
    }

    @Override
    public Angle getEncoderPosition() {
        return this.encoderPosition;
    }

    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        return this.encoderAngularVelocity;
    }

    @Override
    public Current getCurrentDraw() {
        return this.currentDraw;
    }

    @Override
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds) {
        this.lastMotorAppliedOutput = motorAppliedOutput;
        this.lastDeltaTimeSeconds = deltaTimeSeconds;
    }

    /**
     * Get the value of motorAppliedOutput passed into the most recent call to {@link
     * DummySimAdapter#update(Voltage, double)}
     *
     * @return A Voltage, the last motorAppliedOutput passed to `update`
     */
    public Voltage getLastMotorAppliedOutput() {
        return this.lastMotorAppliedOutput;
    }

    /**
     * Get the value of deltaTimeSeconds passed into the most recent call to {@link
     * DummySimAdapter#update(Voltage, double)}
     */
    public double getLastDeltaTimeSeconds() {
        return this.lastDeltaTimeSeconds;
    }

    /**
     * Update the motor position to be provided to IOs.
     *
     * @param motorPosition An Angle, the new motor position
     */
    public void setMotorPosition(Angle motorPosition) {
        this.motorPosition = motorPosition;
    }

    /**
     * Update the motor angular velocity to be provided to IOs.
     *
     * @param motorAngularVelocity An AngularVelocity, the new motor angular velocity
     */
    public void setMotorAngularVelocity(AngularVelocity motorAngularVelocity) {
        this.motorAngularVelocity = motorAngularVelocity;
    }

    /**
     * Update the encoder position to be provided to IOs.
     *
     * @param encoderPosition An Angle, the new encoder position
     */
    public void setEncoderPosition(Angle encoderPosition) {
        this.encoderPosition = encoderPosition;
    }

    /**
     * Update the encoder angular velocity to be provided to IOs.
     *
     * @param encoderAngularVelocity An AngularVelocity, the new encoder angular velocity
     */
    public void setEncoderAnguarVelocity(AngularVelocity encoderAngularVelocity) {
        this.encoderAngularVelocity = encoderAngularVelocity;
    }

    /**
     * Update the current draw to be provided to IOs.
     *
     * @param currentDraw A Current, the new current draw
     */
    public void setCurrentDraw(Current currentDraw) {
        this.currentDraw = currentDraw;
    }
}
