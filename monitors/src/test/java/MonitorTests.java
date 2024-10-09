package coppercore.monitors.test;

public class MonitorTests {
    // TODO: FIGURE OUT HOW TO TEST THIS
    // Callbacks are hard :(

    // Mimics a changing robot state
    // boolean isStateValid = true;

    // @Test
    // public void nonStickyTest() {
    //     Monitor exampleMonitor =
    //             new Monitor("exampleMonitor", false, () -> isStateValid, 1.0, () -> {});

    //     exampleMonitor.periodic(0.0);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     isStateValid = false;
    //     exampleMonitor.periodic(1.0);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     exampleMonitor.periodic(1.25);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     exampleMonitor.periodic(2.0);
    //     Assertions.assertTrue(exampleMonitor.isFaulted());
    //     Assertions.assertTrue(exampleMonitor.isTriggered());

    //     isStateValid = true;
    //     exampleMonitor.periodic(3.0);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());
    // }

    // @Test
    // public void stickyTest() {
    //     // Mimics a changing robot state
    //     boolean isStateValid = true;

    //     Monitor exampleMonitor =
    //             new Monitor(
    //                     "exampleMonitor",
    //                     true,
    //                     () -> isStateValid,
    //                     1.0,
    //                     () -> {}); // TODO: Figure out how to test callback

    //     exampleMonitor.periodic(0.0);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     isStateValid = false;
    //     exampleMonitor.periodic(1.0);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     exampleMonitor.periodic(1.25);
    //     Assertions.assertFalse(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());

    //     exampleMonitor.periodic(2.0);
    //     Assertions.assertTrue(exampleMonitor.isFaulted());
    //     Assertions.assertTrue(exampleMonitor.isTriggered());

    //     isStateValid = true;
    //     exampleMonitor.periodic(3.0);
    //     Assertions.assertTrue(exampleMonitor.isFaulted());
    //     Assertions.assertFalse(exampleMonitor.isTriggered());
    // }
}
