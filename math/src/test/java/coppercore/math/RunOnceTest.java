package coppercore.math;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RunOnceTest {
    @Test
    void runExecutesOnFirstCall() {
        RunOnce runOnce = new RunOnce();
        int[] count = {0};

        runOnce.run(() -> count[0]++);

        assertEquals(1, count[0]);
    }

    @Test
    void runIgnoresSubsequentCalls() {
        RunOnce runOnce = new RunOnce();
        int[] count = {0};

        runOnce.run(() -> count[0]++);
        runOnce.run(() -> count[0]++);
        runOnce.run(() -> count[0]++);

        assertEquals(1, count[0]);
    }

    @Test
    void resetAllowsRunningAgain() {
        RunOnce runOnce = new RunOnce();
        int[] count = {0};

        runOnce.run(() -> count[0]++);
        runOnce.reset();
        runOnce.run(() -> count[0]++);

        assertEquals(2, count[0]);
    }

    @Test
    void differentRunnablesOnlyFirstExecutes() {
        RunOnce runOnce = new RunOnce();
        boolean[] firstRan = {false};
        boolean[] secondRan = {false};

        runOnce.run(() -> firstRan[0] = true);
        runOnce.run(() -> secondRan[0] = true);

        assertTrue(firstRan[0]);
        assertFalse(secondRan[0]);
    }
}
