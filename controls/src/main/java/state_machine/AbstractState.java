package coppercore.controls.state_machine;

public abstract class AbstractState {

    public void periodic() {}

    public boolean canEnter() {
        return true;
    }

    public void onExit() {}

    public void onEnter() {}

    public AbstractState getNextState(AbstractState targetState) {

        return targetState;
    }
}
