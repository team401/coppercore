package coppercore.controls.state_machine.transition;

/** Information about a Transition */
public class TransitionInfo<State, Trigger> {
    private final State currentState;
    private State targetState;
    private final Trigger trigger;
    private boolean failed = false;
    private Transition<State, Trigger> transition;
    private boolean isInternal;

    /**
     * Creates Information Object
     *
     * @param currentState current state
     * @param trigger trigger that was fired
     */
    public TransitionInfo(State currentState, Trigger trigger) {
        this.currentState = currentState;
        this.trigger = trigger;
    }

    /**
     * Sets the target state of attempted transition.
     *
     * @param targetState target state of transition
     */
    public void setTargetState(State targetState) {
        this.targetState = targetState;
    }

    /**
     * Sets the transition
     *
     * @param transition Transition
     */
    public void setTransition(Transition<State, Trigger> transition) {
        targetState = transition.getDestination();
        if (transition != null) {
            isInternal = transition.isInternal();
        }
        this.transition = transition;
    }

    /**
     * Returns the state the machine was in at the start of the transition.
     *
     * @return start state
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Returns the Target State defined by the transition.
     *
     * @return target state
     */
    public State getTargetState() {
        return targetState;
    }

    /**
     * Returns Trigger the was used in the fire event.
     *
     * @return trigger
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * Returns transition that was used.
     *
     * @return trastition
     */
    public Transition getTransition() {
        return transition;
    }

    /** Sets transition to failed */
    public void fail() {
        failed = true;
    }

    public boolean wasInternal() {
        return this.isInternal;
    }

    /**
     * Returns if the transition failed.
     *
     * @return did it fail
     */
    public boolean wasFail() {
        return failed;
    }
}
