package coppercore.controls.test.test_state_machine;

import coppercore.controls.state_machine.AbstractState;

public abstract class AbstractTestState extends AbstractState {

    public static String sharedValue = "This could be a IO for an interface";
    public static int sharedCount = 0;
    public static boolean sharedState = false;
}
