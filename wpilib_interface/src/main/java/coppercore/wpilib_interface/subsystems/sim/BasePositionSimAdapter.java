package coppercore.wpilib_interface.subsystems.sim;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * The BasePositionSimAdapter class serves as a base class for classes that implement
 * PositionSimAdapter and defines interfaces for protected abstract methods for DummySimAdapter to
 * use to directly modify the state of PositionSimAdapters without publicly exposing these methods
 * to other classes outside of the project.
 */
public abstract class BasePositionSimAdapter implements PositionSimAdapter {
    /**
     * The MechanismConfig used for this mechanism. This is required to calculate gear ratios
     * properly for simulation.
     */
    protected final MechanismConfig config;

    /**
     * Constructs a new BasePositionSim, initializing this class's stored config.
     *
     * @param config The MechanismConfig used for this mechanism. This is required to calculate gear
     *     ratios properly for simulation.
     */
    protected BasePositionSimAdapter(MechanismConfig config) {
        this.config = config;
    }

    /**
     * Manually sets the physics sim's position and velocity. This is intended to be used by the
     * DummySimAdapter to mock different positions and observe how a physics sim responds.
     *
     * <p>This method must NOT be called by normal sim code unless you know what you are doing. It
     * will instantly set the position of a mechanism with no regard for physical limitations.
     *
     * @param motorAngle An Angle representing the motor angle to set.
     * @param motorVelocity An AngularVelocity representing the motor velocity to set.
     */
    protected abstract void setState(Angle motorAngle, AngularVelocity motorVelocity);

    /**
     * Gets the ElevatorMechanismConfig associated with this adapter.
     *
     * <p>This method exists to provide DummySimAdapter a clean interface to wrap an adapter while
     * still providing it with a means to access the mechanism config.
     *
     * @return The MechanismConfig provided to this sim adapter.
     */
    protected MechanismConfig getConfig() {
        return this.config;
    }
}
