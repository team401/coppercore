package coppercore.controls.state_machine.transition;

/** Data holder for transition */
public abstract class TransitionBase<State, Trigger> {
    private final State source;
    private final State destination;
    private final Trigger trigger;


    /**
     * Creates Transition Data Object
     *
     * @param source Transition Source
     * @param destination Transition Destination
     * @param trigger Transition Trigger
     */
    public TransitionBase(
            State source, State destination, Trigger trigger) {
        this.source = source;
        this.destination = destination;
        this.trigger = trigger;
    }

    /**
     * Returns the transition source;
     *
     * @return source;
     */
    public State getSource() {
        return source;
    }

    /**
     * Returns the transition destination;
     *
     * @return destination;
     */
    public State getDestination() {
        return destination;
    }

    /**
     * Returns the transition trigger
     *
     * @return trigger
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * Checks if StateMachine can complete transition
     *
     * @return Ability to complete transition
     */
    public boolean canTransition() {
        return true;
    }

    /**
     * Checks if it is entering the same state it is already in.
     *
     * @return reentrant
     */
    public boolean isReentrant() {
        return source != null && source.equals(trigger);
    }

    public boolean runOnEntry() {
        return true;
    }

    public boolean runOnExit() {
        return true;
    }
}
