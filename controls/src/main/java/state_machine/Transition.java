package coppercore.controls;

import java.util.function.Consumer;

public class Transition<State, Trigger> {
    private State source;
    private State destination;
    private Trigger trigger;
    private Consumer<Transition> action;
    private boolean internalTransition;

    public Transition(State source, State destination, Trigger trigger) {
        this(source, destination, trigger, false);
    }

    public Transition(
            State source, State destination, Trigger trigger, boolean internalTransition) {
        this.source = source;
        this.destination = destination;
        this.trigger = trigger;
        this.internalTransition = internalTransition;
    }

    public State getSource() {
        return source;
    }

    public State getDestination() {
        return destination;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public boolean canTransition() {
        return true;
    }

    public void runAction() {
        if (action != null) {
            action.accept(this);
        }
    }

    public boolean isInternal() {
        return internalTransition;
    }

    public boolean isReentrant() {
        return source != null && source.equals(trigger);
    }
}
