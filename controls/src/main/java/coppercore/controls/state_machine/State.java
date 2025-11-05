package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** An abstract class representing a state in a state machine. */
public abstract class State<StateKey extends Enum<StateKey>> {

    /** A record representing a transition from one state to another based on a condition. */
    protected final record Transition<TStateKey>(TStateKey nextState, BooleanSupplier condition) {}

    protected boolean finished = false;
    protected StateKey requestedState = null;
    protected final List<Transition<StateKey>> transitions;

    /** Constructs a new State. */
    public State() {
        this.transitions = new ArrayList<>();
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
     * Adds a transition to another state when the specified condition is true. If multiple
     * transitions are defined, the first whose condition is true is taken.
     *
     * @param condition The condition to evaluate
     * @param state The StateKey of the next state
     * @return The current state for chaining
     */
    public final State<StateKey> transitionWhen(BooleanSupplier condition, StateKey state) {
        transitions.add(new Transition<>(state, condition));
        return this;
    }

    /**
     * Adds a transition to another state when the state is finished. If multiple transitions are
     * defined, the first whose condition is true is taken.
     *
     * @param state The StateKey of the next state
     * @return The current state for chaining
     */
    public final State<StateKey> transitionWhenFinished(StateKey state) {
        transitions.add(new Transition<>(state, this::isFinished));
        return this;
    }

    /**
     * Adds a transition to another state when the state is finished and the specified condition is
     * true. If the state is not finished, the condition is not evaluated. If the state is finished,
     * the condition is evaluated to determine if the transition should occur. And if multiple
     * transitions are defined, the first whose condition is true is taken.
     *
     * @param condition The condition to evaluate
     * @param state The StateKey of the next state
     * @return The current state for chaining
     */
    public final State<StateKey> transitionWhenFinishedAnd(
            BooleanSupplier condition, StateKey state) {
        transitions.add(
                new Transition<>(state, () -> this.isFinished() && condition.getAsBoolean()));
        return this;
    }

    /**
     * Adds a transition to another state when that state is requested. If multiple transitions are
     * defined, the first whose condition is true is taken. If the requested state does not match,
     * the condition is false. Only the most recently requested state is considered.
     *
     * @param state
     * @return
     */
    public final State<StateKey> transitionWhenRequested(StateKey state) {
        transitions.add(new Transition<>(state, () -> this.requestedState == state));
        return this;
    }

    /**
     * Determines the next state based on the defined transitions. Multiple transitions may be
     * defined; the first whose condition is true is taken.
     *
     * @return The StateKey of the next state, or null if no transition is taken
     */
    protected final StateKey getNextState() {
        for (Transition<StateKey> transition : transitions) {
            if (transition.condition.getAsBoolean()) {
                return transition.nextState;
            }
        }
        return null;
    }

    /**
     * Called when the state is entered. This is a internal method; use onEntry() to override
     * behavior.
     */
    protected final void _onEntry() {
        finished = false;
        onEntry();
    }

    /**
     * Called when the state is exited. This is a internal method; use onExit() to override
     * behavior.
     */
    protected final void _onExit() {
        onExit();
        requestedState = null;
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
    protected final void _periodic() {
        periodic();
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

    /**
     * Requests a transition to another state. This is different from marking the state as finished.
     * Because of this, requesting a state does not automatically cause a state transition. And the
     * state machine will only transition to another state if a transition condition is met, and the
     * state machine is updated. Just because a state is requested does not mean the state machine
     * will transition to it.
     *
     * @param state The StateKey of the requested state
     */
    public final void requestState(StateKey state) {
        this.requestedState = state;
    }

    /**
     * Called when the state is entered. This method is called after a state transition. So it is
     * the first chance to perform any setup or initialization for this state.
     */
    protected void onEntry() {}

    /**
     * Called when the state is exited. This method is called before a state transition. So it is
     * the last chance to perform any cleanup or final actions in this state.
     */
    protected void onExit() {}

    /**
     * Called when the state is finished. This method is called when finish() is invoked. So it is
     * not guaranteed to be called before a state transition.
     */
    protected void onFinish() {}

    /**
     * Called periodically while in this state. This method is called by the state machine's
     * periodic update.
     */
    protected abstract void periodic();
}
