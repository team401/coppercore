package coppercore.controls.state_machine;

public abstract class AbstractState {

    public void onEnter() {}

    public void periodic() {}

    public void onExit() {}

    public boolean canEnter() {
        return true;
    }

    public AbstractState getNextState(AbstractState targetState) {
        return targetState;
    }

}
