package coppercore.controls.test.test_state_machine;

public class TestIntakeState extends AbstractTestState {

    public static TestIntakeState instance = new TestIntakeState();
    private static TestIntakeState();
    
    public void periodic(){
        sharedState = true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return (targetState == this)? targetState: TestIdleState.instance;
    }

}
