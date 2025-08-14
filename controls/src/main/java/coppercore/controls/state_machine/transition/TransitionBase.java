package coppercore.controls.state_machine.transition;

import java.util.function.Consumer;

/** Data holder for transition */
public class TransitionBase<State, Trigger> {
    private State source;
    private State destination;
    private Trigger trigger;
    private boolean internalTransition;

    /**
     * Creates Transition Data Object
     *
     * @param source Transition Source
     * @param destination Transition Destination
     * @param trigger Transition Trigger
     */
    public TransitionBase(State source, State destination, Trigger trigger) {
        this(source, destination, trigger, false);
    }

    /**
     * Creates Transition Data Object
     *
     * @param source Transition Source
     * @param destination Transition Destination
     * @param trigger Transition Trigger
     * @param internalTransition is Interal Transition
     */
    public TransitionBase(
            State source, State destination, Trigger trigger, boolean internalTransition) {
        this.source = source;
        this.destination = destination;
        this.trigger = trigger;
        this.internalTransition = internalTransition;
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
     * Returns if the transition fires the on enter and exit actions.
     *
     * @return internal
     */
    public boolean isInternal() {
        return internalTransition;
    }

    /**
     * Checks if it is entering the same state it is already in.
     *
     * @return reentrant
     */
    public boolean isReentrant() {
        return source != null && source.equals(trigger);
    }
}
