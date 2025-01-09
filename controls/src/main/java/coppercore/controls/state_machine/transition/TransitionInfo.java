package coppercore.controls.state_machine.transition;

public class TransitionInfo<State, Trigger> {
    private final State currentState;
    private State targetState;
    private final Trigger trigger;
    private boolean failed = false;
    private Transition<State, Trigger> transition;

    public TransitionInfo(State currentState, Trigger trigger) {
        this.currentState = currentState;
        this.trigger = trigger;
    }

    public void setTargetState(State targetState) {
        this.targetState = targetState;
    }

    public void setTransition(Transition<State, Trigger> transition) {
        targetState = transition.getDestination();
        this.transition = transition;
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getTargetState() {
        return targetState;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Transition getTransition() {
        return transition;
    }

    public void fail() {
        failed = true;
    }

    public boolean wasFail() {
        return failed;
    }
}
