package coppercore.controls.test.test_state_machine;

import coppercore.controls.state_machine.AbstractState;

public class TestIntakeState extends AbstractTestState {

    public static TestIntakeState instance = new TestIntakeState();

    private TestIntakeState() {}
    ;

    public void periodic() {
        sharedState = true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return (targetState == this) ? targetState : TestIdleState.instance;
    }
}
