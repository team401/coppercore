package coppercore.controls.test.test_state_machine;

import coppercore.controls.state_machine.AbstractState;

public class TestShootingPrepState extends AbstractTestState {

    public static TestShootingPrepState instance = new TestShootingPrepState();

    private TestShootingPrepState() {}
    ;

    public void periodic() {
        sharedState = true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return (targetState == TestShootingState.instance || targetState == this)
                ? targetState
                : TestIdleState.instance;
    }
}
