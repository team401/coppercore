package coppercore.controls.test.test_state_machine;

import coppercore.controls.state_machine.AbstractState;

public class TestShootingState extends AbstractTestState {

    public static TestShootingState instance = new TestShootingState();

    private TestShootingState() {}
    ;

    public void periodic() {
        sharedState = true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return (targetState == this) ? targetState : TestIdleState.instance;
    }
}
