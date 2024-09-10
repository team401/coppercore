package coppercore.controls.test.test_state_machine;

public class TestShootingState extends AbstractTestState {

    public static TestShootingState instance = new TestShootingState();
    private static TestShootingState();
    
    public void periodic(){
        sharedState = true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return (targetState == TestShootingState.instace || targetState == this)? targetState : TestIdleState.instance;
    }

}
