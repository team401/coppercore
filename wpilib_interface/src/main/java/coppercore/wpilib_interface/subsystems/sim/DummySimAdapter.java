package coppercore.wpilib_interface.subsystems.sim;

import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * The DummySimAdapter class provides a way to manually set the states used in a
 * CoppercoreSimAdapter. This is intended to be used for unit tests.
 *
 * <p>This class wraps an instance of another CoppercoreSimAdapter, keeping track of values flowing
 * in and out of the sim.
 *
 * <p>DummySimAdapter also keeps track of the last delta time and voltage passed to update (which is
 * a no-op) in case unit tests need to check the values passed into the adapter.
 */
public class DummySimAdapter implements CoppercoreSimAdapter {
    private final BaseSimAdapter wrappedAdapter;

    private Voltage lastMotorAppliedOutput = Volts.zero();
    private double lastDeltaTimeSeconds = 0.0;

    /**
     * Creates a new DummySimAdapter, given a BaseCoppercoreSimAdapter to wrap.
     *
     * @param adapter The BaseCoppercoreSimAdapter to wrap.
     */
    public DummySimAdapter(BaseSimAdapter adapter) {
        this.wrappedAdapter = adapter;
    }

    @Override
    public Angle getMotorPosition() {
        return wrappedAdapter.getMotorPosition();
    }

    @Override
    public AngularVelocity getMotorAngularVelocity() {
        return wrappedAdapter.getMotorAngularVelocity();
    }

    @Override
    public Angle getEncoderPosition() {
        return wrappedAdapter.getEncoderPosition();
    }

    @Override
    public AngularVelocity getEncoderAngularVelocity() {
        return wrappedAdapter.getEncoderAngularVelocity();
    }

    @Override
    public Current getCurrentDraw() {
        return wrappedAdapter.getCurrentDraw();
    }

    @Override
    public void update(Voltage motorAppliedOutput, double deltaTimeSeconds) {
        this.lastMotorAppliedOutput = motorAppliedOutput;
        this.lastDeltaTimeSeconds = deltaTimeSeconds;

        wrappedAdapter.update(motorAppliedOutput, deltaTimeSeconds);
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
     *
     * @return a double, the last delta time measured in seconds.
     */
    public double getLastDeltaTimeSeconds() {
        return this.lastDeltaTimeSeconds;
    }

    /**
     * Update the motor position and velocity to be provided to IOs by manually setting the state of
     * the physics sim in the wrapped adapter.
     *
     * <p>This method also updates encoder position and velocity based on the ratios specified in
     * the mechanism config passed to the CoppercoreSimAdapter that this DummySimAdapter is
     * wrapping.
     *
     * @param motorPosition An Angle, the new motor position
     * @param motorVelocity An AngularVelocity, the new motor angular velocity
     */
    public void setState(Angle motorPosition, AngularVelocity motorVelocity) {
        wrappedAdapter.setState(motorPosition, motorVelocity);
    }
}
