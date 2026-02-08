package coppercore.math;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RunFirstTest {
    @Test
    void runExecutesOnFirstCall() {
        RunFirst runFirst = new RunFirst();
        int[] count = {0};

        runFirst.run(() -> count[0]++);

        assertEquals(1, count[0]);
    }

    @Test
    void runIgnoresSubsequentCalls() {
        RunFirst runFirst = new RunFirst();
        int[] count = {0};

        runFirst.run(() -> count[0]++);
        runFirst.run(() -> count[0]++);
        runFirst.run(() -> count[0]++);

        assertEquals(1, count[0]);
    }

    @Test
    void resetAllowsRunningAgain() {
        RunFirst runFirst = new RunFirst();
        int[] count = {0};

        runFirst.run(() -> count[0]++);
        runFirst.reset();
        runFirst.run(() -> count[0]++);

        assertEquals(2, count[0]);
    }

    @Test
    void differentRunnablesOnlyFirstExecutes() {
        RunFirst runFirst = new RunFirst();
        boolean[] firstRan = {false};
        boolean[] secondRan = {false};

        runFirst.run(() -> firstRan[0] = true);
        runFirst.run(() -> secondRan[0] = true);

        assertTrue(firstRan[0]);
        assertFalse(secondRan[0]);
    }
}
