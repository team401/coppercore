package coppercore.controls.test.test_state_machine;

public class TestIdleState extends AbstractTestState {

    public static TestIdleState instance = new TestIdleState();
    private static TestIdleState();
    
    public void periodic(){
        sharedState = false;
    }

}