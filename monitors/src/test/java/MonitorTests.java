package coppercore.monitors.test;

import coppercore.monitors.Monitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MonitorTests {
    // TODO: FIGURE OUT HOW TO TEST THIS
    // Callbacks are hard :(

    // Mimics a changing robot state
    private boolean isStateValid = true;

    private boolean getIsStateValid() {
        return isStateValid;
    }

    @Test
    public void nonStickyTest() {
        isStateValid = true;

        Monitor exampleMonitor =
                new Monitor.MonitorBuilder()
                        .withName("exampleMonitor")
                        .withStickyness(false)
                        .withIsStateValidSupplier(() -> getIsStateValid())
                        .withTimeToFault(1.0)
                        .withFaultCallback(() -> {})
                        .build();

        exampleMonitor.periodic(0.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());

        isStateValid = false;
        exampleMonitor.periodic(1.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        exampleMonitor.periodic(1.25);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        exampleMonitor.periodic(2.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        isStateValid = true;
        exampleMonitor.periodic(3.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
    }

    @Test
    public void stickyTest() {
        // Mimics a changing robot state
        isStateValid = true;

        Monitor exampleMonitor =
                new Monitor.MonitorBuilder()
                        .withName("exampleMonitor")
                        .withStickyness(true)
                        .withIsStateValidSupplier(() -> getIsStateValid())
                        .withTimeToFault(1.0)
                        .withFaultCallback(() -> {})
                        .build();

        exampleMonitor.periodic(0.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());

        isStateValid = false;
        exampleMonitor.periodic(1.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(getIsStateValid());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        exampleMonitor.periodic(1.25);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        exampleMonitor.periodic(2.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());

        isStateValid = true;
        exampleMonitor.periodic(3.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
    }
}
