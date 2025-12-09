package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO: Add missing javadocs

/** An abstract class representing a state in a state machine. */
public abstract class State<World> {

    protected boolean finished = false;
    protected final List<Transition> transitions;
    protected Supplier<State<World>> requestedStateSupplier;

    /** Constructs a new State. */
    public State() {
        this.transitions = new ArrayList<>();
    }

    public void setRequestedStateSupplier(Supplier<State<World>> supplier) {
        this.requestedStateSupplier = supplier;
    }

    /**
     * Checks if the state has finished its operation. The finished state can be used in transition
     * conditions.
     *
     * @return true if the state is finished, false otherwise
     */
    protected final boolean isFinished() {
        return finished;
    }

    /**
     * Determines the next state based on the defined transitions. Multiple transitions may be
     * defined; the first whose condition is true is taken.
     *
     * @return The StateKey of the next state, or null if no transition is taken
     */
    protected final State<World> getNextState(World world) {
        for (Transition transition : transitions) {
            if (transition.whenCondition.test(world)) {
                return transition.toState;
            }
        }
        return null;
    }

    /**
     * Called when the state is entered. This is a internal method; use onEntry() to override
     * behavior.
     */
    protected final void _onEntry(StateMachine<World> stateMachine, World world) {
        finished = false;
        onEntry(stateMachine, world);
    }

    /**
     * Called when the state is exited. This is a internal method; use onExit() to override
     * behavior.
     */
    protected final void _onExit(StateMachine<World> stateMachine, World world) {
        onExit(stateMachine, world);
    }

    /**
     * Called when the state is finished. This is a internal method; use onFinish() to override
     * behavior.
     */
    protected final void _onFinish() {
        onFinish();
    }

    /**
     * Called periodically while in this state. This is a internal method; use periodic() to
     * override behavior.
     */
    protected final void _periodic(StateMachine<World> stateMachine, World world) {
        periodic(stateMachine, world);
    }

    /**
     * Marks the state as finished. This is different from requesting a transition to another state.
     * Because of this, calling finish() does not automatically cause a state transition. And the
     * state machine will only transition to another state if a transition condition is met, and the
     * state machine is updated. But it can be used in conjunction with transitionWhenFinished() to
     * trigger a transition. And it can be used to indicate that the state's work is done. When
     * finish() is called, the onFinish() method is also called.
     */
    protected final void finish() {
        finished = true;
        _onFinish();
    }

    // TODO: Move this comment to the requestState method in StateMachine.java
    /**
     * Requests a transition to another state. This is different from marking the state as finished.
     * Because of this, requesting a state does not automatically cause a state transition. And the
     * state machine will only transition to another state if a transition condition is met, and the
     * state machine is updated. Just because a state is requested does not mean the state machine
     * will transition to it.
     *
     * @param state The StateKey of the requested state
     */

    /**
     * Called when the state is entered. This method is called after a state transition. So it is
     * the first chance to perform any setup or initialization for this state.
     */
    protected void onEntry(StateMachine<World> stateMachine, World world) {}

    /**
     * Called when the state is exited. This method is called before a state transition. So it is
     * the last chance to perform any cleanup or final actions in this state.
     */
    protected void onExit(StateMachine<World> stateMachine, World world) {}

    /**
     * Called when the state is finished. This method is called when finish() is invoked. So it is
     * not guaranteed to be called before a state transition.
     */
    protected void onFinish() {}

    /**
     * Called periodically while in this state. This method is called by the state machine's
     * periodic update.
     */
    protected abstract void periodic(StateMachine<World> stateMachine, World world);

    public class Transition {
        State<World> toState;
        Predicate<World> whenCondition;

        Transition(State<World> toState, Predicate<World> whenCondition) {
            this.toState = toState;
            this.whenCondition = whenCondition;
        }
    }

    public class TransitionConditionBuilder {
        Predicate<World> condition;

        TransitionConditionBuilder(Predicate<World> condition) {
            this.condition = condition;
        }

        public TransitionConditionBuilder andWhen(Predicate<World> nextCondition) {
            this.condition = this.condition.and(nextCondition);
            return this;
        }

        public void transitionTo(State<World> toState) {
            transitions.add(new Transition(toState, condition));
        }
    }

    public TransitionConditionBuilder when(Predicate<World> condition) {
        return new TransitionConditionBuilder(condition);
    }

    public TransitionConditionBuilder whenFinished() {
        return when((world) -> isFinished());
    }

    public TransitionConditionBuilder whenRequested(State<World> requestedState) {
        return when(
                (world) ->
                        requestedState != null
                                && requestedState.equals(this.requestedStateSupplier.get()));
    }
}
