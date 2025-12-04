package coppercore.wpilib_interface.subsystems.sim;

/**
 * The TestableSimIO interface provides a method to poll the IO in sim, which allows for manually
 * updating timestamps and physics sims during unit tests as if it were a real simulation.
 *
 * <p>TestableSimIOs should keep everything possible in their updateInputs methods as normal, but
 * certain actions that *only* need to take place in unit tests should go in unitTestPeriodic().
 *
 * <p>This is needed for manually refreshing status signals or waiting for new data in sim TalonFX
 * IOs.
 */
public interface TestableSimIO {
    /**
     * Updates/refreshes sim data or status signals that would normally be refreshed automatically
     * if running in a normal robot project.
     */
    public void unitTestPeriodic();
}
