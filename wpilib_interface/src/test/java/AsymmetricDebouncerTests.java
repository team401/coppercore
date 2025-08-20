import static edu.wpi.first.units.Units.Microseconds;
import static edu.wpi.first.units.Units.Seconds;

import coppercore.wpilib_interface.AsymmetricDebouncer;
import edu.wpi.first.util.WPIUtilJNI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsymmetricDebouncerTests {
    /** Enables WPIUtilJNI mock time and set the time to 0.0 seconds */
    @BeforeEach
    @SuppressWarnings("unused")
    void setUpMockTime() {
        WPIUtilJNI.enableMockTime();
        setMockTimeSeconds(0.0);
    }

    /** Disabler mock time in case some other test requires the real time */
    @AfterEach
    @SuppressWarnings("unused")
    void disableMockTime() {
        WPIUtilJNI.disableMockTime();
    }

    /**
     * Set WPIUtilJni's mock time to timeSeconds
     *
     * <p>This exists as a wrapper because WPIUtilJNI.setMockTime expects the time as a long in
     * microseconds
     *
     * @param timeSeconds the time to set, in seconds
     */
    private void setMockTimeSeconds(double timeSeconds) {
        WPIUtilJNI.setMockTime((long) Seconds.of(timeSeconds).in(Microseconds));
    }

    /**
     * Tests a debouncer with an instant rise and fall time:
     *
     * <p>Verifies that the value instantly rises and falls, and remains true/false when passed the
     * same value multiple times in a row.
     */
    @Test
    void instantRiseAndFall() {
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(0.0, 0.0);

        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertTrue(debouncer.calculate(true));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertFalse(debouncer.calculate(false));
        Assertions.assertTrue(debouncer.calculate(true));
    }

    /**
     * Tests a debouncer with an instant rise time but a non-instant (1.0s) fall time. Tests that:
     *
     * <ul>
     *   <li>The debouncer initializes to false
     *   <li>It doesn't fall in less than 1.0 seconds
     *   <li>An instant of 'true' signal resets the fall timer
     *   <li>It falls to false >= 1.0 seconds from the last 'true' signal
     *   <li>It instantly rises back to 'true' on the first 'true' signal
     * </ul>
     */
    @Test
    void instantRiseSlowFall() {
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(0.0, 1.0);

        Assertions.assertFalse(debouncer.calculate(false), "Debouncer must initialize to false.");

        Assertions.assertTrue(
                debouncer.calculate(true), "0.0s rise time means debouncer should rise instantly");

        setMockTimeSeconds(0.5);
        Assertions.assertTrue(
                debouncer.calculate(false), "0.5s < 1.0s fall time, result should remain true");

        // Send in a true value to reset the time on the value falling to false
        debouncer.calculate(true);

        setMockTimeSeconds(1.0);
        Assertions.assertTrue(
                debouncer.calculate(false), "'true' signal should have reset fall time at t=0.5s");

        setMockTimeSeconds(1.5);
        Assertions.assertFalse(
                debouncer.calculate(false),
                "1.5s - 0.5s >= 1.0s fall time, result should fall to false");
    }

    /**
     * Tests a debouncer with a non-instant (1.0s) rise time and an instant fall time. Tests that:
     *
     * <ul>
     *   <li>The debouncer initializes to false
     *   <li>It doesn't rise in less than 1.0 seconds
     *   <li>An instant of 'false' signal resets the rise timer
     *   <li>It rises to true >= 1.0 seconds from the last 'false' signal
     *   <li>It instantly falls back to 'false' on the first 'false' signal
     * </ul>
     */
    @Test
    void slowRiseInstantFall() {
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(1.0, 0.0);

        Assertions.assertFalse(debouncer.calculate(false), "Debouncer must initialize to false.");

        setMockTimeSeconds(0.99);
        Assertions.assertFalse(
                debouncer.calculate(true), "0.99s < 1.0s rise time, result should remain false");

        // Send in a false signal to reset the rise timer at 0.99s (will wait until 1.99s to rise
        // again)
        debouncer.calculate(false);

        setMockTimeSeconds(1.0);
        Assertions.assertFalse(
                debouncer.calculate(true),
                "'false' signal should have reset rise timer at t=0.99s");

        setMockTimeSeconds(1.99);
        Assertions.assertTrue(
                debouncer.calculate(true),
                "1.99s-0.99s >= 1.0s rise time, result should rise to true");

        Assertions.assertFalse(
                debouncer.calculate(false), "0.0s fall time means debouncer should fall instantly");
    }

    /**
     * Tests a debouncer with a non-instant (1.0s) rise and non-instant (1.5s) fall time. Tests
     * that:
     *
     * <ul>
     *   <li>Initializes to false
     *   <li>Doesn't rise in under 1.0s
     *   <li>A single 'false'' signal resets rise time
     *   <li>Rises after >= 1.0s of 'true' signal
     *   <li>Doesn't fall in under 1.5s
     *   <li>A single 'true' signal resets fall time
     *   <li>Falls after >= 1.5s of 'false' signal
     * </ul>
     */
    @Test
    void fastRiseSlowFall() {
        AsymmetricDebouncer debouncer = new AsymmetricDebouncer(1.0, 1.5);

        Assertions.assertFalse(debouncer.calculate(false), "Debouncer must initialize to false.");

        setMockTimeSeconds(0.99);
        Assertions.assertFalse(
                debouncer.calculate(true), "0.99s < 1.0s rise time, signal should remain false");

        // Send a 'false' signal to reset rise time
        debouncer.calculate(false);

        setMockTimeSeconds(1.0);
        Assertions.assertFalse(
                debouncer.calculate(true),
                "'false' signal should have reset rise timer at t=0.99s");

        setMockTimeSeconds(1.99);
        Assertions.assertTrue(
                debouncer.calculate(true),
                "1.99s - 0.99s >= 1.0s rise time, signal should rise to true");

        Assertions.assertTrue(
                debouncer.calculate(false), "0.0s < 1.5s fall time, signal should remain true");

        setMockTimeSeconds(3.0);
        Assertions.assertTrue(
                debouncer.calculate(false),
                "3.0s - 1.99s < 1.5s fall time, signal should remain true");

        // Send a 'true' signal to reset fall time
        debouncer.calculate(true);

        setMockTimeSeconds(3.5);
        Assertions.assertTrue(
                debouncer.calculate(false), "'true' signal should have reset fall tiemr at t=3.0s");

        setMockTimeSeconds(5.0);
        Assertions.assertFalse(
                debouncer.calculate(false),
                "5.0s - 3.5s >= 1.5s fall time, signal should fall to false");
    }
}
