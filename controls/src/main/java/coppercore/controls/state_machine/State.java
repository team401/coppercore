package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.List;
import coppercore.controls.state_machine.StateMachine.Condition;

/** An abstract class representing a state in a state machine. */
public abstract class State<StateKey extends Enum<StateKey>, World> {

    protected boolean finished = false;
    protected StateKey requestedState = null;
    protected final List<Transition> transitions;

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
     * Determines the next state based on the defined transitions. Multiple transitions may be
     * defined; the first whose condition is true is taken.
     *
     * @return The StateKey of the next state, or null if no transition is taken
     */
    protected final StateKey getNextState(World world) {
        for (Transition transition : transitions) {
            if (transition.whenCondition.isFulfilledFor(world)) {
                return transition.toState;
            }
        }
        return null;
    }

    /**
     * Called when the state is entered. This is a internal method; use onEntry() to override
     * behavior.
     */
    protected final void _onEntry(World world) {
        finished = false;
        onEntry(world);
    }

    /**
     * Called when the state is exited. This is a internal method; use onExit() to override
     * behavior.
     */
    protected final void _onExit(World world) {
        onExit(world);
        requestedState = null;
    }

    /**
     * Called when the state is finished. This is a internal method; use onFinish() to override
     * behavior.
     */
    protected final void _onFinish(World world) {
        onFinish(world);
    }

    /**
     * Called periodically while in this state. This is a internal method; use periodic() to
     * override behavior.
     */
    protected final void _periodic(World world) {
        periodic(world);
    }

    /**
     * Marks the state as finished. This is different from requesting a transition to another state.
     * Because of this, calling finish() does not automatically cause a state transition. And the
     * state machine will only transition to another state if a transition condition is met, and the
     * state machine is updated. But it can be used in conjunction with transitionWhenFinished() to
     * trigger a transition. And it can be used to indicate that the state's work is done. When
     * finish() is called, the onFinish() method is also called.
     */
    protected final void finish(World world) {
        finished = true;
        _onFinish(world);
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
    protected void onEntry(World world) {}

    /**
     * Called when the state is exited. This method is called before a state transition. So it is
     * the last chance to perform any cleanup or final actions in this state.
     */
    protected void onExit(World world) {}

    /**
     * Called when the state is finished. This method is called when finish() is invoked. So it is
     * not guaranteed to be called before a state transition.
     */
    protected void onFinish(World world) {}

    /**
     * Called periodically while in this state. This method is called by the state machine's
     * periodic update.
     */
    protected abstract void periodic(World world);
    
    /** A record representing a transition from one state to another based on a condition. */
    // protected final static record Transition<TStateKey>(TStateKey nextState, BooleanSupplier condition) {}

    public class Transition {
        StateKey toState;
        Condition<World> whenCondition;

        Transition(StateKey toState, Condition<World> whenCondition) {
            this.toState = toState;
        }

    }

    public void addTransition(StateKey toState, Condition<World> whenCondition) {
        new Transition(toState, whenCondition);
    }
    
}