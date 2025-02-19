package coppercore.monitors.test;

import coppercore.monitors.Monitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MonitorTests {
    // Mimics a changing robot state
    private boolean isStateValid = true;

    private boolean getIsStateValid() {
        return isStateValid;
    }

    // Number to be incremented by fault callback
    private int faultCallbackCalls = 0;

    // Example fault callback to verify that the callback was called
    private void callFaultCallback() {
        faultCallbackCalls++;
    }

    @Test
    public void nonStickyTest() {
        isStateValid = true;

        Monitor exampleMonitor =
                new Monitor.MonitorBuilder()
                        .withAlertText("exampleMonitor")
                        .withStickyness(false)
                        .withIsStateValidSupplier(() -> getIsStateValid())
                        .withTimeToFault(1.0)
                        .withFaultCallback(
                                () -> {
                                    callFaultCallback();
                                })
                        .build();

        exampleMonitor.periodic(0.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        isStateValid = false;
        exampleMonitor.periodic(1.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        exampleMonitor.periodic(1.25);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        exampleMonitor.periodic(2.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 1);

        isStateValid = true;
        exampleMonitor.periodic(3.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 1);
    }

    @Test
    public void stickyTest() {
        // Mimics a changing robot state
        isStateValid = true;
        faultCallbackCalls = 0;

        Monitor exampleMonitor =
                new Monitor.MonitorBuilder()
                        .withAlertText("exampleMonitor")
                        .withStickyness(true)
                        .withIsStateValidSupplier(() -> getIsStateValid())
                        .withTimeToFault(1.0)
                        .withFaultCallback(
                                () -> {
                                    callFaultCallback();
                                })
                        .build();

        exampleMonitor.periodic(0.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        isStateValid = false;
        exampleMonitor.periodic(1.0);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertFalse(getIsStateValid());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        exampleMonitor.periodic(1.25);
        Assertions.assertFalse(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 0);

        exampleMonitor.periodic(2.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertTrue(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 1);

        isStateValid = true;
        exampleMonitor.periodic(3.0);
        Assertions.assertTrue(exampleMonitor.isFaulted());
        Assertions.assertFalse(exampleMonitor.isTriggered());
        Assertions.assertTrue(faultCallbackCalls == 2);
    }
}
